 package com.softylogics.dreamai;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import androidx.preference.PreferenceManager;

 public class UserPreferences {
     private static Context context = null;
     private static SharedPreferences prefs = null;
     private static Uri default_storage = null;

     public static void init(Context ctx) {
         context = ctx;
         prefs = PreferenceManager.getDefaultSharedPreferences(context);
         default_storage = Uri.fromFile(context.getFilesDir());
     }

     public static void setString(String key, String value) {
         SharedPreferences.Editor editor = prefs.edit()
             .putString(key, value);
         editor.apply();
     }
     public static void setDouble(String key, Double value) {
         SharedPreferences.Editor editor = prefs.edit()
                 .putLong(key, Double.doubleToLongBits(value));
         editor.apply();
     }
     public static double getDouble(String key){
         return Double.longBitsToDouble(prefs.getLong(key, 0));
     }
     public static String getString(String key){
         return prefs.getString(key, "");
     }
    public static boolean getBoolean(String key){
         return prefs.getBoolean(key, false);
    }

     public static void setBoolean(String key, boolean value) {
         SharedPreferences.Editor editor = prefs.edit()
             .putBoolean(key, value);
             editor.apply();
     }
     public static void setInt(String key, int value){
         SharedPreferences.Editor editor = prefs.edit()
                 .putInt(key, value);
         editor.apply();
     }
     public static int getint(String key){
         return prefs.getInt(key, 0);
     }

     public static Uri getStorageUri() {
         String str = prefs.getString("storage_location", null);
         return str == null ? default_storage : Uri.parse(str);
     }

     public static void setStorageUri(Uri uri) {
         setString("storage_location", uri.toString());
     }

     public static boolean getAutoMaticManual() {
         return prefs.getBoolean("automatic_recording", true);
     }
 public static String getFormatListPreference(){
         return prefs.getString("recording_format" ,"1");
 }
     public static String getAutoDelListPreference(){
         return prefs.getString("autodelperiod" ,"10");
     }
     public static String getBlocklistListPreference(){
         return prefs.getString("blocklist" ,"1");
     }
     public static String getSortingPreference(){
         return prefs.getString("sort" ,"1");
     }
//     public static void setEnabled(boolean enabled) {
//         setBoolean("enabled", enabled);
//
//         context.startService(new Intent(context, RecordService.class)
//             .putExtra("commandType", enabled ? Constants.RECORDING_DISABLED
//                 : Constants.RECORDING_ENABLED)
//             .putExtra("enabled", enabled));
//     }

     public static boolean getWelcomeSeen() {
         return prefs.getBoolean("welcome_seen", false);
     }

     public static void setWelcomeSeen() {
         setBoolean("welcome_seen", true);
     }

     public static SharedPreferences getPrefs() {
         return prefs;
     }

     public static void setPrefs(SharedPreferences prefs) {
         UserPreferences.prefs = prefs;
     }
 }
