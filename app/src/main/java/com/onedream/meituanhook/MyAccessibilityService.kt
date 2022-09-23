package com.onedream.meituanhook

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.File
import kotlin.concurrent.thread

class MyAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent?) {
        Log.e("ATU", "onAccessibilityEvent")
        accessibilityEvent?.let {
            Log.e(
                "ATU",
                "onAccessibilityEvent= ${it.packageName} ${it.action} ${it.className.toString()}"
            )
        }
    }

    override fun onInterrupt() {
        Log.e("ATU", "onInterrupt")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.e("ATU", "onServiceConnected")
        thread {
            Thread.sleep(5000)
            //
           while (true){
                //click(622f, 406f)
                //swipe(200f, 0f, 200f, 2000f, 600)
                manualCaptureScreenAndClick()
                autoCaptureScreenAndClick()
            }
        }
    }

    private fun manualCaptureScreenAndClick(){
        if(null != ClickPointHelper.testClickRect){
            Log.e("ATU","不为空")
            click(ClickPointHelper.testClickRect!!.exactCenterX(), ClickPointHelper.testClickRect!!.exactCenterY(), 10)
        }
        Thread.sleep(5000)
    }


    private fun autoCaptureScreenAndClick(){
        if(null != ClickPointHelper.testClickRect){
            return
        }
        CaptureScreenService.start(this)
        Thread.sleep(5000)
        val smallImage =  Environment.getExternalStorageDirectory().path + "/Pictures/button.jpg"
        val nameImage = Environment.getExternalStorageDirectory().path + "/Pictures/current_screen.png"
        Log.e("ATU", "获取按钮图片存储位置为$smallImage")
        Log.e("ATU", "获取图片存储位置为$nameImage")
        //
        val smallFileImage = File(smallImage)
        val fileImage = File(nameImage)
        if (smallFileImage.exists() && fileImage.exists()) {
            //小图
            val bit  = BitmapFactory.decodeStream(smallFileImage.inputStream())
            val source = Mat(bit.height, bit.width, CvType.CV_32FC1)
            Utils.bitmapToMat(bit, source)
            //
            val screenBitmap = BitmapFactory.decodeStream(fileImage.inputStream())
            val target = Mat(screenBitmap.height, screenBitmap.width, CvType.CV_32FC1)
            Utils.bitmapToMat(screenBitmap, target)
            //
            val rect = ImageHelper.singleMatching(
                source,
                target,
                0.8f,
                Environment.getExternalStorageDirectory().path + "/Pictures/result.jpg"
            )
            Log.e("ATU", "路径为" + Environment.getExternalStorageDirectory().path + "/Pictures/result.jpg")
            if(null != rect){
                Log.e("ATU","不为空 点击坐标为x="+ rect.exactCenterX()+"====y="+rect.exactCenterY())
                click(rect.exactCenterX(),rect.exactCenterY(), 10)
            }
        }
        Thread.sleep(5000)
    }
}