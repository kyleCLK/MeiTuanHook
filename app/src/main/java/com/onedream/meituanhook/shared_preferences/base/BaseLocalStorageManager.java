package com.onedream.meituanhook.shared_preferences.base;

import android.util.Log;

import com.onedream.meituanhook.HookApp;
import com.tencent.mmkv.MMKV;

/**
 * 本地存储基类，在这里替换本地存储框架即可
 *
 * @author jdallen
 * @since 2021/3/17
 */
public class BaseLocalStorageManager {

    private static MMKV getDefaultMMKV() {
        MMKV mmkv;
        try {
            mmkv = MMKV.defaultMMKV();
        } catch (Exception e) {
            Log.e("ATU", "DeviceIdLocalStorageManager：MMKV获取默认实例异常" + e.toString());
            String root = MMKV.initialize(HookApp.mBaseContext);
            Log.e("ATU", "DeviceIdLocalStorageManager：MMKV默认实例异常后进行初始化，目录路径" + root);
            mmkv = MMKV.defaultMMKV();
        }
        return mmkv;
    }

    //String
    public static void putString(String key, String value) {
        getDefaultMMKV().encode(key, value == null ? "" : value);
    }

    public static String getString(String key, String defaultValue) {
        return getDefaultMMKV().decodeString(key, defaultValue);
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    //int
    public static void putInt(String key, int value) {
        getDefaultMMKV().encode(key, value);
    }

    public static int getInt(String key, int defaultValue) {
        return getDefaultMMKV().decodeInt(key, defaultValue);
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    //long
    public static void putLong(String key, long value) {
        getDefaultMMKV().encode(key, value);
    }

    public static long getLong(String key, long defaultValue) {
        return getDefaultMMKV().decodeLong(key, defaultValue);
    }

    public static long getLong(String key) {
        return getLong(key, 0);
    }

    //float
    public static void putFloat(String key, float value) {
        getDefaultMMKV().encode(key, value);
    }

    public static float getFloat(String key, float defaultValue) {
        return getDefaultMMKV().decodeFloat(key, defaultValue);
    }

    public static float getFloat(String key) {
        return getFloat(key, 0.0f);
    }

    //double
    public static void putDouble(String key, double value) {
        getDefaultMMKV().encode(key, value);
    }

    public static double getDouble(String key, double defaultValue) {
        return getDefaultMMKV().decodeDouble(key, defaultValue);
    }

    public static double getDouble(String key) {
        return getDouble(key, 0.0D);
    }

    //boolean
    public static void putBoolean(String key, boolean value) {
        getDefaultMMKV().encode(key, value);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return getDefaultMMKV().decodeBool(key, defaultValue);
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }
}
