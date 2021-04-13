package com.kt.unsplashimagesearch.ui.main.viewmodel

import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.LiveData
import com.kt.unsplashimagesearch.db.AppDatabase
import com.kt.unsplashimagesearch.db.UnsplashDao
import com.kt.unsplashimagesearch.db.UnsplashTable


class UnsplashRepository(application: Application?){
    private var unsplashDao: UnsplashDao? = null
    private var allSplash: LiveData<List<UnsplashTable>>? = null
    private var allSplashSearch: LiveData<List<UnsplashTable>>? = null

    init {
        unsplashDao = application?.let {
            AppDatabase.getDatabase(it)?.unsplashDao() }
        allSplash = unsplashDao?.getAllData("No")
        allSplashSearch = unsplashDao?.getAllSearchData("Yes")
    }

    fun getAllUnsplash(): LiveData<List<UnsplashTable>>? {
        return allSplash
    }

    fun getAllSearchUnsplash(): LiveData<List<UnsplashTable>>? {
        return allSplashSearch
    }

    suspend fun insert(photos: UnsplashTable) {
        unsplashDao?.insert(photos)
    }


    private class insertAsyncTask internal constructor(dao: UnsplashDao?) :
        AsyncTask<UnsplashTable?, Void?, Void?>() {
        private val mAsyncTaskDao: UnsplashDao?


        init {
            mAsyncTaskDao = dao
        }

        override fun doInBackground(vararg match: UnsplashTable?): Void? {

            return null
        }
    }
}