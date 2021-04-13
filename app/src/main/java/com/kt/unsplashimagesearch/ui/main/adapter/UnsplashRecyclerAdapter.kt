package com.kt.unsplashimagesearch.ui.main.adapter

import android.app.ActivityOptions
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kt.unsplashimagesearch.R
import com.kt.unsplashimagesearch.db.AppDatabase
import com.kt.unsplashimagesearch.db.UnsplashTable
import com.kt.unsplashimagesearch.ui.base.DisplayActivity
import com.kt.unsplashimagesearch.utils.DownloadImageAsync
import com.kt.unsplashimagesearch.utils.ImagesCache


class UnsplashRecyclerAdapter(private val mCtx: Context, var isSearch: Boolean) :
    RecyclerView.Adapter<UnsplashRecyclerAdapter.TasksViewHolder>() {

    private var dbClient: AppDatabase? = null
    private var unsplashList: List<UnsplashTable?>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val view: View =
            LayoutInflater.from(mCtx).inflate(
                R.layout.layout_item_photos,
                parent, false
            )
        dbClient = AppDatabase.getDatabase(mCtx)

        return TasksViewHolder(view)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val photoItem: UnsplashTable? = unsplashList?.get(position)
        val photoUrlSmall : String?
        val photoUrlFull : String?
        if (isSearch){
            photoUrlSmall = photoItem?.small
            photoUrlFull = photoItem?.regular
        } else{
            photoUrlSmall = photoItem?.small
            photoUrlFull = photoItem?.full
        }
//        val bitmap = Utils.getBitmapFromURL(photoUrlSmall)
//        if(null != bitmap){
//            holder.imgPhoto.setImageBitmap(bitmap)
//        }

        val cache = ImagesCache.getInstance()
        val bm = cache.getImageFromWarehouse(photoUrlSmall)
        if (bm != null) {
            holder.imgPhoto.setImageBitmap(bm)
        } else {
//            holder.imgPhoto.setImageBitmap(null)
            val imgTask = DownloadImageAsync(
                cache,
                holder.imgPhoto,
                400,
                400,
                this
            )
            imgTask.execute(photoUrlSmall)
        }

        holder.clMain.setOnClickListener {
            val progress = ProgressDialog(mCtx)
            progress.setMessage("Please wait....")
            progress.show()

            val progressRunnable = Runnable {
                progress.cancel()
            }

            val handler = Handler()
            handler.postDelayed(progressRunnable, 5000)
            val myIntent = Intent(mCtx, DisplayActivity::class.java)
            myIntent.putExtra(mCtx.getString(R.string.img_path), photoUrlFull)
            mCtx.startActivity(myIntent)
        }
    }

    override fun getItemCount(): Int {
        return if (unsplashList != null)
            unsplashList!!.size
        else 0
    }

    fun updateData(matches: List<UnsplashTable>) {
        unsplashList = matches
//        unsplashList = (unsplashList as List<UnsplashTable>).sortedBy { it.updatedAt }
        notifyDataSetChanged()
    }

    class TasksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPhoto: ImageView = itemView.findViewById(R.id.img_photo)
        val clMain: ConstraintLayout = itemView.findViewById(R.id.cl_main)
    }
}