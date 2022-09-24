package com.onedream.meituanhook.accessibility

import android.accessibilityservice.AccessibilityService
import android.graphics.BitmapFactory
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.onedream.meituanhook.ClickPointHelper
import com.onedream.meituanhook.image.CaptureScreenService
import com.onedream.meituanhook.image.ImageConfigureHelper
import com.onedream.meituanhook.image.ImageOpenCVHelper
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
        val buttonImage =  ImageConfigureHelper.getButtonPicturePath()
        val currentScreenImage = ImageConfigureHelper.getCurrentScreenPicturePath()
        //
        val buttonImageFile = File(buttonImage)
        val currentScreenImageFile = File(currentScreenImage)
        if (buttonImageFile.exists() && currentScreenImageFile.exists()) {
            //小图
            val buttonBitmap  = BitmapFactory.decodeStream(buttonImageFile.inputStream())
            val source = Mat(buttonBitmap.height, buttonBitmap.width, CvType.CV_32FC1)
            Utils.bitmapToMat(buttonBitmap, source)
            //
            val screenBitmap = BitmapFactory.decodeStream(currentScreenImageFile.inputStream())
            val target = Mat(screenBitmap.height, screenBitmap.width, CvType.CV_32FC1)
            Utils.bitmapToMat(screenBitmap, target)
            //
            val resultPicturePath = ImageConfigureHelper.getResultPicturePath()
            val rect = ImageOpenCVHelper.singleMatching(
                source,
                target,
                0.8f,
                resultPicturePath
            )
            if(null != rect){
                Log.e("ATU","不为空 点击坐标为x="+ rect.exactCenterX()+"====y="+rect.exactCenterY())
                click(rect.exactCenterX(),rect.exactCenterY(), 10)
            }
        }
        Thread.sleep(5000)
    }
}