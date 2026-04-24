package com.android.mad.assignments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.LruCache
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import android.content.Intent

class MainActivity : AppCompatActivity() {

    private lateinit var buttonNext: Button
    private lateinit var buttonPrev: Button
    private lateinit var imageView: ImageView

    private val listOfCats    = ArrayList<String>()
    private val listOfCatUrls = ArrayList<String>()
    private val getCatUrl = "https://api.thecatapi.com/v1/images/search"

    private lateinit var memoryCache: LruCache<String, Bitmap>
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMemoryCache()

        buttonNext = findViewById(R.id.buttonNext)
        buttonPrev = findViewById(R.id.buttonPrev)
        imageView  = findViewById(R.id.imageView)

        buttonNext.setOnClickListener { getNextCat() }
        buttonPrev.setOnClickListener { getPrevCat() }

        queue = Volley.newRequestQueue(this)

        val btnTask2 = findViewById<Button>(R.id.btnTask2)
        val btnTask3 = findViewById<Button>(R.id.btnTask3)
        val btnTask4 = findViewById<Button>(R.id.btnTask4)
        val btnTask5 = findViewById<Button>(R.id.btnTask5)
        val btnTask7 = findViewById<Button>(R.id.btnTask7)

        btnTask2.setOnClickListener {
            startActivity(Intent(this, Task2_NetworkBenchmarkActivity::class.java))
        }

        btnTask3.setOnClickListener {
            startActivity(Intent(this, Task3_CacheActivity::class.java))
        }

        btnTask4.setOnClickListener {
            startActivity(Intent(this, Task4_GzipActivity::class.java))
        }

        btnTask5.setOnClickListener {
            startActivity(Intent(this, Task5_ErrorHandlingActivity::class.java))
        }

        btnTask7.setOnClickListener {
            startActivity(Intent(this, Task7_StreamingActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        getNextCat()
    }

    private fun initMemoryCache() {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    private fun getNextCat() {
        val request = JsonArrayRequest(
            com.android.volley.Request.Method.GET,
            getCatUrl, null,
            { response ->
                try {
                    val id  = response.getJSONObject(0).get("id").toString()
                    val url = response.getJSONObject(0).get("url").toString()
                    listOfCats.add(id)
                    listOfCatUrls.add(url)
                    displayCatImage(url, id)
                } catch (e: Exception) {
                    Log.e("CatAPI", "Error", e)
                }
            },
            { error -> Log.e("CatAPI", "Volley error: $error") }
        )
        queue.add(request)
    }

    private fun getPrevCat() {
        if (listOfCats.size > 1) {
            listOfCats.removeAt(listOfCats.size - 1)
            listOfCatUrls.removeAt(listOfCatUrls.size - 1)
            val prevId = listOfCats[listOfCats.size - 1]
            val cached = getBitmapFromMemCache(prevId)
            if (cached != null) {
                imageView.setImageBitmap(cached)
            } else {
                displayCatImage(listOfCatUrls[listOfCatUrls.size - 1], prevId)
            }
        }
    }

    private fun displayCatImage(catUrl: String, id: String) {
        val imageRequest = ImageRequest(
            catUrl,
            { bitmap ->
                val decompressed = compressCatImage(bitmap)
                imageView.setImageBitmap(decompressed)
                addBitmapToMemoryCache(id, decompressed)
            },
            60, 60,
            ImageView.ScaleType.CENTER,
            null,
            { error -> Log.e("CatAPI", "Image error: $error") }
        )
        queue.add(imageRequest)
    }

    private fun compressCatImage(origBitmap: Bitmap): Bitmap {
        val out = ByteArrayOutputStream()
        val mb = origBitmap.byteCount / 1024 / 1024
        val quality = when {
            mb < 2 -> 80
            mb < 3 -> 70
            mb < 4 -> 40
            else   -> 30
        }
        origBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        val bytes = out.toByteArray()
        return BitmapFactory.decodeStream(ByteArrayInputStream(bytes))
    }

    fun addBitmapToMemoryCache(key: String, bitmap: Bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap)
        }
    }

    fun getBitmapFromMemCache(key: String): Bitmap? {
        return memoryCache.get(key)
    }
}