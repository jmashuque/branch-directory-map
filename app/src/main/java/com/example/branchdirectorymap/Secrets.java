package com.example.branchdirectorymap;

import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Secrets {

    private static final String TAG = "SYS-SECRETS";

    public static class EncryptedSharedPreferencesReflection {

        private static Object createEncryptedSharedPreferences(Context context, String prefsName) throws Exception {
            Class<?> espClass = Class.forName("androidx.security.crypto.EncryptedSharedPreferences");
            Class<?> masterKeyClass = Class.forName("androidx.security.crypto.MasterKey");
            Class<?> builderClass = Class.forName("androidx.security.crypto.MasterKey$Builder");

            Constructor<?> builderCtor = builderClass.getConstructor(Context.class);
            Object builderInstance = builderCtor.newInstance(context);

            Class<?> keySchemeEnumClass = Class.forName("androidx.security.crypto.MasterKey$KeyScheme");
            Method keySchemeValueOfMethod = keySchemeEnumClass.getMethod("valueOf", String.class);
            Object aes256GcmValue = keySchemeValueOfMethod.invoke(null, "AES256_GCM");

            Method setKeySchemeMethod = builderClass.getMethod("setKeyScheme", keySchemeEnumClass);
            setKeySchemeMethod.invoke(builderInstance, aes256GcmValue);

            Method buildMethod = builderClass.getMethod("build");
            Object masterKey = buildMethod.invoke(builderInstance);

            Class<?> espKeySchemeClass = Class.forName("androidx.security.crypto.EncryptedSharedPreferences$PrefKeyEncryptionScheme");
            Method espKeySchemeValueOfMethod = espKeySchemeClass.getMethod("valueOf", String.class);
            Object keySchemeEnum = espKeySchemeValueOfMethod.invoke(null, "AES256_SIV");

            Class<?> espValueSchemeClass = Class.forName("androidx.security.crypto.EncryptedSharedPreferences$PrefValueEncryptionScheme");
            Method espValueSchemeValueOfMethod = espValueSchemeClass.getMethod("valueOf", String.class);
            Object valueSchemeEnum = espValueSchemeValueOfMethod.invoke(null, "AES256_GCM");

            Method espCreate = espClass.getMethod("create", Context.class, String.class, masterKeyClass, espKeySchemeClass, espValueSchemeClass);
            return espCreate.invoke(null, context, prefsName, masterKey, keySchemeEnum, valueSchemeEnum);
        }
    }

    private static Object getAESKey() throws Exception {
        Class<?> keyStoreClass = Class.forName("java.security.KeyStore");
        Method getInstanceMethod = keyStoreClass.getMethod("getInstance", String.class);
        Object keyStore = getInstanceMethod.invoke(null, BranchDirectoryMap.ANDROID_KEYSTORE);

        Method loadMethod = keyStoreClass.getMethod("load", java.security.KeyStore.LoadStoreParameter.class);
        loadMethod.invoke(keyStore, (java.security.KeyStore.LoadStoreParameter) null);

        Method containsAliasMethod = keyStoreClass.getMethod("containsAlias", String.class);
        boolean containsAlias = (boolean) containsAliasMethod.invoke(keyStore, BranchDirectoryMap.KEY_ALIAS);

        if (containsAlias) {
            Class<?> secretKeyEntryClass = Class.forName("java.security.KeyStore$SecretKeyEntry");
            Method getEntryMethod = keyStoreClass.getMethod("getEntry", String.class, java.security.KeyStore.ProtectionParameter.class);
            Object secretKeyEntry = getEntryMethod.invoke(keyStore, BranchDirectoryMap.KEY_ALIAS, null);
            Method getSecretKeyMethod = secretKeyEntryClass.getMethod("getSecretKey");
            return getSecretKeyMethod.invoke(secretKeyEntry);
        }

        Class<?> keyGeneratorClass = Class.forName("javax.crypto.KeyGenerator");
        Method keyGeneratorGetInstanceMethod = keyGeneratorClass.getMethod("getInstance", String.class, String.class);
        Object keyGenerator = keyGeneratorGetInstanceMethod.invoke(null, "AES", BranchDirectoryMap.ANDROID_KEYSTORE);

        Class<?> keyGenParameterSpecClass = Class.forName("android.security.keystore.KeyGenParameterSpec$Builder");
        Constructor<?> keyGenParameterSpecBuilderCtor = keyGenParameterSpecClass.getConstructor(String.class, int.class);
        Object keyGenParameterSpecBuilder = keyGenParameterSpecBuilderCtor.newInstance(BranchDirectoryMap.KEY_ALIAS, 3); // PURPOSE_ENCRYPT | PURPOSE_DECRYPT

        Class<?> keyPropertiesClass = Class.forName("android.security.keystore.KeyProperties");
        Field blockModeGCMField = keyPropertiesClass.getField("BLOCK_MODE_GCM");
        Object blockModeGCM = blockModeGCMField.get(null);
        Field encryptionPaddingNoneField = keyPropertiesClass.getField("ENCRYPTION_PADDING_NONE");
        Object encryptionPaddingNone = encryptionPaddingNoneField.get(null);

        Method setBlockModesMethod = keyGenParameterSpecClass.getMethod("setBlockModes", String[].class);
        setBlockModesMethod.invoke(keyGenParameterSpecBuilder, (Object) new String[]{blockModeGCM.toString()});
        Method setEncryptionPaddingsMethod = keyGenParameterSpecClass.getMethod("setEncryptionPaddings", String[].class);
        setEncryptionPaddingsMethod.invoke(keyGenParameterSpecBuilder, (Object) new String[]{encryptionPaddingNone.toString()});
        Method setKeySizeMethod = keyGenParameterSpecClass.getMethod("setKeySize", int.class);
        setKeySizeMethod.invoke(keyGenParameterSpecBuilder, 256);
        Method buildMethod = keyGenParameterSpecClass.getMethod("build");
        Object keyGenParameterSpec = buildMethod.invoke(keyGenParameterSpecBuilder);

        Method initMethod = keyGeneratorClass.getMethod("init", java.security.spec.AlgorithmParameterSpec.class);
        initMethod.invoke(keyGenerator, keyGenParameterSpec);

        Method generateKeyMethod = keyGeneratorClass.getMethod("generateKey");
        return generateKeyMethod.invoke(keyGenerator);
    }

    public interface OnApiKeyReceivedListener {
        void onApiKeyReceived(boolean keyReceived);
    }

    public static void fetchGeocodeApiKey(Context context, OnApiKeyReceivedListener listener) {
        if (!BuildConfig.ALLOW_ROOT) {
            Boolean isTerminate = isRootOrDebug(context);
            if (isTerminate) {
                Log.e(TAG, "Root check failed");
                listener.onApiKeyReceived(false);
                return;
            } else if (!isTerminate) {
                Log.i(TAG, "Root check passed");
            } else {
                Log.e(TAG, "Root check error at isRootOrDebug");
                listener.onApiKeyReceived(false);
                return;
            }
        }
        try {
            Object aesKey = getAESKey();

            Object sharedPreferences = EncryptedSharedPreferencesReflection.createEncryptedSharedPreferences(context, BranchDirectoryMap.SECURE_PREFS);

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
                                        String apiKey = (String) getStringMethod.invoke(firebaseRCInstance, BranchDirectoryMap.KEY_APIKEY);
                                        if (apiKey != null && !apiKey.isEmpty()) {
                                            try {
                                                byte[] encryptedData = encrypt(apiKey, aesKey);

                                                Method editMethod = sharedPreferences.getClass().getMethod("edit");
                                                Object editor = editMethod.invoke(sharedPreferences);

                                                Method putStringMethod = editor.getClass().getMethod("putString", String.class, String.class);
                                                putStringMethod.invoke(editor, BranchDirectoryMap.KEY_APIKEY, Base64.encodeToString(encryptedData, Base64.DEFAULT));

                                                Method applyMethod = editor.getClass().getMethod("apply");
                                                applyMethod.invoke(editor);

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
                    Method editMethod = sharedPreferences.getClass().getMethod("edit");
                    Object editor = editMethod.invoke(sharedPreferences);

                    Method putStringMethod = editor.getClass().getMethod("putString", String.class, String.class);
                    putStringMethod.invoke(editor, BranchDirectoryMap.KEY_APIKEY, Base64.encodeToString(encryptedData, Base64.DEFAULT));

                    Method applyMethod = editor.getClass().getMethod("apply");
                    applyMethod.invoke(editor);

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
        if (!BuildConfig.ALLOW_ROOT) {
            Boolean isTerminate = isRootOrDebug(context);
            if (isTerminate) {
                Log.e(TAG, "Root check failed");
                return null;
            } else if (!isTerminate) {
                Log.i(TAG, "Root check passed");
            } else {
                Log.e(TAG, "Root check error at isRootOrDebug");
                return null;
            }
        }
        String decryptedApiKey = null;
        try {
            Object aesKey = getAESKey();

            Object sharedPreferences = EncryptedSharedPreferencesReflection.createEncryptedSharedPreferences(context, BranchDirectoryMap.SECURE_PREFS);

            Method getStringMethod = sharedPreferences.getClass().getMethod("getString", String.class, String.class);
            String encryptedApiKey = (String) getStringMethod.invoke(sharedPreferences, BranchDirectoryMap.KEY_APIKEY, null);

            if (encryptedApiKey != null) {
                byte[] encryptedData = Base64.decode(encryptedApiKey, Base64.DEFAULT);
                decryptedApiKey = decrypt(encryptedData, aesKey);
            }

            return decryptedApiKey;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            decryptedApiKey = null;
        }
    }

    private static byte[] encrypt(String data, Object secretKey) throws Exception {
        Class<?> cipherClass = Class.forName("javax.crypto.Cipher");
        Method getInstanceMethod = cipherClass.getMethod("getInstance", String.class);
        Object cipher = getInstanceMethod.invoke(null, BranchDirectoryMap.CIPHER_TRANSFORMATION);

        Method initMethod = cipherClass.getMethod("init", int.class, java.security.Key.class);
        initMethod.invoke(cipher, 1, secretKey); // Cipher.ENCRYPT_MODE

        Method getIVMethod = cipherClass.getMethod("getIV");
        byte[] iv = (byte[]) getIVMethod.invoke(cipher);

        Method doFinalMethod = cipherClass.getMethod("doFinal", byte[].class);
        byte[] encryptedData = (byte[]) doFinalMethod.invoke(cipher, data.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[BranchDirectoryMap.IV_SIZE + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, BranchDirectoryMap.IV_SIZE);
        System.arraycopy(encryptedData, 0, combined, BranchDirectoryMap.IV_SIZE, encryptedData.length);

        return combined;
    }

    private static String decrypt(byte[] encryptedData, Object secretKey) throws Exception {
        Class<?> cipherClass = Class.forName("javax.crypto.Cipher");
        Method getInstanceMethod = cipherClass.getMethod("getInstance", String.class);
        Object cipher = getInstanceMethod.invoke(null, BranchDirectoryMap.CIPHER_TRANSFORMATION);

        byte[] iv = Arrays.copyOfRange(encryptedData, 0, BranchDirectoryMap.IV_SIZE);
        byte[] cipherText = Arrays.copyOfRange(encryptedData, BranchDirectoryMap.IV_SIZE, encryptedData.length);

        Class<?> gcmParameterSpecClass = Class.forName("javax.crypto.spec.GCMParameterSpec");
        Constructor<?> gcmParameterSpecConstructor = gcmParameterSpecClass.getConstructor(int.class, byte[].class);
        Object spec = gcmParameterSpecConstructor.newInstance(BranchDirectoryMap.GCM_TAG_SIZE, iv);

        Method initMethod = cipherClass.getMethod("init", int.class, java.security.Key.class, java.security.spec.AlgorithmParameterSpec.class);
        initMethod.invoke(cipher, 2, secretKey, spec); // Cipher.DECRYPT_MODE

        Method doFinalMethod = cipherClass.getMethod("doFinal", byte[].class);
        byte[] decryptedData = (byte[]) doFinalMethod.invoke(cipher, cipherText);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    // Boolean objects can be null as well
    public static @Nullable Boolean isRootOrDebug(Context context) {
        try {
            Class<?> rootBeerClass = Class.forName("com.scottyab.rootbeer.RootBeer");
            java.lang.reflect.Constructor<?> constructor = rootBeerClass.getConstructor(Context.class);
            Object rootBeerInstance = constructor.newInstance(context);
            java.lang.reflect.Method isRootedMethod = rootBeerClass.getMethod("isRooted");
            boolean isRooted = (Boolean) isRootedMethod.invoke(rootBeerInstance);
            return isRooted || Debug.isDebuggerConnected();
        } catch (Exception e) {
            Log.e(TAG, "Error checking for root: " + e);
            return null;
        }
    }

    public static class NativeLib {

        static {
            if (!BuildConfig.USE_FIREBASE) {
                System.loadLibrary("native-lib");
            }
        }

        public static native String getApiKey();
    }
}