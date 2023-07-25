# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# Retrofit
-keepattributes Signature
-keepattributes Exceptions

# Retrofit specific classes
-dontwarn retrofit.*
-keep class retrofit.* { *; }
-keep class okhttp3.* { *; }
-keep class okio.* { *; }
-keep class com.google.gson.* { *; }
-keep class retrofitRxJava2.* { *; }
-keep class retrofitJackson.* { *; }
-keep class jacksonJsonSchema.* { *; }


# Volley
-keep class com.android.volley.** { *; }
-dontwarn com.android.volley.**


#-dontwarn com.squareup.okhttp.Cache
#-dontwarn com.squareup.okhttp.CacheControl$Builder
#-dontwarn com.squareup.okhttp.CacheControl
#-dontwarn com.squareup.okhttp.Call
#-dontwarn com.squareup.okhttp.OkHttpClient
#-dontwarn com.squareup.okhttp.Request$Builder
#-dontwarn com.squareup.okhttp.Request
#-dontwarn com.squareup.okhttp.Response
#-dontwarn com.squareup.okhttp.ResponseBody
#-dontwarn java.beans.ConstructorProperties
#-dontwarn java.beans.Transient