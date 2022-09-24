package com.onedream.meituanhook

import android.app.Application
import android.content.Context
import com.onedream.meituanhook.shared_preferences.base.MMKVInitManager

class HookApp : Application() {

    override fun onCreate() {
        super.onCreate()
        mBaseContext = this
        MMKVInitManager.initMMKV(this)
    }

    companion object{
        lateinit var mBaseContext :Context
    }
}