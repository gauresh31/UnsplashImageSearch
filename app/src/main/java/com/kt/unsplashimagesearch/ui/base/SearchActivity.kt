package com.kt.unsplashimagesearch.ui.base

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
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
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


class SearchActivity : AppCompatActivity() {
    private lateinit var context: Context
    private var searchText: String = ""
    private var existingJson: JSONArray? = null
    private lateinit var unplashAdapter: UnsplashRecyclerAdapter

    private lateinit var photosAdapter: PhotosRecyclerAdapter
    lateinit var defJsonArray: JSONArray
    private lateinit var unsplashViewModel: UnsplashViewModel
    private var unsplashDao: UnsplashDao? = null
    private var dbClient: AppDatabase? = null
    private var unsplashSearchList: List<UnsplashTable?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        setContentView(R.layout.activity_search)
        initValues()
        setDefaults()
    }

    private fun initValues() {
        val intentVal: Intent = intent
        searchText = intentVal.getStringExtra(getString(R.string.search_text)).toString()
        title = searchText
        dbClient = AppDatabase.getDatabase(this@SearchActivity)
    }

    private fun setDefaults() {
        context = this@SearchActivity
        val cache = ImagesCache.getInstance()
        cache.initializeCache()
        unplashAdapter = UnsplashRecyclerAdapter(context, true)
        recyclerview_search_photos.adapter = unplashAdapter
        recyclerview_search_photos.layoutManager =
            StaggeredGridLayoutManager(2, LinearLayout.VERTICAL)
        unsplashViewModel = ViewModelProvider(this).get(UnsplashViewModel::class.java)
        unsplashDao = dbClient?.unsplashDao()
        deleteData()
        PreferenceUtils.setIntPreference(
            this@SearchActivity, getString(R.string.str_search_disp_page),
            1
        )
        PreferenceUtils.setStringPreference(
            this@SearchActivity, getString(R.string.spl_search_resp), ""
        )
        callSearchAPI()
        recyclerview_search_photos.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    var pgId = PreferenceUtils.getIntPreference(
                        this@SearchActivity, getString(R.string.str_search_disp_page)
                    )
                    PreferenceUtils.setIntPreference(
                        this@SearchActivity, getString(R.string.str_search_disp_page), ++pgId
                    )
                    callSearchAPI()
                }
            }
        })
    }

    private fun deleteData() {
        lifecycleScope.launch {
            unsplashDao?.deleteAllSearch(context.getString(R.string.str_yes))
        }
    }

    @Suppress("DEPRECATION")
    private fun callSearchAPI() {

        val progress: ProgressDialog = ProgressDialog(context)
        progress.setMessage("Downloading data......")
        progress.setCancelable(false)
        progress.show()

        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val response = APIAdapter.apiClient.getSearchedUnsplashData(
                    PreferenceUtils.getIntPreference(
                        context, getString(R.string.str_search_disp_page)
                    ), searchText, getString(R.string.client_key)
                )
                if (response.isSuccessful && response.body() != null) {
                    progress.dismiss()
                    val resultSet = response.body()?.data

                    if (resultSet != null && resultSet.isNotEmpty()) {
                        progress.dismiss()
                        unsplashViewModel.saveSearchData(context, resultSet)
                        fetchLocalData()
                    } else {
                        progress.dismiss()
                        val myIntent = Intent(context, BlankActivity::class.java)
                        startActivity(myIntent)
                        finish()
                    }
                } else {
                    progress.dismiss()
                    progress.dismiss()
                    val myIntent = Intent(context, BlankActivity::class.java)
                    startActivity(myIntent)
                    finish()
                }
            } catch (e: Exception) {
                progress.dismiss()
                Toast.makeText(
                    this@SearchActivity,
                    "Error Occurred: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun fetchLocalData() {
        lifecycleScope.launch {
            unsplashSearchList = unsplashDao?.getAll(context.getString(R.string.str_yes))
            checkData()
        }
    }

    private fun checkData() {
        if (null != unsplashSearchList && unsplashSearchList!!.isNotEmpty()) {
            unplashAdapter.updateData(unsplashSearchList as List<UnsplashTable>)
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
        val jsonTutsList = JSONObject(gson.toJson(response))

        val localJson = PreferenceUtils.getStringPreference(
            this@SearchActivity, getString(R.string.spl_search_resp)
        )
        if (localJson.isNotEmpty()) {
            existingJson = JSONArray(localJson)
        }
        if (null != existingJson && existingJson!!.length() > 0) {
            for (i in 0 until jsonTutsList.length()) {
                existingJson!!.put(jsonTutsList.optJSONArray("results").optJSONObject(i))
            }
            PreferenceUtils.setStringPreference(
                this@SearchActivity, getString(R.string.spl_search_resp),
                "$existingJson"
            )
            photosAdapter.updateData(existingJson!!)
        } else {
            PreferenceUtils.setStringPreference(
                this@SearchActivity, getString(R.string.spl_search_resp),
                jsonTutsList.optJSONArray("results").toString()
            )
            displayData(
                JSONArray(
                    PreferenceUtils.getStringPreference(
                        this@SearchActivity, getString(R.string.spl_search_resp)
                    )
                )
            )
        }
    }

    private fun displayData(response: JSONArray) {
        photosAdapter = PhotosRecyclerAdapter(this@SearchActivity, response, true)
        recyclerview_search_photos.adapter = photosAdapter
        recyclerview_search_photos.layoutManager = GridLayoutManager(this, 2)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        R.id.download -> {
            showDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_filter, menu)

        return true
    }

    fun showDialog() {
        var dialog = Dialog(ContextThemeWrapper(this, R.style.DialogAnimation))
//        dialog.window?.setGravity(Gravity.BOTTOM)
//        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.layout_advance_search)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val dialogButtonCross: ImageView = dialog.findViewById(R.id.img_cross)
        val btnClear: Button = dialog.findViewById(R.id.btn_clear)
        val btnApply: Button = dialog.findViewById(R.id.btn_apply)

        val rdRelevance: RadioButton = dialog.findViewById(R.id.rb_relevance)
        val rdNewest: RadioButton = dialog.findViewById(R.id.rb_newest)
        val rdAnyColor: RadioButton = dialog.findViewById(R.id.rb_any_color)
        val rdBlckWhite: RadioButton = dialog.findViewById(R.id.rb_black_white)

        rdRelevance.isChecked = true
        rdAnyColor.isChecked = true

        btnClear.setOnClickListener {
            rdRelevance.isChecked = true
            rdAnyColor.isChecked = true
        }

        btnApply.setOnClickListener {
            when {
                rdNewest.isChecked && rdBlckWhite.isChecked -> {
                    filterNewBlckWhiteData()
                }
                rdNewest.isChecked -> {
                    filterDataDate()
                }
                rdBlckWhite.isChecked -> {
                    filterDataColor()
                }
                rdRelevance.isChecked -> {
                    fetchLocalData()
                }
            }
            dialog.dismiss()
        }

        dialogButtonCross.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun filterDataColor() {
        lifecycleScope.launch {
            unsplashSearchList = unsplashDao?.getColorSearch(context.getString(R.string.str_yes))
            checkData()
        }
    }

    private fun filterNewBlckWhiteData() {
        lifecycleScope.launch {
            unsplashSearchList = unsplashDao?.getColorNewSearch(context.getString(R.string.str_yes))
            checkData()
        }
    }

    private fun filterDataDate() {
        lifecycleScope.launch {
            unsplashSearchList =
                unsplashDao?.getDateSortedDescSearch(context.getString(R.string.str_yes))
            checkData()
        }
    }

    fun sortJsonArray(array: JSONArray, key: String): JSONArray? {
        val jsonsList: MutableList<JSONObject?> = ArrayList()
        try {
            for (i in 0 until array.length()) {
                jsonsList.add(array.getJSONObject(i))
            }
            jsonsList.sortWith(Comparator { p0, p1 ->
                var CompareString1 = ""
                var CompareString2: String? = ""
                try {
                    CompareString1 = p0?.getString(key).toString() //Key must be   present in JSON
                    CompareString2 = p1?.getString(key) //Key must be present in JSON
                } catch (ex: JSONException) {
                    // Json Excpetion handling
                }
                CompareString1.compareTo(CompareString2!!)
            })
        } catch (ex: JSONException) {
            // Json Excpetion handling
        }
        return JSONArray(jsonsList)
    }
}