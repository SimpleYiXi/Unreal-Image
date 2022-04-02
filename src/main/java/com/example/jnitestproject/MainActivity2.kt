package com.example.jnitestproject

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.jnitestproject.databinding.ActivityMain2Binding
import com.example.listviewtest.SingleOption
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity2 : AppCompatActivity() {
    private lateinit var bd: ActivityMain2Binding
    private val TAG = "pain"
    private val sigma = 30.0f
    private val option = 1
    private var startFlag:Boolean = false
    private var uri : Uri? = null
    private val optionList = ArrayList<SingleOption>()
    private lateinit var mat:Mat
    private lateinit var dst:Mat
    private val k = Mat(3,3,CvType.CV_32FC1)
    private val selectPictureLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { returndata ->
        returndata?.let{ uri:Uri->
            val intent = Intent(this, MainActivity2::class.java)
            intent.putExtra("uri",uri.toString())
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.m1 ->{
                if(!startFlag)
                {
                    AlertDialog.Builder(this).apply {
                        setTitle("功能说明")
                        setMessage("点击该功能后，您需要在出现的编辑框中填入参数值，其中alpha在0~1时会使图像变暗，>1时变亮，glamma类似，以1为分界线。若不填入值，默认取值都为1.1。填值完毕后，再次点击功能即可生效。")
                        setCancelable(true)
                        setPositiveButton("好的") { dialog, which ->
                        }
                        setNegativeButton("OK") { dialog, which ->
                        }
                        show()
                    }
                    bd.editText1.visibility = View.VISIBLE
                    bd.editText2.visibility = View.VISIBLE
                    startFlag = true
                }
                else {
                    //调整亮度和对比度
                    mat=cvtColor(mat,Imgproc.COLOR_BGRA2BGR)
                    val black = mat.clone()
                    black.setTo(Scalar(0.0,0.0,0.0))
                    val dst = mat.clone()
                    var alpha = bd.editText1.text.toString().toDouble()
                    var glamma = bd.editText2.text.toString().toDouble()
                    if(bd.editText1.text.toString() == "")
                        alpha = 1.1
                    if(bd.editText2.text.toString() == "")
                        glamma = 1.1
                    //像素混合——基于权重
                    Core.addWeighted(mat,alpha,black,1-alpha,glamma,dst)
                    bd.imageView.setImageBitmap(matToBitmap(dst))
                    mat=dst.clone()
                    dst.release()
                    startFlag = false
                    bd.editText1.visibility = View.GONE
                    bd.editText2.visibility = View.GONE
                }
            }
            R.id.m2 ->{
                //Toast.makeText(this, "待补充", Toast.LENGTH_SHORT).show()

                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.boy)
                val mat1 = bitmapToMat(bitmap)
                val src =cvtColor(mat1,Imgproc.COLOR_BGRA2GRAY)
                //造张纯黑图
                val black = src.clone()
                black.setTo(Scalar(0.0,0.0,0.0))
                val dst = src.clone()
                val alpha = 1.0
                val gamma = 0.1
                //像素混合——基于权重
                Core.addWeighted(src,alpha,black,1-alpha,gamma,dst)
                bd.imageView.setImageBitmap(matToBitmap(dst))
                mat =dst;

            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bd = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(bd.root)
        val data = FloatArray(9){_ ->0.0f}
        for(i in data.indices)
        {
            if(i==1 || i==3 || i==5 || i==7)
                data[i] = 1.0f/8.0f
            else if(i==4)
                data[i] = 4.0f/8.0f  //即0.5f
        }
        k.put(0,0,data)
        //锐化因子初始化完成
        var bitmap: Bitmap? = BitmapFactory.decodeResource(resources,R.drawable.second)
        uri = Uri.parse(intent.getStringExtra("uri"));
        if (getBitmapFromUri(uri!!) == null)
            Toast.makeText(this, "初始化失败~", Toast.LENGTH_SHORT).show()
        else bitmap=getBitmapFromUri(uri!!)
        bd.imageView.setImageBitmap(bitmap)
        mat = bitmap?.let { bitmapToMat(it) }!!

        bd.reSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            //指定文件选择器打开后只显示图片(限定文件类型)
            val myal: Array<String> = arrayOf("image/*")
            selectPictureLauncher.launch(myal)
        }
        bd.saveImage.setOnClickListener {
            var mybitmap = matToBitmap(mat)!!
            if (mat.empty()) {
                Toast.makeText(this, "Error in initialization of mat!", Toast.LENGTH_SHORT).show()
                finish()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) mybitmap.let { it1 ->
                saveImageInQ(
                    it1
                )
            }
            else mybitmap?.let { it1 -> saveTheImageLegacyStyle(it1,"yours"+System.currentTimeMillis()) }
            Toast.makeText(this, "图片已保存~", Toast.LENGTH_SHORT).show()
        }
        bd.beautify.setOnClickListener {
            Toast.makeText(this, "正在处理~", Toast.LENGTH_SHORT).show()
            dst = cvtColor(mat,Imgproc.COLOR_BGRA2BGR)
            simpleBeautify(dst.nativeObjAddr,mat.nativeObjAddr,k.nativeObjAddr)
            Toast.makeText(this, "处理完成~", Toast.LENGTH_SHORT).show()
            bd.imageView.setImageBitmap(matToBitmap(mat))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu)
    }
    fun saveImageInQ(bitmap: Bitmap): Uri? {
        val filename = "YIXI_IMG_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        var imageUri: Uri? = null
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        //use application context to get contentResolver
        val contentResolver = application.contentResolver
        val resolver = this.contentResolver
        contentResolver.also { resolver ->
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }

        fos?.use { bitmap.compress(Bitmap.CompressFormat.JPEG, 70, it) }

        contentValues.clear()
        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
        imageUri?.let { resolver.update(it, contentValues, null, null) }

        return imageUri
    }
    fun saveTheImageLegacyStyle(bitmap:Bitmap,filename:String){
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        val fos = FileOutputStream(image)
        fos?.use {bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)}
    }

    private fun initOptions(){
//        optionList.add(SingleOption("使用说明",R.drawable.baiyun))
//        optionList.add(SingleOption("一键美颜",R.drawable.baiyun))
    }

    external fun simpleBeautify(srcAddress: Long, dstAddress: Long,kernelAddress:Long)
    external fun stringFromJNI(): String

    fun matToBitmap(mat: Mat?): Bitmap? {
        var resultBitmap: Bitmap? = null
        if (mat != null) {
            resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
            if (resultBitmap != null) Utils.matToBitmap(mat, resultBitmap)
        }
        return resultBitmap
    }
    fun bitmapToMat(bitmap: Bitmap): Mat{
        var resultMat: Mat = Mat()
        Utils.bitmapToMat(bitmap,resultMat)
        return resultMat
    }
    fun cvtColor(mat:Mat?,colorType:Int):Mat= if (mat == null) {
        val result = Mat()
        val initValue = 500
        val m = Mat(initValue,initValue, CvType.CV_8UC3)
        Imgproc.cvtColor(m,result,colorType)
        result
    }
    else {
        val result = Mat()
        Imgproc.cvtColor(mat,result,colorType)
        result
    }
    private fun getBitmapFromUri(uri: Uri) = contentResolver.openFileDescriptor(uri,"r")?.use {
        BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
    }
}