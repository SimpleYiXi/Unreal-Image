package com.example.listviewtest

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.jnitestproject.MainActivity2
import com.example.jnitestproject.R
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class OptionsAdapter(val optionList: List<SingleOption>)
    : RecyclerView.Adapter<OptionsAdapter.ViewHolder>() {

    inner  class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val optionImage:ImageView = view.findViewById(R.id.optionImage)
        val optionName:TextView = view.findViewById(R.id.optionName)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.option_item,parent,false)
        val viewHolder = ViewHolder(view)
        viewHolder.itemView.setOnClickListener{
            val myview = view.findViewById<ImageView>(R.id.imageView)
            val drawable: Drawable = myview.getDrawable()
            var bitmap: Bitmap? = null
            if (null != drawable)
                bitmap = (drawable as BitmapDrawable).getBitmap()
            val mat = bitmap?.let { it1 -> bitmapToMat(it1) }
            val gray = cvtColor(mat,Imgproc.COLOR_BGRA2GRAY)
            if (bitmap!=null && mat !=null)
                Toast.makeText(parent.context, "ok", Toast.LENGTH_SHORT).show()
            else if(bitmap==null)
                Toast.makeText(parent.context, "bitmaop", Toast.LENGTH_SHORT).show()
            else if(mat ==null)
                Toast.makeText(parent.context, "bitmaop", Toast.LENGTH_SHORT).show()
//            Utils.matToBitmap(gray,bitmap)
//            myview.setImageBitmap(bitmap)

        }
        return ViewHolder(view)
    }
    fun bitmapToMat(bitmap: Bitmap): Mat{
        var resultMat: Mat = Mat()
        Utils.bitmapToMat(bitmap,resultMat)
        return resultMat
    }
    fun cvtColor(mat: Mat?, colorType:Int): Mat = if (mat == null) {
        val result = Mat()
        val initValue = 500
        val m = Mat(initValue,initValue, CvType.CV_8UC3)
        Imgproc.cvtColor(m,result,colorType)
        result
    }   else {
        val result = Mat()
        Imgproc.cvtColor(mat,result,colorType)
        result
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selection = optionList[position]
        holder.optionImage.setImageResource(selection.imageId)
        holder.optionName.text = selection.name
    }

    override fun getItemCount(): Int {
        return  optionList.size
    }
}