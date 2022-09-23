package com.onedream.meituanhook

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
                "cv",
                "Internal OpenCV library not found. Using OpenCV Manager for initialization"
            )
        } else {
            Log.i("cv", "OpenCV library found inside package. Using it!")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //
        findViewById<Button>(R.id.btn_jump_to_settings).setOnClickListener {
            this.jumpToAccessibilitySetting()
        }
        findViewById<TextView>(R.id.btn_test).setOnClickListener {
            Toast.makeText(this, "I'm clicked", Toast.LENGTH_SHORT).show()
        }


        findViewById<Button>(R.id.but).setOnClickListener(View.OnClickListener { //intent可以应用于广播和发起意图，其中属性有：ComponentName,action,data等
            val intent = Intent()
            intent.type = "image/*"
            //action表示intent的类型，可以是查看、删除、发布或其他情况；我们选择ACTION_GET_CONTENT，系统可以根据Type类型来调用系统程序选择Type
            //类型的内容给你选择
            intent.action = Intent.ACTION_GET_CONTENT
            //如果第二个参数大于或等于0，那么当用户操作完成后会返回到本程序的onActivityResult方法
            startActivityForResult(intent, 1)
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
            ImageHelper.matching(
                source,
                target,
                0.8f,
                getExternalFilesDir("").toString() + "/result.jpg"
            )
            Log.e("ATU", "路径为" + getExternalFilesDir("") + "/result.jpg")
            /* Imgproc.cvtColor(src, src, Imgproc.COLOR_BGR2GRAY);
                    Utils.matToBitmap(src, bitmap);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            img.setImageBitmap(bitmap);
                        }
                    });*/
            //
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //用户操作完成，结果码返回是-1，即RESULT_OK
        if (resultCode == RESULT_OK && requestCode == 1) {
            //获取选中文件的定位符
            data?.let {
                val uri = data.data
                Log.e("uri", uri.toString())
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
                    Log.e("Exception", e.message, e)
                }
            }

        } else {
            //操作错误或没有选择图片
            Log.i("MainActivtiy", "operation error")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}