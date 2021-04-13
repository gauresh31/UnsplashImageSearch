package com.kt.unsplashimagesearch.ui.base

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.kt.unsplashimagesearch.R
import com.kt.unsplashimagesearch.api.APIAdapter
import com.kt.unsplashimagesearch.db.AppDatabase
import com.kt.unsplashimagesearch.db.UnsplashDao
import com.kt.unsplashimagesearch.db.UnsplashTable
import com.kt.unsplashimagesearch.ui.main.adapter.PhotosRecyclerAdapter
import com.kt.unsplashimagesearch.ui.main.adapter.UnsplashRecyclerAdapter
import com.kt.unsplashimagesearch.ui.main.viewmodel.UnsplashViewModel
import com.kt.unsplashimagesearch.utils.ImagesCache
import com.kt.unsplashimagesearch.utils.PreferenceUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.json.JSONArray


class MainActivity : AppCompatActivity() {
    private var dbClient: AppDatabase? = null
    private lateinit var unsplashAdapter: UnsplashRecyclerAdapter
    private lateinit var unsplashViewModel: UnsplashViewModel
    private var unsplashDao: UnsplashDao? = null

    private var unsplashList: List<UnsplashTable?>? = null

    private var existingJson: JSONArray? = null
    private lateinit var photosAdapter: PhotosRecyclerAdapter
    lateinit var defJsonArray: JSONArray
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        setContentView(R.layout.activity_main)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setDefaults()
        setClickListeners()
    }

    private fun setClickListeners() {


        img_search.setOnClickListener {
            val searchTxt = edt_search.text.toString()
            if (searchTxt.isNotEmpty()) {
                val myIntent = Intent(this, SearchActivity::class.java)
                myIntent.putExtra(getString(R.string.search_text), edt_search.text.toString())
                startActivity(myIntent)
            } else {
                Toast.makeText(this, "Search text cannot be blank", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setDefaults() {

        dbClient = AppDatabase.getDatabase(this@MainActivity)
        val cache = ImagesCache.getInstance()
        cache.initializeCache()
        unsplashAdapter = UnsplashRecyclerAdapter(this@MainActivity, false)
        recyclerview_photos.adapter = unsplashAdapter
        recyclerview_photos.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        unsplashViewModel = ViewModelProvider(this).get(UnsplashViewModel::class.java)
        unsplashViewModel.getAllUnsplash()?.observe(this@MainActivity,
            { _photos ->
                CoroutineScope(Dispatchers.Main).launch {
                    unsplashAdapter.updateData(requireNotNull(_photos))
                }
            })
        unsplashDao = dbClient?.unsplashDao()
        deleteData()
        PreferenceUtils.setIntPreference(
            this@MainActivity, getString(R.string.str_disp_page),
            1
        )
        PreferenceUtils.setStringPreference(
            this@MainActivity, getString(R.string.spl_resp), ""
        )
        callAPI()
        recyclerview_photos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    var pgId = PreferenceUtils.getIntPreference(
                        this@MainActivity, getString(R.string.str_disp_page)
                    )
                    PreferenceUtils.setIntPreference(
                        this@MainActivity, getString(R.string.str_disp_page), ++pgId
                    )
                    callAPI()
                }
            }
        })
    }

    private fun deleteData() {
        lifecycleScope.launch {
            unsplashDao?.deleteAll()
        }
    }

    @Suppress("DEPRECATION")
    private fun callAPI() {

        val progress: ProgressDialog = ProgressDialog(this@MainActivity)
        progress.setMessage("Downloading data......")
        progress.setCancelable(false)
        progress.show()

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val response = APIAdapter.apiClient.getUnsplashData(
                    PreferenceUtils.getIntPreference(
                        this@MainActivity, getString(R.string.str_disp_page)
                    ),
                    getString(R.string.client_key)
                )
                if (response.count() > 0) {
                    progress.dismiss()
                    unsplashViewModel.saveUnsplashData(this@MainActivity, response)
                    fetchLocalData()
//                    checkLocalData(response)
                } else {
                    progress.dismiss()
//                    Toast.makeText(
//                        this@MainActivity,
//                        "Error Occurred: ",
//                        Toast.LENGTH_LONG
//                    ).show()
                }
            } catch (e: Exception) {
                progress.dismiss()
                Toast.makeText(
                    this@MainActivity,
                    "Error Occurred: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun saveData(record: UnsplashTable) {
//        dbClient?.UnsplashDao()?.insert(record)
//        lifecycleScope.launch {
        unsplashViewModel.insert(record)
//        }
    }

    private fun fetchLocalData() {
        lifecycleScope.launch {
            unsplashList = unsplashDao?.getAll("No")
            checkData()
        }
    }

    private fun checkData() {
        if (null != unsplashList && unsplashList!!.isNotEmpty()) {
            unsplashAdapter.updateData(unsplashList as List<UnsplashTable>)
        }
        val progress = ProgressDialog(this)
        progress.setMessage("Please wait....")
        progress.show()

        val progressRunnable = Runnable {
            progress.cancel()
        }

        val handler = Handler()
        handler.postDelayed(progressRunnable, 10000)
    }

    private fun checkLocalData(response: Any) {
        val gson = Gson()
        val jsonTutsList = JSONArray(gson.toJson(response))

        var localJson = PreferenceUtils.getStringPreference(
            this@MainActivity, getString(R.string.spl_resp)
        )
        if (localJson.isNotEmpty()) {
            existingJson = JSONArray(localJson)
        }
        if (null != existingJson && existingJson!!.length() > 0) {
            for (i in 0 until jsonTutsList.length()) {
                existingJson!!.put(jsonTutsList.getJSONObject(i))
            }
            PreferenceUtils.setStringPreference(
                this@MainActivity, getString(R.string.spl_resp),
                "$existingJson"
            )
            photosAdapter.updateData(existingJson!!)
        } else {
            PreferenceUtils.setStringPreference(
                this@MainActivity, getString(R.string.spl_resp),
                "$jsonTutsList"
            )
            displayData(
                JSONArray(
                    PreferenceUtils.getStringPreference(
                        this@MainActivity, getString(R.string.spl_resp)
                    )
                )
            )
        }
    }

    private fun displayData(response: JSONArray) {
        photosAdapter = PhotosRecyclerAdapter(this@MainActivity, response, false)
        recyclerview_photos.adapter = photosAdapter
        recyclerview_photos.layoutManager = StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
    }
}