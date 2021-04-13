package com.kt.unsplashimagesearch.ui.main.adapter

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kt.unsplashimagesearch.R
import com.kt.unsplashimagesearch.ui.base.DisplayActivity
import com.kt.unsplashimagesearch.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class PhotosRecyclerAdapter(
    private val mCtx: Context,
    private var photosList: JSONArray,
    var isSearch: Boolean
) :
    RecyclerView.Adapter<PhotosRecyclerAdapter.TasksViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val view: View =
            LayoutInflater.from(mCtx).inflate(
                R.layout.layout_item_photos,
                parent, false
            )

        return TasksViewHolder(view)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val photoItem = photosList.getJSONObject(position)
        val photoUrlSmall : String
        val photoUrlFull : String
        if (isSearch){
            photoUrlSmall = photoItem.optJSONObject(mCtx.getString(R.string.param_cover_photo))
                .optJSONObject(mCtx.getString(R.string.param_urls)).optString(mCtx.getString(R.string.param_small)).toString()
            photoUrlFull = photoItem.optJSONObject(mCtx.getString(R.string.param_cover_photo))
                .optJSONObject(mCtx.getString(R.string.param_urls)).optString(mCtx.getString(R.string.param_regular)).toString()
        } else{
             photoUrlSmall = photoItem.optJSONObject(mCtx.getString(R.string.param_urls))
                 .optString(mCtx.getString(R.string.param_small)).toString()
            photoUrlFull = photoItem.optJSONObject(mCtx.getString(R.string.param_urls))
                .optString(mCtx.getString(R.string.param_full)).toString()
        }
        val bitmap = Utils.getBitmapFromURL(photoUrlSmall)
        if(null != bitmap){
            holder.imgPhoto.setImageBitmap(bitmap)
        }
//        doSomeNetworkStuff(photoItem.optJSONObject("urls").optString("small"), holder.imgPhoto)
        holder.clMain.setOnClickListener {
            val myIntent = Intent(mCtx, DisplayActivity::class.java)
            myIntent.putExtra(mCtx.getString(R.string.img_path), photoUrlFull)
            mCtx.startActivity(myIntent)
        }
    }

    override fun getItemCount(): Int {
        return photosList.length() ?: 0
    }

    fun updateData(jsonArray: JSONArray) {
        photosList = jsonArray
        notifyDataSetChanged()
    }

    class TasksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.img_photo)
        val clMain: ConstraintLayout = itemView.findViewById(R.id.cl_main)
    }

    fun doSomeNetworkStuff(src: String, imgPhoto: ImageView) {
        GlobalScope.launch(Dispatchers.IO) {
            val url = URL(src)
            val connection: HttpURLConnection = url
                .openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            imgPhoto.setImageBitmap(BitmapFactory.decodeStream(input))
        }
    }
}