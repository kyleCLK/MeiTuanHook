package com.onedream.meituanhook

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.onedream.meituanhook.accessibility.jumpToAccessibilitySetting
import com.onedream.meituanhook.image.CaptureScreenService
import com.onedream.meituanhook.image.ImageConfigureHelper
import com.onedream.meituanhook.image.ImageOpenCVHelper
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.io.FileNotFoundException

class MainActivity : AppCompatActivity() {
    private var bitmap: Bitmap? = null
    private var bitmap2: Bitmap? = null

    override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.i(
                "ATU cv",
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
        } else {
            Log.i("ATU cv", "OpenCV library found inside package. Using it!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //
        findViewById<Button>(R.id.btn_jump_to_settings).setOnClickListener {
            if (isHasCaptureScreenPermission()) {
                this.jumpToAccessibilitySetting()
            }
        }

        findViewById<Button>(R.id.but).setOnClickListener(View.OnClickListener { //intent可以应用于广播和发起意图，其中属性有：ComponentName,action,data等
            val intent = Intent()
            intent.type = "image/*"
            //action表示intent的类型，可以是查看、删除、发布或其他情况；我们选择ACTION_GET_CONTENT，系统可以根据Type类型来调用系统程序选择Type
            //类型的内容给你选择
            intent.action = Intent.ACTION_GET_CONTENT
            //如果第二个参数大于或等于0，那么当用户操作完成后会返回到本程序的onActivityResult方法
            startActivityForResult(intent, GET_IMAGE_INTENT_REQUEST_CODE)
        })

        findViewById<Button>(R.id.use).setOnClickListener(View.OnClickListener {
            //小图
            val bit = bitmap!!.copy(Bitmap.Config.ARGB_8888, false)
            val source = Mat(bit.height, bit.width, CvType.CV_32FC1)
            Utils.bitmapToMat(bit, source)
            //大图
            val bit2 = bitmap2!!.copy(Bitmap.Config.ARGB_8888, false)
            val target = Mat(bit2.height, bit2.width, CvType.CV_32FC1)
            Utils.bitmapToMat(bit2, target)
            //
            val resultPicturePath = ImageConfigureHelper.getResultPicturePath()

            ClickPointHelper.testClickRect = ImageOpenCVHelper.singleMatching(
                source,
                target,
                0.8f,
                resultPicturePath
            )
        })
    }


    private fun isHasCaptureScreenPermission(): Boolean {
        val mMediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        CaptureScreenService.setMediaProjectionManager(mMediaProjectionManager)
        return if (CaptureScreenService.getIntent() != null && CaptureScreenService.getIntentResultCode() !== 0) {
            true
        } else {
            startActivityForResult(
                mMediaProjectionManager.createScreenCaptureIntent(),
                SCREEN_CAPTURE_INTENT_REQUEST_CODE
            )
            CaptureScreenService.setMediaProjectionManager(mMediaProjectionManager)
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            GET_IMAGE_INTENT_REQUEST_CODE -> {
                if (resultCode != RESULT_OK) {
                    Log.e("ATU", "获取本地图片请求操作的返回码错误")
                    return
                }
                if (null == data) {
                    Log.e("ATU", "获取本地图片请求操作的返回数据为空")
                    return
                }
                val uri = data.data
                Log.e("ATU 获取本地图片请求操作的图片uri", uri.toString())
                //使用content的接口
                val cr = this.contentResolver
                try {
                    if (null == bitmap) {
                        //获取图片
                        bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri!!))
                        findViewById<ImageView>(R.id.img)!!.setImageBitmap(bitmap)
                    } else {
                        bitmap2 = BitmapFactory.decodeStream(cr.openInputStream(uri!!))
                        findViewById<ImageView>(R.id.img_big)!!.setImageBitmap(bitmap2)
                    }
                } catch (e: FileNotFoundException) {
                    Log.e("ATU 获取本地图片请求操作的Exception", e.message, e)
                }
            }

            SCREEN_CAPTURE_INTENT_REQUEST_CODE -> {
                if (resultCode != RESULT_OK) {
                    Log.e("ATU", "截屏权限请求操作的返回码错误")
                    return
                }
                if (null == data) {
                    Log.e("ATU", "截屏权限请求操作的返回数据为空")
                    return
                }
                CaptureScreenService.setIntentResultCode(resultCode)
                CaptureScreenService.setIntent(data)
                //
                this.jumpToAccessibilitySetting()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }


    companion object {
        private const val SCREEN_CAPTURE_INTENT_REQUEST_CODE = 20001//截屏权限请求码
        private const val GET_IMAGE_INTENT_REQUEST_CODE = 1//获取图片请求码
    }
}