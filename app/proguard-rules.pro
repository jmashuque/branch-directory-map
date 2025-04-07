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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keepattributes Signature

-keep class com.scottyab.rootbeer.RootBeer {
    <init>(android.content.Context);
    boolean isRooted();
}

-keep class com.example.branchdirectorymap.Secrets {
    public static <methods>;
    public static class *;
}

-keep class com.example.branchdirectorymap.Secrets$NativeLib {
    public static native <methods>;
    static <clinit>();
}

-keep class androidx.security.crypto.EncryptedSharedPreferences {
    public static *** create(...);
}
-keep class androidx.security.crypto.EncryptedSharedPreferences$PrefKeyEncryptionScheme {
    public static androidx.security.crypto.EncryptedSharedPreferences$PrefKeyEncryptionScheme valueOf(java.lang.String);
}
-keep class androidx.security.crypto.EncryptedSharedPreferences$PrefValueEncryptionScheme {
    public static androidx.security.crypto.EncryptedSharedPreferences$PrefValueEncryptionScheme valueOf(java.lang.String);
}
-keep class androidx.security.crypto.MasterKey
-keep class androidx.security.crypto.MasterKey$Builder {
    public androidx.security.crypto.MasterKey$Builder setKeyScheme(androidx.security.crypto.MasterKey$KeyScheme);
    public <init>(android.content.Context);
    public androidx.security.crypto.MasterKey build();
}
-keep class androidx.security.crypto.MasterKey$KeyScheme {
    public static androidx.security.crypto.MasterKey$KeyScheme valueOf(java.lang.String);
}

-keep class java.security.KeyStore
-keep class java.security.KeyStore$SecretKeyEntry
-keep class javax.crypto.KeyGenerator
-keep class android.security.keystore.KeyGenParameterSpec$Builder
-keep class android.security.keystore.KeyProperties
-keep class javax.crypto.Cipher
-keep class javax.crypto.spec.GCMParameterSpec

-keep class com.google.firebase.remoteconfig.FirebaseRemoteConfig {
    public static com.google.firebase.remoteconfig.FirebaseRemoteConfig getInstance();
    public java.lang.String getString(java.lang.String);
    public com.google.android.gms.tasks.Task setConfigSettingsAsync(com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings);
    public com.google.android.gms.tasks.Task fetchAndActivate();
}
-keep class com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
-keep class com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings$Builder {
    public com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings$Builder setMinimumFetchIntervalInSeconds(long);
    public com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings build();
}

-keep class com.google.android.gms.tasks.OnCompleteListener
-keep class com.google.android.gms.tasks.Task {
    public <methods>;
}
-keepclassmembers class * implements com.google.android.gms.tasks.OnCompleteListener {
    public void onComplete(com.google.android.gms.tasks.Task);
}
-keepclassmembers class * {
    public com.google.android.gms.tasks.Task addOnCompleteListener(com.google.android.gms.tasks.OnCompleteListener);
}
-keep interface com.google.android.gms.tasks.OnCompleteListener {
    void onComplete(com.google.android.gms.tasks.Task);
}

-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**