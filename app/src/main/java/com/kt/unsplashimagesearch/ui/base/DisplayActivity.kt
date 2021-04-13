package com.kt.unsplashimagesearch.ui.base

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.kt.unsplashimagesearch.R
import com.kt.unsplashimagesearch.utils.Utils
import kotlinx.android.synthetic.main.activity_display.*

class DisplayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setContentView(R.layout.activity_display)
        initValues()
    }

    private fun initValues() {
        title = getString(R.string.app_name)
        val intentVal: Intent = intent
        val bitmap = Utils.getBitmapFromURL(intentVal.getStringExtra(getString(R.string.img_path)))
        img_photo_detail.setImageBitmap(bitmap)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}