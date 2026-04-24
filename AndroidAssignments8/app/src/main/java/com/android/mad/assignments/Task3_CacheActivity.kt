package com.android.mad.assignments

// ============================================================
// ЗАДАНИЕ 3 — Кэширование HTTP-запросов
// OkHttp: DiskLruCache (файловый кэш HTTP-ответов)
// Volley: DiskBasedCache (встроенный диск-кэш)
// ============================================================
// Куда добавить файл:
//   app/src/main/java/com/android/mad/assignments/Task3_CacheActivity.kt
//
// Зависимости в build.gradle (уже нужны из Задания 2):
//   implementation 'com.squareup.okhttp3:okhttp:4.12.0'
//
// Добавить в AndroidManifest.xml:
//   <activity android:name=".Task3_CacheActivity" android:exported="false"/>
// ============================================================

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Cache
import com.android.volley.Network
import com.android.volley.Request
import com.android.volley.toolbox.*
import okhttp3.*
import java.io.File
import java.io.IOException

class Task3_CacheActivity : AppCompatActivity() {

    private val TEST_URL = "https://jsonplaceholder.typicode.com/posts/1"

    // OkHttp с DiskCache — кэшируем HTTP-ответы на диск (10 МБ)
    private lateinit var cachedOkHttpClient: OkHttpClient

    // Volley с DiskBasedCache
    private lateinit var volleyQueue: com.android.volley.RequestQueue

    private lateinit var tvLog:       TextView
    private lateinit var btnOkHttp:   Button
    private lateinit var btnVolley:   Button
    private lateinit var btnClear:    Button

    private val log = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupClients()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        tvLog = TextView(this).apply {
            textSize = 12f
            text     = "Нажмите кнопку — первый запрос идёт в сеть,\nвторой отдаётся из кэша.\n"
        }
        btnOkHttp = Button(this).apply { text = "OkHttp запрос (с DiskCache)" }
        btnVolley = Button(this).apply { text = "Volley запрос (с DiskCache)" }
        btnClear  = Button(this).apply { text = "Очистить лог" }

        layout.addView(btnOkHttp)
        layout.addView(btnVolley)
        layout.addView(btnClear)
        layout.addView(ScrollView(this).apply { addView(tvLog) })
        setContentView(layout)

        btnOkHttp.setOnClickListener { doOkHttpRequest() }
        btnVolley.setOnClickListener { doVolleyRequest() }
        btnClear.setOnClickListener  { log.clear(); tvLog.text = "" }
    }

    private fun setupClients() {
        // ── OkHttp: настраиваем кэш в папке cacheDir ──────────────────────
        val cacheDir  = File(cacheDir, "okhttp_cache")
        val cacheSize = 10L * 1024 * 1024 // 10 МБ
        val httpCache = Cache(cacheDir, cacheSize)

        cachedOkHttpClient = OkHttpClient.Builder()
            .cache(httpCache)
            // Если нет сети — разрешаем отдавать кэш до 7 дней
            .addInterceptor { chain ->
                var request = chain.request()
                request = request.newBuilder()
                    .header("Cache-Control", "public, max-age=60") // кэш 60 сек
                    .build()
                chain.proceed(request)
            }
            .build()

        // ── Volley: настраиваем DiskBasedCache вручную ────────────────────
        val volleyCacheDir = File(cacheDir, "volley_cache")
        val cache: Cache    = DiskBasedCache(volleyCacheDir, 1024 * 1024) // 1 МБ
        val network: Network = BasicNetwork(HurlStack())
        volleyQueue = com.android.volley.RequestQueue(cache, network).apply { start() }
    }

    // ── OkHttp запрос ──────────────────────────────────────────────────────
    private fun doOkHttpRequest() {
        val start   = System.currentTimeMillis()
        val request = okhttp3.Request.Builder().url(TEST_URL).build()

        cachedOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val elapsed   = System.currentTimeMillis() - start
                val fromCache = response.cacheResponse != null  // true = из кэша
                val networkUsed = response.networkResponse != null

                response.close()
                runOnUiThread {
                    appendLog(
                        "[OkHttp]\n" +
                        "  Время:       $elapsed мс\n" +
                        "  Из кэша:     $fromCache\n" +
                        "  Сеть:        $networkUsed\n" +
                        "  Кэш (байт):  ${cachedOkHttpClient.cache?.size()}\n"
                    )
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { appendLog("[OkHttp] ОШИБКА: $e\n") }
            }
        })
    }

    // ── Volley запрос ──────────────────────────────────────────────────────
    private fun doVolleyRequest() {
        val start = System.currentTimeMillis()

        val req = object : StringRequest(Request.Method.GET, TEST_URL,
            { response ->
                val elapsed = System.currentTimeMillis() - start
                // Смотрим в кэш Volley — есть ли запись по этому URL?
                val cached  = volleyQueue.cache.get(TEST_URL)
                val fromCache = cached != null && !cached.isExpired
                appendLog(
                    "[Volley]\n" +
                    "  Время:    $elapsed мс\n" +
                    "  Кэш есть: $fromCache\n" +
                    "  TTL до:   ${if (cached != null) "${cached.ttl}" else "–"}\n" +
                    "  Ответ:    ${response.take(60)}…\n"
                )
            },
            { error ->
                appendLog("[Volley] ОШИБКА: $error\n")
            }) {
            // Устанавливаем заголовки кэширования
            override fun getHeaders() = mapOf(
                "Cache-Control" to "public, max-age=60"
            )
        }

        // shouldCache = true — Volley будет кэшировать ответ на диск
        req.setShouldCache(true)
        volleyQueue.add(req)
    }

    private fun appendLog(msg: String) {
        log.appendLine(msg)
        tvLog.text = log.toString()
    }
}
