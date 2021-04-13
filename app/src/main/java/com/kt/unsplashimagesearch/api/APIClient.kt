package com.kt.unsplashimagesearch.api

import com.kt.unsplashimagesearch.data.model.UnsplashNestedModel
import com.kt.unsplashimagesearch.data.model.UnsplashSearchNestedModel
import org.json.JSONArray
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIClient {

    @GET("/collections/2423569/photos")
    suspend fun getUnsplashData(@Query(value = "page") page_id: Int,
                                @Query(value = "client_id") client_id : String): Array<UnsplashNestedModel>

    @GET("/search/collections")
    suspend fun getSearchedUnsplashData(@Query(value = "page") page_id: Int,
                                        @Query(value = "query") query: String,
                                @Query(value = "client_id") client_id : String): Response<UnsplashSearchNestedModel>

}