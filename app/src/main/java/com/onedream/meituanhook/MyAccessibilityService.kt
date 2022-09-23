package com.onedream.meituanhook

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlin.concurrent.thread

class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent?) {
        Log.e("ATU", "onAccessibilityEvent")
        accessibilityEvent?.let {
            Log.e("ATU", "onAccessibilityEvent= ${it.packageName} ${it.action} ${it.className.toString()}")
        }
    }

    override fun onInterrupt() {
        Log.e("ATU", "onInterrupt")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.e("ATU", "onServiceConnected")
        thread {
            for(i in 0..30000){
                Thread.sleep(5000)
                //click(622f, 406f)
                //swipe(200f, 0f, 200f, 2000f, 600)
                if(null != ClickPointHelper.testClickRect){
                    Log.e("ATU","不为空")
                    click(ClickPointHelper.testClickRect!!.exactCenterX(), ClickPointHelper.testClickRect!!.exactCenterY(), 10)
                }
            }
        }
    }
}