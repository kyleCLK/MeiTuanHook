package com.onedream.meituanhook

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.onedream.meituanhook.accessibility.jumpToAccessibilitySetting
import com.onedream.meituanhook.image.CaptureScreenService
import com.onedream.meituanhook.image.ImageConfigureHelper
import com.onedream.meituanhook.image.ImageOpenCVHelper
import com.onedream.meituanhook.permission.PermissionHelper
import com.onedream.meituanhook.shared_preferences.ScreenLocalStorage
import com.onedream.meituanhook.system.ScreenHelper
import kotlinx.android.synthetic.main.activity_main.*
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
        showScreenHeight()
        //
        btn_edit.setOnClickListener {
            changeScreenHeightEditText(true)
        }
        btn_save.setOnClickListener {
            val editHeight = edit_abc.text.toString()
            if (editHeight.isNullOrEmpty()) {
                return@setOnClickListener
            }
            try {
                ScreenLocalStorage.setScreenHeight(editHeight.toInt())
                showScreenHeight()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        //
        btn_jump_to_settings.setOnClickListener {
            start()
        }


        but.setOnClickListener(View.OnClickListener { //intent可以应用于广播和发起意图，其中属性有：ComponentName,action,data等
            val intent = Intent()
            intent.type = "image/*"
            //action表示intent的类型，可以是查看、删除、发布或其他情况；我们选择ACTION_GET_CONTENT，系统可以根据Type类型来调用系统程序选择Type
            //类型的内容给你选择
            intent.action = Intent.ACTION_GET_CONTENT
            //如果第二个参数大于或等于0，那么当用户操作完成后会返回到本程序的onActivityResult方法
            startActivityForResult(intent, GET_IMAGE_INTENT_REQUEST_CODE)
        })

        use.setOnClickListener(View.OnClickListener {
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
            if (null != ClickPointHelper.testClickRect) {
                Toast.makeText(this, "识别到按钮的区域！！", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "错误！！识别不到按钮的区域！！", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showScreenHeight() {
        edit_abc.setText(
            ScreenLocalStorage.getScreenHeight(ScreenHelper.getDisplayMetrics(this).heightPixels)
                .toString()
        )
        changeScreenHeightEditText(false)
    }

    private fun changeScreenHeightEditText(isEnabled: Boolean) {
        edit_abc.isEnabled = isEnabled
        btn_edit.visibility = if (isEnabled) View.GONE else View.VISIBLE
        btn_save.visibility = if (isEnabled) View.VISIBLE else View.GONE
    }

    private fun start() {
        if (isHasExternalStoragePermission()) {
            if (isHasCaptureScreenPermission()) {
                this.jumpToAccessibilitySetting()
            }
        }
    }

    private fun isHasExternalStoragePermission(): Boolean {
        return if (PermissionHelper.hasPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            true
        } else {
            val permissionArray = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            requestPermissions(permissionArray, REQUEST_EXTERNAL_STORAGE_PERMISSION_CODE)
            false
        }
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
            false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionHelper.processRequestPermissionResult(this, permissions, grantResults, object :
            PermissionHelper.WeDoXPermissionListener {
            override fun permissionRequestSuccess() {
                start()
            }

            override fun permissionRequestFail(
                grantedPermissions: Array<out String>?,
                deniedPermissions: Array<out String>?,
                forceDeniedPermissions: Array<out String>?
            ) {
                if (null != forceDeniedPermissions && forceDeniedPermissions.isNotEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "请到应用设置界面开启读写权限，否则无法继续使用该功能",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (null != deniedPermissions && deniedPermissions.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "请允许所有权限，方可继续使用该功能", Toast.LENGTH_LONG).show()
                    start()
                } else {
                    start()
                }
            }
        })
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
                        img.setImageBitmap(bitmap)
                    } else {
                        bitmap2 = BitmapFactory.decodeStream(cr.openInputStream(uri!!))
                        img_big.setImageBitmap(bitmap2)
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
        private const val REQUEST_EXTERNAL_STORAGE_PERMISSION_CODE = 30000//请求存储权限码
    }
}