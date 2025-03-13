#include <jni.h>
#include <string>
#include <sstream>
#include <cstdlib>
#include <android/log.h>

#define LOG_TAG "SYS-NATIVE"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

#define STRINGIFY(x) #x
#define TOSTRING(x) STRINGIFY(x)

#ifdef GOOGLE_API_KEY_HEX
const char* encodedApiKey = TOSTRING(GOOGLE_API_KEY_HEX);
#endif

std::string decodeHex(const std::string &hex) {
    std::string result;
    for (size_t i = 0; i < hex.length(); i += 2) {
        std::string byteString = hex.substr(i, 2);
        char byte = (char) strtol(byteString.c_str(), nullptr, 16);
        result.push_back(byte);
    }
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_branchdirectorymap_Secrets_00024NativeLib_getApiKey(JNIEnv *env, jclass clazz) {
    std::string hexStr(encodedApiKey);
    if (hexStr.size() >= 2 && hexStr.front() == '\"' && hexStr.back() == '\"') {
        hexStr = hexStr.substr(1, hexStr.size() - 2);
    }
    std::string apiKey = decodeHex(hexStr);
    return env->NewStringUTF(apiKey.c_str());
}