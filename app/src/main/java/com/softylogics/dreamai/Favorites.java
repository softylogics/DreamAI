package com.softylogics.dreamai;

import static com.softylogics.dreamai.MainActivity.account;
import static com.softylogics.dreamai.MainActivity.activeFragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.softylogics.dreamai.databinding.FavoriteFragmentBinding;
import com.softylogics.dreamai.databinding.HistoryFragmentBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Favorites extends Fragment implements SetOnClickListener {

    DreamViewModel viewModel;
    private FavoriteFragmentBinding binding;
    private List<DreamItem> itemList;
    private CustomFavoriteAdapter adapter;

    private Context c;
    private NavController navController;
    private int items = 0;
    private boolean itemRemoved = false;
    private boolean markedFavUnFav = false;
    private boolean allSelected = false;
    boolean unAbleToDelete = false;
    private boolean favChecked;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FavoriteFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);
        c = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        itemList = new ArrayList<>();
        adapter = new CustomFavoriteAdapter(itemList, this);
        binding.listRec.setAdapter(adapter);
        navController = Navigation.findNavController(view);

      //  implementBannerAutoHideAndShow();
        fetchFavorites();
        implementBackButtonFunctionality(view);
        implementSearchView();


//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        itemList.add(new DreamItem("asd", "dasd", 1231231231, false, false));
//        binding.progressBar.setVisibility(View.GONE);
//        binding.listRec.setVisibility(View.VISIBLE);
//        adapter.setList(itemList);
//        adapter.notifyDataSetChanged();

    }



    private void implementSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.listRec.setEnabled(false);
                binding.listRec.setAlpha(0.5f);

                // Perform the search operation using the query
                ArrayList<DreamItem> tempArrayList = new ArrayList<>();
                for(DreamItem item : itemList){
                    if (query.length() <= item.getPrompt().length()) {
                        if (item.getPrompt().toLowerCase().contains(query.toString().toLowerCase())) {
                            tempArrayList.add(item);
                        }
                    }
                    if (query.length() <= item.getInterpretation().length()) {
                        if (item.getInterpretation().toLowerCase().contains(query.toString().toLowerCase())) {
                            tempArrayList.add(item);
                        }
                    }
                }
                adapter.setList(tempArrayList);
                adapter.notifyDataSetChanged();
                binding.progressBar.setVisibility(View.GONE);
                binding.listRec.setEnabled(true);
                binding.listRec.setAlpha(1f);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length()==0){
                    adapter.setList(itemList);
                    adapter.notifyDataSetChanged();
                }
                return false;
            }
        });
    }


    private void implementBackButtonFunctionality(View view) {


            // This callback will only be called when MyFragment is at least Started.
            OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
                @Override
                public void handleOnBackPressed() {
                    if(activeFragment!=Constants.MORE_FRAGMENT){
                        activeFragment = Constants.MORE_FRAGMENT;
                        Navigation.findNavController(view).navigate(R.id.action_favorite_to_navigation_more);
                    }
                    }

            };
            requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);


    }

    private void fetchFavorites() {
        favChecked = false;

        if (account != null) {
            if (checkInternet() != 0) {
                // Create a Handler object



                Log.d("firebase", "getRef");
//                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(account.getId());
                Log.d("firebase", "aftergettingref");

                myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Log.d("firebase", "ondatachangeStart");
                            int counter = 0;
                            itemList.clear();
                            for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                DreamItem item = itemSnapshot.getValue(DreamItem.class);
                                DreamItem itemDecrypted = null;
                                try {
                                    itemDecrypted = new DreamItem(AESUtils.decrypt(item.getPrompt()), AESUtils.decrypt(item.getInterpretation()),item.getDate(), item.getFavorite(), item.getIsChecked());
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                                counter++;
                                if(itemDecrypted.getFavorite()) {
                                    itemList.add(itemDecrypted);
                                }
                                Log.d("firebase", "ondatachange-forloop");

                            }
                            Log.d("firebase", "ondatachange-after-forloop");

                            binding.progressBar.setVisibility(View.GONE);
                            binding.listRec.setVisibility(View.VISIBLE);
                            if (itemList.size() == 0) {
                                showToast("No Favorites Found");



                            }
                            Log.d("items" , itemList.size() + "");
                            items = itemList.size();
                            adapter.setList(itemList);
                            adapter.notifyDataSetChanged();
                            Log.d("firebase", "ondatachangeEnd");
                            favChecked = true;

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            showToast(error.getMessage());
                            binding.progressBar.setVisibility(View.GONE);
                        }

                    });

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!favChecked){
                            showToast("There was a problem fetching your favorites, please check your internet connection");
                            binding.progressBar.setVisibility(View.GONE);

                        }
                    }
                }, 10000);



            } else {
                showToast("No internet connection");
                binding.progressBar.setVisibility(View.GONE);

            }

        } else {

            binding.progressBar.setVisibility(View.GONE);
            binding.txtMessage.setVisibility(View.VISIBLE);


        }
    }

    private int checkInternet() {
        int result = 0; // Returns connection type. 0: none; 1: mobile data; 2: wifi
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = 3;
                    }
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = 2;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = 1;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_VPN) {
                        result = 3;
                    }
                }
            }
        }
        return result;


    }


    @Override
    public void onItemClick(DreamItem item, int position) {
        viewModel.setDreamItem(item);
        if(activeFragment!=Constants.FAVORITE_DETAIL_FRAGMENT) {
            activeFragment = Constants.FAVORITE_DETAIL_FRAGMENT;
            navController.navigate(R.id.action_favorite_to_favorite_detail);
        }

    }

    @Override
    public void onLongItemClick(DreamItem item, int position) {

    }

    @Override
    public void shareDream(DreamItem item, int position) {

            // share the text content as before
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareText = "Dream: " + item.getPrompt() + "\nInterpretation: " + item.getInterpretation();
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "Share via"));

    }

    @Override
    public void delDream(DreamItem item, int position) {

    }

    @Override
    public void markFav(DreamItem item, int position) {

    }


    private void refreshLayout() {
        binding.progressBar.setVisibility(View.GONE);
        binding.listRec.setEnabled(true);
        binding.listRec.setAlpha(1);
        adapter.notifyDataSetChanged();


    }

    @Override
    public void unMarkFav(DreamItem item, int adapterPosition) {
        markedFavUnFav = false;
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.listRec.setEnabled(false);
        binding.listRec.setAlpha(0.7f);
        if (account != null) {
            if (checkInternet() != 0) {
                Log.d("firebase", "getRef");
//                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(account.getId());
                Log.d("firebase", "aftergettingref");
                myRef.child(String.valueOf(item.getDate())).child("isFavorite").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            adapter.setList(itemList);
                            refreshLayout();
                            markedFavUnFav = true;
                            showToast("Un-Marked as Favorite, your favorites are updated");

                        }
                        else{
                            refreshLayout();
                            showToast("A problem occurred, please try again later");
                        }
                    }
                });


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!markedFavUnFav){
                            showToast("There was a problem in deleting, please check your internet connection");
                            refreshLayout();

                        }
                    }
                }, 10000);



            } else {
                showToast("No internet connection");
                refreshLayout();

            }

        } else {

            showToast("Cannot carry out this function without signing in");
        }

    }

    @Override
    public void showDelButton(boolean b) {

    }

    @Override
    public void toggleFavorite(DreamItem item, int adapterPosition) {
        if(item.getFavorite()){
            unMarkFav(item, adapterPosition);
        }

    }

    private void showToast(String message) {
        Toast.makeText(c, message, Toast.LENGTH_LONG).show();
    }


}
