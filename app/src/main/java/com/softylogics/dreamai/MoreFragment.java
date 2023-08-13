package com.softylogics.dreamai;

import static com.softylogics.dreamai.Constants.CONST_SIGN_IN;
import static com.softylogics.dreamai.MainActivity.account;
import static com.softylogics.dreamai.MainActivity.activeFragment;
import static com.softylogics.dreamai.MainActivity.anonymousUserId;
import static com.softylogics.dreamai.MainActivity.billingClientHelper;

import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.softylogics.dreamai.databinding.FragmentMoreBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class MoreFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener{


    private FragmentMoreBinding binding;
    private DreamViewModel viewModel;

    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;






    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentMoreBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        UserPreferences.init(getContext());
        UserPreferences.getPrefs().registerOnSharedPreferenceChangeListener(this);
        binding.loginOut.setOnClickListener(this);
        binding.txtBuyPremium.setOnClickListener(this);

//        binding.txtRestoreSub.setOnClickListener(this);
        binding.txtDreamInterpretationGuide.setOnClickListener(this);
        binding.txtFeedback.setOnClickListener(this);
        binding.txtDreamGuide.setOnClickListener(this);
        binding.txtPrivacyPolicy.setOnClickListener(this);
        binding.txtRateApp.setOnClickListener(this);
        binding.txtShareApp.setOnClickListener(this);
        binding.favoritesInMore.setOnClickListener(this);
        String text = "Your id: " + anonymousUserId;
//        binding.txtUSerId.setText(text);
//        binding.txtUSerId.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                copyTextToClipboard();
//                return true;
//            }
//        });
        updateUI(account);
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(view).navigate(R.id.action_navigation_more_to_navigation_journal);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), callback);
        if(UserPreferences.getBoolean(Constants.IS_PURCHASED)){
            binding.txtBuyPremium.setEnabled(false);
            binding.txtBuyPremium.setAlpha(0.5f);
        }


    }


//    private void copyTextToClipboard() {
//        String text = binding.txtUSerId.getText().toString().trim();
//        if (!text.isEmpty()) {
//            ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//            clipboard.setText(text);
//            showToast("Text copied to clipboard");
//        }
//    }
    private void googleSignIn() {
        binding.moreScrollView.setEnabled(false);
        binding.moreScrollView.setAlpha(0.8f);
        binding.loading.setVisibility(View.VISIBLE);
        binding.loginOut.setEnabled(false);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, CONST_SIGN_IN);


    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONST_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignIn(task);
        }
    }
    private void handleSignIn(Task<GoogleSignInAccount> task) {
        // The Task returned from this call is always completed, no need to attach
        // a listener.


        try {
            account = task.getResult(ApiException.class);


            updateUI(account);
            binding.loading.setVisibility(View.GONE);
            binding.moreScrollView.setEnabled(true);
            binding.moreScrollView.setAlpha(1f);


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            showToast("There was some error signing in.");
            binding.loading.setVisibility(View.GONE);
            binding.moreScrollView.setEnabled(true);
            binding.moreScrollView.setAlpha(1f);

            binding.loginOut.setEnabled(true);
        }


    }
    private void updateUI(GoogleSignInAccount account) {
        if(account!=null){
            Picasso.with(getContext()).load(account.getPhotoUrl()).into(binding.profilePicture);
            binding.txtLog.setText("Sign Out");
        }
        else{
            binding.profilePicture.setImageDrawable(getResources().getDrawable(R.drawable.user_icon));
            binding.txtLog.setText("Sign In");
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }
    private void signOut() {
        binding.moreScrollView.setEnabled(false);
        binding.moreScrollView.setAlpha(0.8f);

        binding.loading.setVisibility(View.VISIBLE);
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                handleSignOut();
                            }
                        }, 3000);
                    } else {
                        showToast("There was an error signing out");
                    }
                });
    }

    private void handleSignOut() {
        account = null;
        updateUI(null);
        showToast("You are successfully signed out");
        binding.loading.setVisibility(View.GONE);
        binding.moreScrollView.setEnabled(true);
        binding.moreScrollView.setAlpha(1f);

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.txtBuyPremium) {
            if(billingClientHelper.getClient().isReady()) {
                if (!UserPreferences.getBoolean(Constants.IS_PURCHASED)) {
                    if(billingClientHelper.getClient().isReady()) {//todo: test this
                        billingClientHelper.startPurchaseFlow(getActivity());
                    }
                    else{
                        showToast("There was an error in starting the billing process, please try again after some time");
                    }
//                    if(UserPreferences.getBoolean(Constants.IS_PURCHASED)){
//                        binding.txtBuyPremium.setEnabled(false);
//                        binding.txtBuyPremium.setAlpha(0.5f);
//                    }
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Already Subscribed")
                            .setMessage("You are already subscribed to our premium plan. Enjoy all the benefits!")
                            .setPositiveButton("OK", null)
                            .show();
                }
            }
        }
//        else if (v.getId() == R.id.txtRestoreSub) {
//            if(billingClientHelper.getClient().isReady()) {
//                if (!UserPreferences.getBoolean(Constants.IS_PURCHASED)) {
//                    billingClientHelper.restoreSubscriptions();
//                }
//                else{
//                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                    builder.setTitle("Subscription Already Restored")
//                            .setMessage("Your subscription has already been successfully restored. You can continue enjoying all the benefits of our premium plan.")
//                            .setPositiveButton("OK", null)
//                            .show();
//                }
//            }
//        }
        else if(v.getId() == R.id.txtDreamGuide){
            Uri privacyPolicyUri = Uri.parse("https://ai-dream.web.app/Dream%20Symbols%20Guide.html");
            Intent intent = new Intent(Intent.ACTION_VIEW, privacyPolicyUri);
            startActivity(intent);
        }
        else if(v.getId() == R.id.txtDreamInterpretationGuide){
            Log.d("more" , "inter guide");
            Uri privacyPolicyUri = Uri.parse("https://ai-dream.web.app/index.html");
            Intent intent = new Intent(Intent.ACTION_VIEW, privacyPolicyUri);
            startActivity(intent);
        }

        else if(v.getId() == R.id.txtFeedback){
            Log.d("more" , "feed");
            Uri privacyPolicyUri = Uri.parse("https://ai-dream.web.app/Feedback.html");
            Intent intent = new Intent(Intent.ACTION_VIEW, privacyPolicyUri);
            startActivity(intent);

        }
        else if(v.getId() == R.id.txtPrivacyPolicy){
            Uri privacyPolicyUri = Uri.parse("https://ai-dream.web.app/Privacy%20Policy.html");
            Intent intent = new Intent(Intent.ACTION_VIEW, privacyPolicyUri);
            startActivity(intent);

        }else if(v.getId() == R.id.txtRateApp){
            try {
                // Replace "com.example.myapp" with your app's package name
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.softylogics.dreamai")));
            } catch (ActivityNotFoundException e) {
                // If the Play Store app is not installed, open the website in a browser
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.softylogics.dreamai")));
            }
        }else if(v.getId() == R.id.txtShareApp){
            try {
                startActivity(new Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_SUBJECT, "Check out this awesome app!")
                        .putExtra(Intent.EXTRA_TEXT, "Download the app from the Google Play Store: https://play.google.com/store/apps/details?id=com.softylogics.dreamai"));
            } catch (ActivityNotFoundException e) {
                showToast("No app found to handle the share action.");
            }
        }
        else if(v.getId()==R.id.favorites_in_more){
            Log.d("more" , "fav");
            if(activeFragment!=Constants.FAVORITE_FRAGMENT) {
                activeFragment = Constants.FAVORITE_FRAGMENT;
                Navigation.findNavController(v).navigate(R.id.action_navigation_more_to_favorite);
            }
        }
        else if(v.getId() == R.id.login_out){
            if(account!=null){
                signOut();
            }
            else{
                googleSignIn();
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if(billingClientHelper.getClient()!=null){
            billingClientHelper.queryPurchases();
        }
    }

    @Override
    public void onStart() {
        super.onStart();


        if(account != null){
            updateUI(account);
        }

    }

    @Override
    public void onDestroy() {
        if (billingClientHelper.getClient() != null) {
            billingClientHelper.getClient().endConnection();
        }
        super.onDestroy();

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(Constants.IS_PURCHASED)){
            if(sharedPreferences.getBoolean(Constants.IS_PURCHASED, false)){
                binding.txtBuyPremium.setEnabled(false);
                binding.txtBuyPremium.setAlpha(0.5f);
            }
        }
    }
}