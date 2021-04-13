package com.kt.unsplashimagesearch.ui.main.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.kt.unsplashimagesearch.R
import com.kt.unsplashimagesearch.data.model.Results
import com.kt.unsplashimagesearch.data.model.UnsplashNestedModel
import com.kt.unsplashimagesearch.db.AppDatabase
import com.kt.unsplashimagesearch.db.UnsplashDao
import com.kt.unsplashimagesearch.db.UnsplashTable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class UnsplashViewModel(application: Application) : AndroidViewModel(application) {
    private var mRepository: UnsplashRepository = UnsplashRepository(application)
    private val unsplashDao: UnsplashDao? = AppDatabase.getDatabase(application)?.unsplashDao()
    private var unsplashList: LiveData<List<UnsplashTable>>? = mRepository.getAllUnsplash()
    private var unsplashListSearch: LiveData<List<UnsplashTable>>? = mRepository.getAllSearchUnsplash()


    fun getAllUnsplash(): LiveData<List<UnsplashTable>>? {
        return unsplashList
    }

    fun getAllUnsplashSearch(): LiveData<List<UnsplashTable>>? {
        return unsplashListSearch
    }

    suspend fun insert(photos: UnsplashTable) {
        mRepository.insert(photos)
    }

//    fun getSearch() {
//        unsplashDao?.getSearch("")
//    }

    fun update(photos: UnsplashTable) {
        unsplashDao?.update(photos)
    }

    suspend fun deleteAll() {
        unsplashDao?.deleteAll()
    }

    suspend fun saveUnsplashData(context: Context, response: Array<UnsplashNestedModel>) {
        for (i in 0 until response.count()) {
            val record = UnsplashTable()
            val name = response[i].alt_description ?: context.getString(R.string.not_appl)
            Log.d("alt_Desc: ", name)
            record.altDescription = name

            val updatedDate = response[i].updated_at ?: context.getString(R.string.not_appl)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ldt:LocalDateTime = LocalDateTime.parse(updatedDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ"))
                val currentZoneId: ZoneId = ZoneId.systemDefault()
                val currentZonedDateTime: ZonedDateTime = ldt.atZone(currentZoneId)
                val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val formattedDate = format.format(currentZonedDateTime)
                record.updatedAt = formattedDate
            } else {
                record.updatedAt = updatedDate
            }


            val fulll = response[i].urls?.full ?: context.getString(R.string.not_appl)
            record.full = fulll

            val smll = response[i].urls?.small ?: context.getString(R.string.not_appl)
            record.small = smll

            val reg = response[i].urls?.regular ?: context.getString(R.string.not_appl)
            record.regular = reg

            val thmp = response[i].urls?.thumb ?: context.getString(R.string.not_appl)
            record.thumb = thmp
            record.search = context.getString(R.string.str_no)
            insert(record)
        }
    }

    suspend fun saveSearchData(context: Context, response: List<Results>?) {
        for (i in 0 until response?.count()!!) {
            val record = UnsplashTable()
            val name = response[i].alt_description ?: context.getString(R.string.not_appl)
            Log.d("alt_Desc: ", name)
            record.altDescription = name

            val updatedDate = response[i].updated_at ?: context.getString(R.string.not_appl)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ldt:LocalDateTime = LocalDateTime.parse(updatedDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ"))
                val currentZoneId: ZoneId = ZoneId.systemDefault()
                val currentZonedDateTime: ZonedDateTime = ldt.atZone(currentZoneId)
                val format: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val formattedDate = format.format(currentZonedDateTime)
                record.updatedAt = formattedDate
            } else {
                record.updatedAt = updatedDate
            }

            val fulll = response[i].cover_photo?.urls?.full ?: context.getString(R.string.not_appl)
            record.full = fulll

            val smll = response[i].cover_photo?.urls?.small ?: context.getString(R.string.not_appl)
            record.small = smll

            val reg = response[i].cover_photo?.urls?.regular ?: context.getString(R.string.not_appl)
            record.regular = reg

            val thmp = response[i].cover_photo?.urls?.thumb ?: context.getString(R.string.not_appl)
            record.thumb = thmp

            val colr = response[i].cover_photo?.color ?: context.getString(R.string.not_appl)
            record.color = colr

            record.search = context.getString(R.string.str_yes)
            insert(record)
        }
    }
}