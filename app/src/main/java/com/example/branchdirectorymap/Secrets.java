package com.example.branchdirectorymap;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.lang.reflect.Method;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.Arrays;

public class Secrets {

    private static final String TAG = "SYS-SECRETS";
    private static final BranchDirectoryMap app = BranchDirectoryMap.getInstance();

    private static SecretKey getAESKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(app.ANDROID_KEYSTORE);
        keyStore.load(null);

        if (keyStore.containsAlias(app.KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(app.KEY_ALIAS, null)).getSecretKey();
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, app.ANDROID_KEYSTORE);
        keyGenerator.init(new KeyGenParameterSpec.Builder(
                app.KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build());

        return keyGenerator.generateKey();
    }

    public interface OnApiKeyReceivedListener {
        void onApiKeyReceived(boolean keyReceived);
    }

    public static void fetchGeocodeApiKey(Context context, OnApiKeyReceivedListener listener) {
        try {
            SecretKey aesKey = getAESKey();

            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    context,
                    app.SECURE_PREFS,
                    new MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            if (BuildConfig.USE_FIREBASE) {
                try {
                    Class<?> firebaseRCClass = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfig");
                    Method getInstanceMethod = firebaseRCClass.getMethod("getInstance");
                    Object firebaseRCInstance = getInstanceMethod.invoke(null);

                    Class<?> firebaseRCSettingsClass = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings");
                    Class<?> builderClass = Class.forName("com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings$Builder");
                    Object builderInstance = builderClass.getConstructor().newInstance();
                    Method setIntervalMethod = builderClass.getMethod("setMinimumFetchIntervalInSeconds", long.class);
                    setIntervalMethod.invoke(builderInstance, 3600L);
                    Method buildMethod = builderClass.getMethod("build");
                    Object configSettings = buildMethod.invoke(builderInstance);

                    Method setConfigSettingsAsyncMethod = firebaseRCClass.getMethod("setConfigSettingsAsync", firebaseRCSettingsClass);
                    setConfigSettingsAsyncMethod.invoke(firebaseRCInstance, configSettings);

                    Handler timeoutHandler = new Handler(Looper.getMainLooper());
                    Runnable timeoutRunnable = () -> {
                        Log.e(TAG, "Timeout: Failed to fetch API key within " + BuildConfig.TIMEOUT_S + " seconds");
                        listener.onApiKeyReceived(false);
                    };
                    timeoutHandler.postDelayed(timeoutRunnable, BuildConfig.TIMEOUT_S * 1000);

                    Method fetchAndActivateMethod = firebaseRCClass.getMethod("fetchAndActivate");
                    Object fetchTask = fetchAndActivateMethod.invoke(firebaseRCInstance);

                    Class<?> onCompleteListenerClass = Class.forName("com.google.android.gms.tasks.OnCompleteListener");
                    Object onCompleteListener = java.lang.reflect.Proxy.newProxyInstance(
                            onCompleteListenerClass.getClassLoader(),
                            new Class[]{onCompleteListenerClass},
                            (proxy, method, args) -> {
                                if ("onComplete".equals(method.getName())) {
                                    timeoutHandler.removeCallbacks(timeoutRunnable);
                                    Object task = args[0];
                                    Method isSuccessfulMethod = task.getClass().getMethod("isSuccessful");
                                    boolean isSuccessful = (boolean) isSuccessfulMethod.invoke(task);
                                    if (isSuccessful) {
                                        Method getStringMethod = firebaseRCClass.getMethod("getString", String.class);
                                        String apiKey = (String) getStringMethod.invoke(firebaseRCInstance, app.KEY_APIKEY);
                                        if (apiKey != null && !apiKey.isEmpty()) {
                                            try {
                                                byte[] encryptedData = encrypt(apiKey, aesKey);
                                                sharedPreferences.edit().putString(app.KEY_APIKEY,
                                                        Base64.encodeToString(encryptedData, Base64.DEFAULT)).apply();
                                            } catch (Exception e) {
                                                e.printStackTrace(); // FIX THIS: add logging
                                            } finally {
                                                Log.i(TAG, "Successfully fetched and stored API key from Firebase");
                                                listener.onApiKeyReceived(true);
                                            }
                                        }
                                    } else {
                                        Log.e(TAG, "Firebase API key fetch failed");
                                        listener.onApiKeyReceived(false);
                                    }
                                }
                                return null;
                            }
                    );

                    Method addOnCompleteListenerMethod = fetchTask.getClass().getMethod("addOnCompleteListener", onCompleteListenerClass);
                    addOnCompleteListenerMethod.invoke(fetchTask, onCompleteListener);

                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onApiKeyReceived(false);
                }
            } else {
                String apiKey = NativeLib.getApiKey();
                if (apiKey != null && !apiKey.isEmpty()) {
                    byte[] encryptedData = encrypt(apiKey, aesKey);
                    sharedPreferences.edit().putString(app.KEY_APIKEY,
                            Base64.encodeToString(encryptedData, Base64.DEFAULT)).apply();
                    Log.i(TAG, "Successfully fetched and stored API key from NativeLib");
                    listener.onApiKeyReceived(true);
                } else {
                    Log.e(TAG, "NativeLib returned an empty API key");
                    listener.onApiKeyReceived(false);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch or store Geocode API Key", e);
        }
    }

    public static String getStoredGeocodeApiKey(Context context) {
        String decryptedApiKey = null;
        try {
            SecretKey aesKey = getAESKey();

            EncryptedSharedPreferences sharedPreferences = (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    context,
                    app.SECURE_PREFS,
                    new MasterKey.Builder(context)
                            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                            .build(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String encryptedApiKey = sharedPreferences.getString(app.KEY_APIKEY, null);
            if (encryptedApiKey != null) {
                byte[] encryptedData = Base64.decode(encryptedApiKey, Base64.DEFAULT);
                decryptedApiKey = decrypt(encryptedData, aesKey);
            }

            return decryptedApiKey;

        } catch (Exception e) {
            e.printStackTrace(); // FIX THIS: add logging
            return null;
        } finally {
            decryptedApiKey = null;
        }
    }

    private static byte[] encrypt(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(app.CIPHER_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] iv = cipher.getIV();
        byte[] encryptedData = cipher.doFinal(data.getBytes("UTF-8"));

        byte[] combined = new byte[app.IV_SIZE + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, app.IV_SIZE);
        System.arraycopy(encryptedData, 0, combined, app.IV_SIZE, encryptedData.length);

        return combined;
    }

    private static String decrypt(byte[] encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(app.CIPHER_TRANSFORMATION);

        byte[] iv = Arrays.copyOfRange(encryptedData, 0, app.IV_SIZE);
        byte[] cipherText = Arrays.copyOfRange(encryptedData, app.IV_SIZE, encryptedData.length);

        GCMParameterSpec spec = new GCMParameterSpec(app.GCM_TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decryptedData = cipher.doFinal(cipherText);
        String decryptedString = new String(decryptedData, "UTF-8");

        return decryptedString;
    }

    public class NativeLib {

        static {
            if (!BuildConfig.USE_FIREBASE) {
                System.loadLibrary("native-lib");
            }
        }

        public static native String getApiKey();
    }
}