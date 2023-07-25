package com.softylogics.dreamai;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport;
import com.google.firebase.firestore.auth.User;
import com.softylogics.dreamai.databinding.ActivityMainBinding;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    // todo: change progress bar





    public static int dreamCounter = 0;
    private static final String TAG = "In-App Updates";
    private AppBarConfiguration mAppBarConfiguration;
    private BillingClientHelper billingClientHelper;
    private ActivityMainBinding binding;

    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAnalytics mFirebaseAnalytics;
    public static GoogleSignInAccount account;

    public static int activeFragment = 0;
    public static String anonymousUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
//        UUID uuid = UUID.randomUUID();
//        // Convert UUID to string
//        anonymousUserId = uuid.toString();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        UserPreferences.init(this);
        setContentView(binding.getRoot());
//        setSupportActionBar(binding);

//        binding.appBarMain.toolbar.setNavigationIcon(R.drawable.baseline_menu_24);
//        binding.appBarMain.toolbar.setCollapseIcon(R.drawable.baseline_menu_24);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_journal, R.id.navigation_history, R.id.navigation_more)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        MyTask myTask = new MyTask();

        // Execute the background task
        myTask.execute();
        NavOptions navOptions = new NavOptions.Builder()
                .setEnterAnim(R.anim.slide_in)
                .setExitAnim(R.anim.slide_out)
                .setPopEnterAnim(R.anim.fade_in)
                .setPopExitAnim(R.anim.fade_out)
                .build();
        binding.bottomNav.setOnNavigationItemSelectedListener(item ->{
            if(item.getItemId() == R.id.navigation_journal){
                if(activeFragment!=Constants.JOURNAL_FRAGMENT) {
                    activeFragment = Constants.JOURNAL_FRAGMENT;
                    navController.navigate(R.id.navigation_journal, null, navOptions);

                }
            }
            else if(item.getItemId()==R.id.navigation_history){
                if(activeFragment!=Constants.HISTORY_FRAGMENT) {
                    activeFragment = Constants.HISTORY_FRAGMENT;
                    navController.navigate(R.id.navigation_history, null, navOptions);
                }
            }
            else if(item.getItemId()==R.id.navigation_more){
                if(activeFragment!=Constants.MORE_FRAGMENT) {
                    activeFragment = Constants.MORE_FRAGMENT;
                    navController.navigate(R.id.navigation_more, null, navOptions);
                }
            }
            return true;
        });


        account = isUserSignedIn();
        billingClientHelper = new BillingClientHelper(this);
        if(!UserPreferences.getBoolean(Constants.IS_PURCHASED)) {
            if (billingClientHelper.restoreSubscriptions()) {
                UserPreferences.setBoolean(Constants.IS_PURCHASED, true);
                showToast("Congratulations! Your subscription has been successfully restored. You can now enjoy all the benefits of our premium plan.");
            }
        }
    }

    private void signOut() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        updateUI();
                        showToast("You are successfully signed out");

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.nav_host_fragment_content_main, new HistoryDetailFragment())
                                .commit();




                    } else {
                        showToast(task.getException().getMessage().toString());
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_logoff) {
            signOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }








    /**
     * Registers the install state listener.
     */
    @Override
    protected void onResume() {
        super.onResume();



    }

    /**
     * Unregisters the install state listener.
     */
    @Override
    protected void onPause() {
        super.onPause();


    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }




    private void updateUI() {

//        View headerView = binding.navView.getHeaderView(0);
//        NavHeaderMainBinding headerBinding = NavHeaderMainBinding.bind(headerView);
//        headerBinding.username.setText("Guest User");
//
//        Picasso.with(this).load(R.drawable.ic_account_circle_black_24dp).into(headerBinding.avatar);
    }
    private GoogleSignInAccount isUserSignedIn() {

        return GoogleSignIn.getLastSignedInAccount(this);

    }

    public class MyTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return mFirebaseAnalytics.getFirebaseInstanceId();
        }

        @Override
        protected void onPostExecute(String token) {
            anonymousUserId = token;
        }
    }

}
