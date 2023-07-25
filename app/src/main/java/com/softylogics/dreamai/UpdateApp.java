package com.softylogics.dreamai;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;


import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;


public class UpdateApp {
    Context context;
    public static final int RC_APP_UPDATE = 11;

    Activity activty;
    AppUpdateManager appUpdateManager;
    Task<AppUpdateInfo> appUpdateInfoTask;

    public UpdateApp(Context context, Activity activity) {
        this.activty = activity;
        this.context = context;
        appUpdateManager  = AppUpdateManagerFactory.create(context);
        appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

    }

    void updateApp(){
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // For a flexible update, use AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo , AppUpdateType.IMMEDIATE , activty, RC_APP_UPDATE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    void resumeUpdate(){
        appUpdateManager
                .getAppUpdateInfo()
                .addOnSuccessListener(
                        appUpdateInfo -> {

                            if (appUpdateInfo.updateAvailability()
                                    == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                // If an in-app update is already running, resume the update.
                                try {
                                    appUpdateManager.startUpdateFlowForResult(
                                            appUpdateInfo,
                                            IMMEDIATE,
                                            activty,
                                            RC_APP_UPDATE);
                                } catch (IntentSender.SendIntentException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
    }

}
