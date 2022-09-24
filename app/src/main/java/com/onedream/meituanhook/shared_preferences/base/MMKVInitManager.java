package com.onedream.meituanhook.shared_preferences.base;

import android.app.Application;
import android.util.Log;

import com.tencent.mmkv.MMKV;

public class MMKVInitManager {

    //初始化MMKV
    public static void initMMKV(Application application) {
        String rootDir = MMKV.initialize(application);
        Log.e("ATU","MMKV的根目录为" + rootDir);
    }
}
