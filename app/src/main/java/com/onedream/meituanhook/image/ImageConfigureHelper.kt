package com.onedream.meituanhook.image

import android.os.Environment
import android.util.Log

object ImageConfigureHelper {
    fun getResultPicturePath() :String{
        val resultPicturePath = Environment.getExternalStorageDirectory().path + "/Pictures/result.jpg"
        Log.e("ATU", "指定的识别结果图片路径为$resultPicturePath")
        return  resultPicturePath
    }

    fun getButtonPicturePath():String{
        val buttonPicturePath = Environment.getExternalStorageDirectory().path + "/Pictures/button.jpg"
        Log.e("ATU", "指定的按钮图片路径为$buttonPicturePath")
        return buttonPicturePath
    }

    fun getCurrentScreenPicturePath() :String{
        return Environment.getExternalStorageDirectory().path + "/Pictures/current_screen.png";
    }
}