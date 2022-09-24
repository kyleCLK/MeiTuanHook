package com.onedream.meituanhook.system

import android.content.Context
import android.util.DisplayMetrics
import android.view.Display

object ScreenHelper {

    fun getDisplayMetrics(context :Context):DisplayMetrics{
        val display: Display = context.display!!
        val dm = DisplayMetrics()
        display.getRealMetrics(dm) // 屏幕宽度、高度、密度、缩放因子
        return dm
    }

   /* fun getDisplayMetrics(context :Context):DisplayMetrics{
        val resources = context.resources
        val dm = resources.displayMetrics
        return dm
    }*/
}