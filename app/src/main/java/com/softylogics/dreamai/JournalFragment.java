package com.softylogics.dreamai;

import static android.app.Activity.RESULT_OK;
import static com.softylogics.dreamai.Constants.AD_UNIT_ID;
import static com.softylogics.dreamai.Constants.API_ENDPOINT;
import static com.softylogics.dreamai.Constants.NEVER_REMIND_SIGNIN;
import static com.softylogics.dreamai.Constants.REQUEST_RECORD_AUDIO_PERMISSION;
import static com.softylogics.dreamai.Constants.REQUEST_SPEECH_TO_TEXT;
import static com.softylogics.dreamai.Constants.TAG_AD;
import static com.softylogics.dreamai.Constants.token;

import static com.softylogics.dreamai.MainActivity.account;
import static com.softylogics.dreamai.MainActivity.activeFragment;
import static com.softylogics.dreamai.MainActivity.anonymousUserId;
import static com.softylogics.dreamai.MainActivity.dreamCounter;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.softylogics.dreamai.databinding.FragmentJournalBinding;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JournalFragment extends Fragment {
    // todo: pivot point or shrink animation

    // dark mode colors
    // add functionality for google action
    // hotpot ai attribution
    // db local synch when internet




    private UpdateApp updateApp;
    private FragmentJournalBinding binding;

    private DreamViewModel viewModel;
    private InterstitialAd interstitialAd;

    private Handler handler;
    private boolean doubleBackToExitPressedOnce = false;

    private SpeechRecognizer speechRecognizer;
    private JSONObject jsonResponse;
    private NavController navController;


    private boolean requestingAd = false;
    private boolean isFragmentAttached = false;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentJournalBinding.inflate(inflater, container, false);


        return binding.getRoot();


    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        int centerX = binding.txtBkwas.getWidth() / 2;
        int centerY = binding.txtBkwas.getHeight() / 2;


        navController = Navigation.findNavController(view);

        UserPreferences.init(getActivity()); // todo: check if this make any error coz init called in MainActivity and is being used here without init
        // Set the pivot point to the center of the TextView
        binding.txtBkwas.setPivotX(centerX);
        binding.txtBkwas.setPivotY(centerY);
        implementBackButtonFunctionality();
        viewModel = new ViewModelProvider(requireActivity()).get(DreamViewModel.class);
        binding.btnEditPrompt.setOnClickListener(v -> {
            binding.dream.setText(binding.txtPrompt.getText());
        });

        binding.dream.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (binding.ivLogo.getVisibility() == View.VISIBLE) {
                        Animation shrinkAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.shrink);
                        shrinkAnimation.setFillAfter(true);

                        binding.ivLogo.startAnimation(shrinkAnimation);
                        binding.txtBkwas.startAnimation(shrinkAnimation);
                        animateWelcomeTextView();
                        shrinkAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                // Animation started
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                // Animation ended, perform your desired actions here

                                binding.ivLogo.setVisibility(View.GONE);
                                binding.txtBkwas.setVisibility(View.GONE);


                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                                // Animation repeated
                            }
                        });
                    }
                }
            }
        });

        binding.dreamInput.setEndIconOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            Rect r = new Rect();
            binding.getRoot().getWindowVisibleDisplayFrame(r);
            int screenHeight = binding.getRoot().getRootView().getHeight();
            Log.d("keyboard", screenHeight + "screenheight");
            int keyboardHeight = screenHeight - r.bottom;
            Log.d("keyboard", keyboardHeight + "keyboardheight");
            // If the keyboard height is 0, it means the soft keyboard is hidden
            if (!TextUtils.isEmpty(binding.dream.getText().toString())) {
                binding.dreamInput.setEndIconVisible(false);
                binding.dreamInput.setEndIconActivated(false);
                binding.btnMic.setEnabled(false);
                binding.progressBar.setVisibility(View.VISIBLE);
                if (checkInternet() != 0) {

                    String prompt = "You are a dream interpreter AI. Please interpret the following dream for me:" +

                            "Dream: " + binding.dream.getText().toString();


                    callAPI(prompt);
                } else {
                    showToast("No internet connection");
                    binding.progressBar.setVisibility(View.GONE);
                    binding.dreamInput.setEndIconVisible(true);
                    binding.dreamInput.setEndIconActivated(true);
                    binding.btnMic.setEnabled(true);

                }
            } else {
                showToast("Please write your dream in the input field");
            }

        });


        binding.dream.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 200) {
                    binding.dreamInput.setError("Character Limit Reached");
                } else {
                    binding.dreamInput.setError(null);
                }
            }
        });


        binding.btnShareInter.setOnClickListener(v -> {


            String appLink = "https://play.google.com/store/apps/details?id=com.softylogics.dreamai";
            String appName = "DreamAI";

            String shareText = "Check out this dream I had:\n\n" +
                    "Dream: " + viewModel.getSelectedItem().getValue().getPrompt() + "\n" +
                    "Interpretation: " + viewModel.getSelectedItem().getValue().getInterpretation() + "\n\n" +
                    "Download " + appName + " app to explore more dreams:\n" + appLink;

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared Dream");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

            startActivity(Intent.createChooser(shareIntent, "Share via"));


        });

        binding.btnMic.setOnClickListener(v -> {
            checkSpeechToTextPermission();
        });
// Get a reference to the root view of your fragment layout


        // Add a global layout listener to detect changes in the layout
//        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                Rect r = new Rect();
//                binding.getRoot().getWindowVisibleDisplayFrame(r);
//                int screenHeight = binding.getRoot().getRootView().getHeight();
//                Log.d("variables" , "screen height "+screenHeight);
//                // Calculate the height difference between the visible display frame and the root view's height
//                int heightDifference = screenHeight - (r.bottom - r.top);
//                Log.d("variables" , "r "+(r.bottom - r.top));
//
//                Log.d("variables" , "height diff "+heightDifference);
//
//                // Check if the height difference is greater than a threshold to determine if the soft keyboard is visible
//                boolean isKeyboardVisible = heightDifference > (screenHeight * 0.15) && heightDifference > dpToPx(200); // Adjust the threshold as needed
//                Log.d("variables" , "keyb visible "+isKeyboardVisible);
//
//                // Adjust the position of the TextInputLayout based on the soft keyboard visibility
//                if (isKeyboardVisible) {
//                    // Soft keyboard is visible, move the TextInputLayout up
//                    int translationY =  heightDifference - binding.dreamInput.getTop() + binding.dreamInput.getHeight();
//                    Log.d("variables" , "binding.dreamInput.getTop() "+binding.dreamInput.getTop());
//
//                    Log.d("variables" , "translation y "+translationY);
//
//                    binding.dreamInput.setTranslationY(translationY);
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                    params.setMargins(0,0,0,0);
//                    binding.txtBkwas.setLayoutParams(params);
//
//                } else {
//                    // Soft keyboard is not visible, reset the translation of the TextInputLayout
//                    binding.dreamInput.setTranslationY(0);
//                }
//            }
//        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
        if(account==null) {
            if (!UserPreferences.getBoolean(NEVER_REMIND_SIGNIN)) {
                showSignInReminderDialog();
            }
        }
    }

    private void checkSpeechToTextPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        } else {
            startSpeechToText();
        }
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your dream...");

        try {
            startActivityForResult(intent, REQUEST_SPEECH_TO_TEXT);
        } catch (ActivityNotFoundException e) {
            showToast("Speech recognition not supported on this device.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SPEECH_TO_TEXT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String dreamText = result.get(0);
                String alreadyText = binding.dream.getText().toString();
                binding.dream.setText(alreadyText + " " + dreamText);
            }
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


    private void implementBackButtonFunctionality() {
        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    requireActivity().finish();
                }

                // Navigate to home fragment


                doubleBackToExitPressedOnce = true;
                showToast("Press again to exit");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 3000); // Reset the double back press flag after 2 seconds


            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(requireActivity(), callback);

    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void animateWelcomeText() {
        String welcomeText = "Please enter your dream or describe it in detail, and I will interpret it for you. If you're experiencing difficulty or not sure what to write, here are some tips:\n\n" +
                "1) Try to recall specific details from your dream, such as people, places, or objects.\n\n" +
                "2) Describe any emotions or sensations you felt during the dream.\n\n" +
                "3) Include any recurring themes or symbols that stood out to you.\n\n" +
                "Remember, the more information you provide, the better I can interpret your dream accurately.\n\n" +
                "Please avoid typing random letters or unrelated content, as it may result in an inaccurate interpretation. Now, go ahead and share your dream, and I'll do my best to provide you with insights and meanings.\n\n";
        handler = new Handler();
        final int delay = 1; //milliseconds
        handler.postDelayed(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                if (i < welcomeText.length()) {
                    binding.txtAIWelcome.append("" + welcomeText.charAt(i));
                    i++;
                    handler.postDelayed(this, delay);
                } else {
                    binding.dreamInput.setEndIconActivated(true);
                    binding.btnMic.setEnabled(true);

                }
            }
        }, delay);


    }

    private void animateText(String interpretation) {
        handler = new Handler();
        final int delay = 2; //milliseconds
        binding.dreamInput.setEndIconActivated(false);
        binding.btnMic.setEnabled(false);
        handler.postDelayed(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                if (i < interpretation.length()) {
                    binding.txtResult.append("" + interpretation.charAt(i));
                    binding.wholeScrollView.post(() -> {
                        binding.wholeScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    });
                    i++;
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);
        binding.btnShareInter.setVisibility(View.VISIBLE);
        binding.dreamInput.setEndIconActivated(true);
        binding.btnMic.setEnabled(true);

    }

    private void animateTextInputLayout() {
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, 600); // Parameters: (fromXDelta, toXDelta, fromYDelta, toYDelta)
        translateAnimation.setDuration(200); // Animation duration in milliseconds
        translateAnimation.setFillAfter(true); // Keep the final position of the view after animation
        binding.dreamInput.startAnimation(translateAnimation);

    }

    private void animateResultTextView() {
        binding.txtResultLayout.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.expand);
        binding.txtResultLayout.startAnimation(animation);


    }

    private void animateWelcomeTextView() {
        binding.welcomeLayout.setVisibility(View.VISIBLE);
        Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.expand);
        binding.welcomeLayout.startAnimation(animation);
        binding.dreamInput.setEndIconActivated(false);
        binding.btnMic.setEnabled(false);
        animateWelcomeText();


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }


//    private class dreamOpenAI extends AsyncTask<String, Void, String> {
//
//
//        @Override
//        protected String doInBackground(String... strings) {
//
//            //   return completeText(strings[0]).toString();
////
////            OpenAiService service = new OpenAiService(token);
//            String promptPretext = "You are a dream interpreter AI. Please interpret the following dream for me:" +
//
//                    "Dream: " + strings[0] +
//
//                    "\nError Handling: While interpreting the dream, if you encounter the following scenarios, send back a polite warning to user to try again while briefly describing the problem.\n" +
//                    "1. If the input is gibberish or nonsensical" +
//                    "2. If the input contains explicit or inappropriate content" +
//                    "3. If the input includes offensive language or discriminatory terms";
////                    CompletionRequest completionRequest = CompletionRequest.builder()
////                    .model("text-davinci-003")
////                    .prompt(promptPretext)
////                    .echo(false)
////                    .user("testing")
////                    .n(1)
////                    .maxTokens(150)
////                    .build();
////            try {
////                String result = service.createCompletion(completionRequest).getChoices().get(0).getText();
////
////                return result.trim();
////            } catch (Exception ex) {
////                return ex.getMessage();
////            }
////            interpretation = callAPI(promptPretext);
//
//
//
//            return interpretation;
//
//        }
//
//
//        @Override
//        protected void onPostExecute(String interpretation) {
//            super.onPostExecute(interpretation);
//            if (interpretation != null) {
//                handleInterpretation(interpretation, date);
//            } else {
//                showToast("An error occurred, try again in a few minutes");
//                binding.progressBar.setVisibility(View.GONE);
//            }
//        }
//    }

    private void callAPI(String prompt) {

        JSONObject requestBody = new JSONObject();
        JSONArray messages = new JSONArray();
        JSONObject system = new JSONObject();
        JSONObject user = new JSONObject();
        try {
            system.put("role", "system");
            system.put("content" , "You are a dream interpreter, please reply in only english alphabets. Do not reply in gibberish or use any symbols in your reply. please ignore any nonsensical query and if the letters are gibberish or any explicit or other unlawful talk and send a warning to try again.");

            user.put("role", "user");
            user.put("content" , prompt);

            messages.put(system);
            messages.put(user);
            requestBody.put("temperature", 0.2);
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 150);

            requestBody.put("user", anonymousUserId);
            requestBody.put("n", 1);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, API_ENDPOINT, requestBody, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    Log.d("attach", isFragmentAttached +"");
                    String text = response.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim();
                    if(isFragmentAttached) {
                        handleInterpretation(text);
                    }
                    Log.e("API Response", response.toString());
//                    Toast.makeText(getActivity(),text,Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("attach", isFragmentAttached +"");
                if(isFragmentAttached){
                showToast("An error occurred, try again in a few minutes");
                binding.progressBar.setVisibility(View.GONE);
                binding.dreamInput.setEndIconVisible(true);
                binding.dreamInput.setEndIconActivated(true);
                binding.btnMic.setEnabled(true);
                error.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }
        };
        int timeoutMs = 25000; // 25 seconds timeout
        RetryPolicy policy = new DefaultRetryPolicy(timeoutMs, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        // Add the request to the RequestQueue
        MySingleton.getInstance(getActivity()).addToRequestQueue(request);


    }

//    private void callAPIGPT35(String prompt) {
//        responseAI = "";
//        JSONObject temp = new JSONObject();
//
//        JSONObject requestBody = new JSONObject();
//        try {
//            requestBody.put("temperature", 0.2);
//            requestBody.put("model", "gpt-3.5-turbo");
//            requestBody.put("prompt", prompt);
//            requestBody.put("max_tokens", 150);
//            requestBody.put("echo", false);
//            requestBody.put("user", anonymousUserId);
//            requestBody.put("n", 1);
//            requestBody.put("message", temp);
//
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                try {
//                    JSONArray choicesArray = response.getJSONArray("choices");
//
//                    JSONObject choiceObject = choicesArray.getJSONObject(0);
//                    String text = choiceObject.getString("text").trim();
//                    handleInterpretation(text);
//                    Log.e("API Response", response.toString());
////                    Toast.makeText(getActivity(),text,Toast.LENGTH_SHORT).show();
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                showToast("An error occurred, try again in a few minutes");
//                binding.progressBar.setVisibility(View.GONE);
//                binding.dreamInput.setEndIconVisible(true);
//                binding.dreamInput.setEndIconActivated(true);
//                binding.btnMic.setEnabled(true);
//                Log.e("API Error", error.toString());
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers = new HashMap<>();
//                headers.put("Authorization", "Bearer " + token);
//                headers.put("Content-Type", "application/json");
//                return headers;
//            }
//
//            @Override
//            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
//                return super.parseNetworkResponse(response);
//            }
//        };
//        int timeoutMs = 25000; // 25 seconds timeout
//        RetryPolicy policy = new DefaultRetryPolicy(timeoutMs, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
//        request.setRetryPolicy(policy);
//        // Add the request to the RequestQueue
//        MySingleton.getInstance(getActivity()).addToRequestQueue(request);
//
//
//    }

    public void handleInterpretation(String text) {
        Log.d("handleInter", "inMethod");
        binding.progressBar.setVisibility(View.GONE);
        binding.dreamInput.setEndIconVisible(true);
        binding.dreamInput.setEndIconActivated(true);
        binding.btnMic.setEnabled(true);
        DreamItem item = new DreamItem(capitalizeFirstLetter(binding.dream.getText().toString()), text, System.currentTimeMillis(), false, false);
        viewModel.setDreamItem(item);
        if (!UserPreferences.getBoolean(Constants.IS_PURCHASED)) {
            showInterstitial();
        } else {
            if (account != null) {
            addDream(viewModel.getSelectedItem().getValue(), account);
            }
            showInterpretation(viewModel.getSelectedItem().getValue().getInterpretation());
        }
    }

    private void showInterpretation(String text) {
        if (binding.txtResultLayout.getVisibility() != View.VISIBLE) {
            animateResultTextView();
        }
        binding.txtResult.setText("");
        binding.txtPrompt.setText(viewModel.getSelectedItem().getValue().getPrompt());
        binding.dream.setText("");
        animateText(text);
    }

    public String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private void showdialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Upgrade to Premium");
        builder.setMessage("Thank you for watching an ad. Did you know that by purchasing our premium version, you can enjoy an ad-free experience?");
        builder.setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (activeFragment != Constants.MORE_FRAGMENT) {
                    activeFragment = Constants.MORE_FRAGMENT;
                    navController.navigate(R.id.action_navigation_journal_to_navigation_more);
                }
            }
        });
        builder.setNeutralButton("Remind me Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showInterpretation(viewModel.getSelectedItem().getValue().getInterpretation());
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Never Remind", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                UserPreferences.setBoolean(Constants.NEVER_REMIND_SUBSCRIPTION, true);
                showInterpretation(viewModel.getSelectedItem().getValue().getInterpretation());
                dialog.dismiss();

            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showSignInReminderDialog() {

        // Create the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Don't Miss Out!")
                .setMessage("Sign in to save your dream history and access it from any device.")
                .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (activeFragment != Constants.MORE_FRAGMENT) {
                            activeFragment = Constants.MORE_FRAGMENT;
                            navController.navigate(R.id.action_navigation_journal_to_navigation_more);
                        }
                    }
                })
                .setNegativeButton("Never Remind", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserPreferences.setBoolean(Constants.NEVER_REMIND_SIGNIN, true);
                        dialog.dismiss();
                    }
                });

        // Show the alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();    }

    public void loadAd() {
        if(!requestingAd) {
            requestingAd = true;
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(
                    requireActivity(),
                    AD_UNIT_ID,
                    adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialad) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.
                            requestingAd = false;
                            interstitialAd = interstitialad;

                            Log.i(TAG_AD, "onAdLoaded");
                            interstitialAd.setFullScreenContentCallback(
                                    new FullScreenContentCallback() {
                                        @Override
                                        public void onAdDismissedFullScreenContent() {
                                            // Called when fullscreen content is dismissed.
                                            // Make sure to set your reference to null so you don't
                                            // show it a second time.
                                            interstitialAd = null;
                                            Log.d(TAG_AD, "The ad was dismissed.");
                                            if (account != null) {
                                                Log.d("addDream", "loadADDismissed");
                                                addDream(viewModel.getSelectedItem().getValue(), account);
                                            }
                                            if (dreamCounter == 0) {
                                                if (!UserPreferences.getBoolean(Constants.NEVER_REMIND_SUBSCRIPTION)) {
                                                    showdialog();
                                                }


                                            } else {
                                                showInterpretation(viewModel.getSelectedItem().getValue().getInterpretation());

                                            }
                                            Log.d("counter", dreamCounter+"");
                                            Log.d("counter", UserPreferences.getBoolean(Constants.NEVER_REMIND_SUBSCRIPTION)+"");
                                            dreamCounter++;

                                            if (dreamCounter == 3) {
                                                dreamCounter = 0;
                                            }
                                            loadAd();

                                        }

                                        @Override
                                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                                            // Called when fullscreen content failed to show.
                                            // Make sure to set your reference to null so you don't
                                            // show it a second time.
                                            interstitialAd = null;
                                            Log.d(TAG_AD, "The ad failed to show.");
                                            if (account != null) {
                                                Log.d("addDream", "loadADFailedShow");
                                                addDream(viewModel.getSelectedItem().getValue(), account);
                                                //
                                            }

                                            showInterpretation(viewModel.getSelectedItem().getValue().getInterpretation());
                                            loadAd();

                                        }

                                        @Override
                                        public void onAdShowedFullScreenContent() {
                                            // Called when fullscreen content is shown.
                                            Log.d(TAG_AD, "The ad was shown.");
                                        }
                                    });
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.i(TAG_AD, loadAdError.getMessage());
                            interstitialAd = null;
                            requestingAd = false;

//                        String error = String.format("domain: %s, code: %d, message: %s", loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
//                        Toast.makeText(getContext(), "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT).show();
                            loadAd();

                        }
                    });
        }
    }

    private void showInterstitial() {
            if (interstitialAd != null) {
                interstitialAd.show(getActivity());
            } else {
                if (account != null) {
                    addDream(viewModel.getSelectedItem().getValue(), account);
                }
                showInterpretation(viewModel.getSelectedItem().getValue().getInterpretation());
            }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!UserPreferences.getBoolean(Constants.IS_PURCHASED)) {

                loadAd();

        }
        if (updateApp != null) {
            updateApp.resumeUpdate();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!UserPreferences.getBoolean(Constants.IS_PURCHASED)) {

                loadAd();

        }
        if (account != null) {
            Picasso.with(getContext()).load(account.getPhotoUrl()).into(binding.ivProfilePic);
        }
        updateApp = new UpdateApp(getContext(), getActivity());
        updateApp.updateApp();
    }

    private void addDream(DreamItem item, GoogleSignInAccount account) {


        Log.d("addDream", "inAdd");
        DreamItem itemEncrypted;
        try {
            itemEncrypted = new DreamItem(AESUtils.encrypt(item.getPrompt()), AESUtils.encrypt(item.getInterpretation()), item.getDate(), item.getFavorite(), item.getIsChecked());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
//        DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        DatabaseReference myRef = FirebaseDatabase.getInstance("https://dreamai-35520-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child(account.getId());
        myRef.child(String.valueOf(item.getDate())).setValue(itemEncrypted).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("firebase", "dreamAdded");
                } else {
                    Log.d("firebase", "dreamNotAdded " + task.getException().toString());
                }
            }
        });
        Log.d("handleInter", "inAdd");

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
        Log.d("attach", isFragmentAttached +"");
        isFragmentAttached = false;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }


    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d("attach", isFragmentAttached +"");
        isFragmentAttached = true;
    }


    @Override
    public void onDetach() {

        super.onDetach();
    }
}