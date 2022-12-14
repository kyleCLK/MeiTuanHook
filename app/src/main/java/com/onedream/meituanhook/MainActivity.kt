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


        but.setOnClickListener(View.OnClickListener { //intent?????????????????????????????????????????????????????????ComponentName,action,data???
            val intent = Intent()
            intent.type = "image/*"
            //action??????intent???????????????????????????????????????????????????????????????????????????ACTION_GET_CONTENT?????????????????????Type?????????????????????????????????Type
            //???????????????????????????
            intent.action = Intent.ACTION_GET_CONTENT
            //????????????????????????????????????0?????????????????????????????????????????????????????????onActivityResult??????
            startActivityForResult(intent, GET_IMAGE_INTENT_REQUEST_CODE)
        })

        use.setOnClickListener(View.OnClickListener {
            //??????
            val bit = bitmap!!.copy(Bitmap.Config.ARGB_8888, false)
            val source = Mat(bit.height, bit.width, CvType.CV_32FC1)
            Utils.bitmapToMat(bit, source)
            //??????
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
                Toast.makeText(this, "??????????????????????????????", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "?????????????????????????????????????????????", Toast.LENGTH_SHORT).show()
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
                        "??????????????????????????????????????????????????????????????????????????????",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (null != deniedPermissions && deniedPermissions.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "???????????????????????????????????????????????????", Toast.LENGTH_LONG).show()
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
                    Log.e("ATU", "????????????????????????????????????????????????")
                    return
                }
                if (null == data) {
                    Log.e("ATU", "???????????????????????????????????????????????????")
                    return
                }
                val uri = data.data
                Log.e("ATU ???????????????????????????????????????uri", uri.toString())
                //??????content?????????
                val cr = this.contentResolver
                try {
                    if (null == bitmap) {
                        //????????????
                        bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri!!))
                        img.setImageBitmap(bitmap)
                    } else {
                        bitmap2 = BitmapFactory.decodeStream(cr.openInputStream(uri!!))
                        img_big.setImageBitmap(bitmap2)
                    }
                } catch (e: FileNotFoundException) {
                    Log.e("ATU ?????????????????????????????????Exception", e.message, e)
                }
            }

            SCREEN_CAPTURE_INTENT_REQUEST_CODE -> {
                if (resultCode != RESULT_OK) {
                    Log.e("ATU", "??????????????????????????????????????????")
                    return
                }
                if (null == data) {
                    Log.e("ATU", "?????????????????????????????????????????????")
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
        private const val SCREEN_CAPTURE_INTENT_REQUEST_CODE = 20001//?????????????????????
        private const val GET_IMAGE_INTENT_REQUEST_CODE = 1//?????????????????????
        private const val REQUEST_EXTERNAL_STORAGE_PERMISSION_CODE = 30000//?????????????????????
    }
}