package com.softylogics.dreamai;

import static com.softylogics.dreamai.MainActivity.account;
import static com.softylogics.dreamai.MainActivity.activeFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.softylogics.dreamai.databinding.FragmentHistoryDetailBinding;
import com.squareup.picasso.Picasso;


public class HistoryDetailFragment extends Fragment {

    private FragmentHistoryDetailBinding binding;
    private DreamViewModel viewModel;
    private Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentHistoryDetailBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DreamItem item = viewModel.getSelectedItem().getValue();
        binding.txtPrompt.setText(item.getPrompt());
        animateText(item);
        binding.btnShareInter.setVisibility(View.VISIBLE);
        binding.btnShareInter.setOnClickListener(v->{

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
        });

        backButtonImplementation(view);
    }

    private void backButtonImplementation(View view) {
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if(activeFragment!=Constants.HISTORY_FRAGMENT) {
                    handler.removeCallbacksAndMessages(null);
                    handler = null;
                    activeFragment = Constants.HISTORY_FRAGMENT;
                    Navigation.findNavController(view).navigate(R.id.action_history_detail_to_navigation_history);
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getActivity(), callback);


    }

    private void animateText(DreamItem item) {
        handler = new Handler();
        final int delay = 5; //milliseconds
        handler.postDelayed(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                if (i < item.getInterpretation().length()) {
                    binding.txtResult.append("" + item.getInterpretation().charAt(i));
                    binding.wholeScrollView.post(() -> {
                        binding.wholeScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    });
                    i++;
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (account != null) {
            Picasso.with(getContext()).load(account.getPhotoUrl()).into(binding.ivProfilePic);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}