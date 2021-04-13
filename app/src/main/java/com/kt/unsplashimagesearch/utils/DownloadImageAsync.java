package com.kt.unsplashimagesearch.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.kt.unsplashimagesearch.ui.main.adapter.UnsplashRecyclerAdapter;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImageAsync extends AsyncTask<String, Void, Bitmap> {
    private int inSampleSize = 0;
    private String imageUrl;
    private UnsplashRecyclerAdapter adapter;
    private final ImagesCache cache;
    private final int desiredWidth;
    private final int desiredHeight;
    private Bitmap image = null;
    private ImageView ivImageView;

    public DownloadImageAsync(ImagesCache cache, ImageView ivImageView, int desireWidth, int desireHeight, UnsplashRecyclerAdapter unAdapter) {
        this.cache = cache;
        this.ivImageView = ivImageView;
        this.desiredHeight = desireHeight;
        this.desiredWidth = desireWidth;
        this.adapter = unAdapter;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        imageUrl = params[0];
        return getImage(imageUrl);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        if (result != null) {
            cache.addImageToWarehouse(imageUrl, result);
            if (ivImageView != null) {
                ivImageView.setImageBitmap(result);
                adapter.notifyDataSetChanged();
            } else if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private Bitmap getImage(String imageUrl) {
        if (cache.getImageFromWarehouse(imageUrl) == null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = inSampleSize;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                image = BitmapFactory.decodeStream(stream, null, options);
                int imageWidth = options.outWidth;
                int imageHeight = options.outHeight;
                if (imageWidth > desiredWidth || imageHeight > desiredHeight) {
                    System.out.println("imageWidth:" + imageWidth + ", imageHeight:" + imageHeight);
                    inSampleSize = inSampleSize + 2;
                    getImage(imageUrl);
                } else {
                    options.inJustDecodeBounds = false;
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    stream = connection.getInputStream();
                    image = BitmapFactory.decodeStream(stream);
                    return image;
                }
            } catch (Exception e) {
                Log.e("getImageException--->", e.toString());
            }
        }
        return image;
    }
}