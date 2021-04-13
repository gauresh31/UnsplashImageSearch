package com.kt.unsplashimagesearch.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity
public class UnsplashTable : Serializable {
    @PrimaryKey(autoGenerate = true)
    private var id = 0

    @ColumnInfo(name = "updated_at")
    var updatedAt: String? = null

    @ColumnInfo(name = "full")
    var full: String? = null

    @ColumnInfo(name = "regular")
    var regular: String? = null

    @ColumnInfo(name = "small")
    var small : String? = null

    @ColumnInfo(name = "thumb")
    var thumb : String? = null

    @ColumnInfo(name = "alt_description")
    var altDescription : String? = null

    @ColumnInfo(name = "search")
    var search : String? = null

    @ColumnInfo(name = "color")
    var color : String? = null

    /*
    * Getters and Setters
    * */
    fun getId(): Int {
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }
}