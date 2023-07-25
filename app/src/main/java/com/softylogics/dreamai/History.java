package com.softylogics.dreamai;

import static com.softylogics.dreamai.MainActivity.account;
import static com.softylogics.dreamai.MainActivity.activeFragment;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.softylogics.dreamai.databinding.HistoryFragmentBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class History extends Fragment implements SetOnClickListener {


    DreamViewModel viewModel;
    private HistoryFragmentBinding binding;
    private List<DreamItem> itemList;
    private CustomAdapterCopy adapter;

    private Context c;
    private NavController navController;
    private int items = 0;
    private boolean itemRemoved = false;
    private boolean markedFavUnFav = false;
    private boolean allSelected = false;
    private boolean historyChecked;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HistoryFragmentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);
        c = getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        itemList = new ArrayList<>();
        adapter = new CustomAdapterCopy(itemList, this);
        binding.listRec.setAdapter(adapter);
        navController = Navigation.findNavController(view);

        //implementBannerAutoHideAndShow();
        fetchHistory();
        implementBackButtonFunctionality(view);
        implementSearchView();
        implementSelectAll();
        implementDeleteSelected();
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
        binding.listRec.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d("SelectedItemsDragging", "selected " + adapter.getSelectedItems().size());
                } else {
                    Log.d("SelectedItemsIdle", "selected" + adapter.getSelectedItems().size());
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                adapter.handleScrolling(firstVisibleItemPosition, lastVisibleItemPosition);

            }
        });
    }

    private void implementDeleteSelected() {
        binding.deleteSelected.setOnClickListener(v -> {
            if (adapter.getSelectedItems().size() > 0) {
                showDialogForDelSelected();
            }
        });
    }

    private void showDialogForDelSelected() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Confirm deletion!");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.dismiss();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.listRec.setEnabled(false);
            binding.listRec.setAlpha(0.5f);
            connectFirebaseToDeleteSelected();
//            ArrayList<DreamItem> itemsSelected = adapter.getSelectedItems();
//            for(DreamItem item: itemsSelected) {
//                itemList.remove(item);
//            }
//            adapter.setList(itemList);
            adapter.getSelectedItems().clear();
            adapter.disableSelection();
            showDelButton(false);
//            adapter.notifyDataSetChanged();



        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void connectFirebaseToDeleteSelected() {
        itemRemoved = false;
        if (account != null) {
            if (checkInternet() != 0) {
                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(account.getId());
//                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

                Log.d("firebase", "getRef");
                Map<String, Object> update = new HashMap<>();

                for (DreamItem item : adapter.getSelectedItems()) {
                    update.put("/" + item.getDate() + "/", null);
                    Log.d("updates", "/" + item.getDate() + "/");
                }

                Log.d("firebase", "aftergettingref");

                myRef.updateChildren(update).addOnCompleteListener(task -> {
                    showToast("Deletion successful");
                    itemRemoved = true;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.listRec.setEnabled(true);
                    binding.listRec.setAlpha(1f);

                });
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (!itemRemoved) {
                        showToast("There was a problem in deleting, please check your internet connection");
                    }
                }, 10000);

            } else {
                showToast("No internet connection");
            }
        } else {

            showToast("Cannot delete without signing in");
        }
    }

    private void implementSelectAll() {
        binding.selectAll.setOnClickListener(v -> {
            if (!allSelected) {
                adapter.selectAll();
                allSelected = true;
                binding.selectAll.setBackground((getActivity().getResources()).getDrawable(R.drawable.baseline_deselect_24));
                adapter.notifyDataSetChanged();
            } else {
                adapter.deSelectAll();
                allSelected = false;
                binding.selectAll.setBackground(getActivity().getResources().getDrawable(R.drawable.baseline_select_all_24));
                adapter.notifyDataSetChanged();
            }
        });

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
                for (DreamItem item : itemList) {
                    if (query.length() <= item.getPrompt().length()) {
                        if (item.getPrompt().toLowerCase().contains(query.toString().toLowerCase())) {
                            if(!tempArrayList.contains(item)) {
                                tempArrayList.add(item);
                            }
                        }
                    }
                    if (query.length() <= item.getInterpretation().length()) {
                        if (item.getInterpretation().toLowerCase().contains(query.toString().toLowerCase())) {
                            if(!tempArrayList.contains(item)) {
                                tempArrayList.add(item);
                            }
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
                if (newText.length() == 0) {
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
                if (adapter.getSelectionEnable()) {
                    adapter.deSelectAll();
                    adapter.disableSelection();
                    showDelButton(false);
                    adapter.notifyDataSetChanged();
                } else {
                    if(activeFragment!=Constants.JOURNAL_FRAGMENT) {
                        activeFragment = Constants.JOURNAL_FRAGMENT;
                        Navigation.findNavController(view).navigate(R.id.action_navigation_history_to_navigation_journal);
                    }
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);


    }

    private void animateBanner() {
        TranslateAnimation translateAnimation = new TranslateAnimation(-200, 0, 0, 0); // Parameters: (fromXDelta, toXDelta, fromYDelta, toYDelta)
        translateAnimation.setDuration(500); // Animation duration in milliseconds
        translateAnimation.setFillAfter(true); // Keep the final position of the view after animation

        // Create fade-in animation
        AlphaAnimation fadeInAnimation = new AlphaAnimation(0f, 1f);
        fadeInAnimation.setDuration(1000); // Animation duration in milliseconds

        // Create animation set
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(fadeInAnimation);
        animationSet.addAnimation(translateAnimation);

        binding.roundedLayout.setVisibility(View.VISIBLE); // Set the visibility of the LinearLayout to visible before starting the animation
        binding.roundedLayout.startAnimation(animationSet);
    }

    private void fetchHistory() {
        historyChecked = false;

        if (account != null) {
            if (checkInternet() != 0) {
                // Create a Handler object


                Log.d("firebase", "getRef");
                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(account.getId());

//                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
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
                            itemList.add(itemDecrypted);
                            Log.d("firebase", "ondatachange-forloop");

                        }
                        Log.d("firebase", "ondatachange-after-forloop");

                        binding.progressBar.setVisibility(View.GONE);
                        binding.listRec.setVisibility(View.VISIBLE);
                        if (itemList.size() == 0) {
                            showToast("No History Found");


                        }
                        Log.d("items", itemList.size() + "");
                        items = itemList.size();
                        adapter.setList(itemList);
                        adapter.notifyDataSetChanged();
                        Log.d("firebase", "ondatachangeEnd");
                        historyChecked = true;

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
                        if (!historyChecked) {
                            showToast("There was a problem fetching your history, please check your internet connection");
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

    private void implementBannerAutoHideAndShow() {
        binding.listRec.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private static final int HIDE_THRESHOLD = 20; // Adjust this threshold as needed
            private int scrollDistance = 0;
            private boolean isVisible = true;

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Update the scroll distance
                scrollDistance += dy;

                // Check if the linear layout should be shown or hidden
                if (scrollDistance > HIDE_THRESHOLD && isVisible) {
                    hideLinearLayout();
                    isVisible = false;
                    scrollDistance = 0;
                } else if (scrollDistance < -HIDE_THRESHOLD && !isVisible) {
                    showLinearLayout();
                    isVisible = true;
                    scrollDistance = 0;
                }
            }

            private void hideLinearLayout() {
                binding.roundedLayout.animate().translationY(-binding.roundedLayout.getHeight() - 50).setDuration(200).start();
            }

            // Helper method to show the linear layout
            private void showLinearLayout() {
                binding.roundedLayout.animate().translationY(0).setDuration(200).start();
            }
        });

    }


    @Override
    public void onItemClick(DreamItem item, int position) {
        viewModel.setDreamItem(item);
        if(activeFragment!=Constants.HISTORY_DETAIL_FRAGMENT) {
            activeFragment = Constants.HISTORY_DETAIL_FRAGMENT;
            navController.navigate(R.id.action_navigation_history_to_history_detail);
        }
    }

    @Override
    public void onLongItemClick(DreamItem item, int position) {

    }

    @Override
    public void shareDream(DreamItem item, int position) {


            String appLink = "https://play.google.com/store/apps/details?id=com.softylogics.dreamai";
            String appName = "DreamAI";

            String shareText = "Check out this dream I had:\n\n" +
                    "Dream: " + item.getPrompt() + "\n" +
                    "Interpretation: " + item.getInterpretation() + "\n\n" +
                    "Download " + appName + " app to explore more dreams:\n" + appLink;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared Dream");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "Share via"));

    }

    @Override
    public void delDream(DreamItem item, int position) {
        itemRemoved = false;
        showdialog(item, position);

    }

    private void showdialog(DreamItem item, int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm deletion!");
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                connectFirebaseToDelete(item, position);
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.listRec.setEnabled(false);
                binding.listRec.setAlpha(0.8f);
            }
        });
        builder.setNegativeButton("No, Thanks", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void connectFirebaseToDelete(DreamItem item, int position) {

        if (account != null) {
            if (checkInternet() != 0) {
                Log.d("firebase", "getRef");
//                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(account.getId());
                Log.d("firebase", "aftergettingref");
                myRef.child(String.valueOf(item.getDate())).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showToast("Deleted successfully");
                            itemRemoved = true;


                            refreshLayout();
                            //  adapter.notifyDataSetChanged();
//                            fetchHistory();
                        } else {
                            refreshLayout();
                            showToast("A problem occurred, please try again later");
                        }
                    }
                });


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!itemRemoved) {
                            showToast("There was a problem in deleting, please check your internet connection");
                            refreshLayout();

                        }
                    }
                }, 10000);


            } else {
                showToast("No internet connection");
                binding.progressBar.setVisibility(View.GONE);

            }

        } else {

            showToast("Cannot delete without signing in");
        }


    }

    private void refreshLayout() {
        binding.progressBar.setVisibility(View.GONE);
        binding.listRec.setEnabled(true);
        binding.listRec.setAlpha(1);
        adapter.notifyDataSetChanged();


    }

    @Override
    public void markFav(DreamItem item, int position) {
        markedFavUnFav = false;
        if (checkInternet() != 0) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.listRec.setEnabled(false);
            binding.listRec.setAlpha(0.7f);
            if (account != null) {
                Log.d("firebase", "getRef");
//            DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

            DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(account.getId());
                Log.d("firebase", "aftergettingref");
                myRef.child(String.valueOf(item.getDate())).child("isFavorite").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {


                            refreshLayout();
                            markedFavUnFav = true;
                            showToast("Marked as Favorite, you can find this item in your favorites now");
                        } else {
                            refreshLayout();
                            showToast("A problem occurred, please try again later");
                        }
                    }
                });


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!markedFavUnFav) {
                            showToast("There was a problem in deleting, please check your internet connection");
                            refreshLayout();

                        }
                    }
                }, 10000);


            } else {
                showToast("Cannot carry out this function without signing in");
                refreshLayout();

            }

        } else {
            showToast("No internet connection");

        }

    }

    @Override
    public void unMarkFav(DreamItem item, int adapterPosition) {
        markedFavUnFav = false;
        if (account != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.listRec.setEnabled(false);
            binding.listRec.setAlpha(0.7f);
            if (checkInternet() != 0) {
                Log.d("firebase", "getRef");
//                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();

                DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(account.getId());
                Log.d("firebase", "aftergettingref");
                myRef.child(String.valueOf(item.getDate())).child("isFavorite").setValue(false).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {


                            refreshLayout();
                            markedFavUnFav = true;
                            showToast("Un-Marked as Favorite, your favorites are updated");

                        } else {
                            refreshLayout();
                            showToast("A problem occurred, please try again later");
                        }
                    }
                });


                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!markedFavUnFav) {
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
    public void showDelButton(boolean show) {
        if (show) {
            binding.deleteSelected.setAlpha(1f);
            binding.selectAll.setAlpha(1f);
            binding.deleteSelected.setEnabled(true);
            binding.selectAll.setEnabled(true);

        } else {
            binding.deleteSelected.setAlpha(0f);
            binding.selectAll.setAlpha(0f);
            binding.deleteSelected.setEnabled(false);
            binding.selectAll.setEnabled(false);

        }
    }

    @Override
    public void toggleFavorite(DreamItem item, int adapterPosition) {
        if (item.getFavorite()) {
            unMarkFav(item, adapterPosition);
        } else {
            markFav(item, adapterPosition);
        }
    }

    private void showToast(String message) {
        Toast.makeText(c, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
