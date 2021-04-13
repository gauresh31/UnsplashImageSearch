package com.kt.unsplashimagesearch.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update


@Dao
interface UnsplashDao {
    @Query("SELECT * FROM unsplashtable where search Like :is_search order by updated_at DESC")
    suspend fun getDateSortedDescSearch(is_search : String): List<UnsplashTable?>?

    @Query("SELECT * FROM unsplashtable where search Like :is_search and color In ('#000000','#FFFFFF') order by updated_at DESC")
    suspend fun getColorNewSearch(is_search : String): List<UnsplashTable?>?

    @Query("SELECT * FROM unsplashtable where search Like :is_search and color In ('#000000','#FFFFFF')")
    suspend fun getColorSearch(is_search : String): List<UnsplashTable?>?

    @Insert
    suspend fun insert(country: UnsplashTable)

    @Update
    fun update(country: UnsplashTable?)

    @Query("DELETE FROM unsplashtable")
    suspend fun deleteAll()

    @Query("DELETE FROM unsplashtable where search Like :is_search")
    suspend fun deleteAllSearch(is_search : String)

    @Query("SELECT * FROM unsplashtable where search Like :is_search order by updated_at ASC")
    suspend fun getAll(is_search : String): List<UnsplashTable>

    @Query("SELECT * FROM unsplashtable where search Like :is_search order by updated_at ASC")
    fun getAllData(is_search : String): LiveData<List<UnsplashTable>>

    @Query("SELECT * FROM unsplashtable where search Like :is_search order by updated_at ASC")
    fun getAllSearchData(is_search : String): LiveData<List<UnsplashTable>>



//    @Query("SELECT * FROM unsplashtable where id = rowid")
//    fun getSearch(rowid : String): List<UnsplashTable?>?
}
