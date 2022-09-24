package com.onedream.meituanhook.image

import android.os.Environment
import android.util.Log
import com.onedream.meituanhook.HookApp

object ImageConfigureHelper {
    fun getResultPicturePath(): String {
        val resultPicturePath = HookApp.mBaseContext.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)!!.path + "/result.jpg"
        Log.e("ATU", "指定的识别结果图片路径为$resultPicturePath")
        return resultPicturePath
    }

    fun getButtonPicturePath(): String {
        val buttonPicturePath = Environment.getExternalStorageDirectory().path + "/Pictures/button.jpg"
        Log.e("ATU", "指定的按钮图片路径为$buttonPicturePath")
        return buttonPicturePath
    }

    fun getCurrentScreenPicturePath(): String {
        val currentScreenPicturePath = HookApp.mBaseContext.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)!!.path + "/current_screen.png"
        Log.e("ATU", "指定的当前屏幕图片路径为$currentScreenPicturePath")
        return currentScreenPicturePath
    }
}