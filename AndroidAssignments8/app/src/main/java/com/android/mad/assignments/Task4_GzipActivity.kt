package com.android.mad.assignments

// ============================================================
// ЗАДАНИЕ 4 — Влияние gzip-сжатия на производительность
// Сравниваем запрос БЕЗ gzip и С gzip через OkHttp и Volley
// Замеряем: время ответа, размер тела ответа в байтах
// ============================================================
// Куда добавить файл:
//   app/src/main/java/com/android/mad/assignments/Task4_GzipActivity.kt
//
// Зависимости: только okhttp (уже добавлен в Задании 2)
//
// Добавить в AndroidManifest.xml:
//   <activity android:name=".Task4_GzipActivity" android:exported="false"/>
//
// КАК РАБОТАЕТ gzip в HTTP:
// Клиент отправляет заголовок: Accept-Encoding: gzip
// Сервер (если поддерживает) возвращает сжатое тело + Content-Encoding: gzip
// OkHttp по умолчанию добавляет Accept-Encoding: gzip и распаковывает сам.
// Для "без gzip" явно убираем этот заголовок.
// ============================================================

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import okhttp3.*
import java.io.IOException

class Task4_GzipActivity : AppCompatActivity() {

    // Используем большой JSON-ответ чтобы разница была заметна
    // /posts возвращает ~27KB JSON → с gzip ~5KB
    private val TEST_URL = "https://jsonplaceholder.typicode.com/posts"

    private lateinit var tvResults: TextView
    private lateinit var btnRun:    Button
    private lateinit var progress:  ProgressBar

    private val results = mutableMapOf<String, String>()
    private var done = 0
    private val TOTAL = 4 // okhttp-no-gzip, okhttp-gzip, volley-no-gzip, volley-gzip

    // OkHttp БЕЗ автоматического gzip
    private val clientNoGzip = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val req = chain.request().newBuilder()
                .removeHeader("Accept-Encoding") // убираем gzip
                .build()
            chain.proceed(req)
        }
        .build()

    // OkHttp С gzip (по умолчанию — ничего не делаем)
    private val clientGzip = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }
        tvResults = TextView(this).apply { textSize = 13f; text = "Нажмите кнопку" }
        progress  = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = TOTAL }
        btnRun    = Button(this).apply { text = "Запустить тест gzip" }

        layout.addView(btnRun)
        layout.addView(progress)
        layout.addView(ScrollView(this).apply { addView(tvResults) })
        setContentView(layout)

        btnRun.setOnClickListener {
            btnRun.isEnabled = false
            results.clear()
            done = 0
            progress.progress = 0
            tvResults.text = "Идёт тест…"
            runTests()
        }
    }

    private fun runTests() {
        testOkHttpNoGzip()
        testOkHttpGzip()
        testVolleyNoGzip()
        testVolleyGzip()
    }

    // ── OkHttp БЕЗ gzip ───────────────────────────────────────────────────
    private fun testOkHttpNoGzip() {
        val req   = okhttp3.Request.Builder().url(TEST_URL).build()
        val start = System.currentTimeMillis()
        clientNoGzip.newCall(req).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val bytes   = response.body?.bytes()?.size ?: 0
                val elapsed = System.currentTimeMillis() - start
                val encoding = response.header("Content-Encoding") ?: "нет"
                record("OkHttp БЕЗ gzip", elapsed, bytes, encoding)
                response.close()
            }
            override fun onFailure(call: Call, e: IOException) = record("OkHttp БЕЗ gzip", -1, 0, "err")
        })
    }

    // ── OkHttp С gzip ─────────────────────────────────────────────────────
    private fun testOkHttpGzip() {
        val req   = okhttp3.Request.Builder().url(TEST_URL).build()
        val start = System.currentTimeMillis()
        clientGzip.newCall(req).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val bytes   = response.body?.bytes()?.size ?: 0
                val elapsed = System.currentTimeMillis() - start
                // OkHttp сам распаковывает, но заголовок Content-Encoding покажет gzip
                val encoding = response.header("Content-Encoding") ?: "нет (OkHttp распаковал)"
                record("OkHttp С gzip", elapsed, bytes, encoding)
                response.close()
            }
            override fun onFailure(call: Call, e: IOException) = record("OkHttp С gzip", -1, 0, "err")
        })
    }

    // ── Volley БЕЗ gzip ───────────────────────────────────────────────────
    private fun testVolleyNoGzip() {
        val start = System.currentTimeMillis()
        val req = object : StringRequest(Request.Method.GET, TEST_URL,
            { response ->
                record("Volley БЕЗ gzip", System.currentTimeMillis() - start,
                    response.toByteArray().size, "нет")
            },
            { record("Volley БЕЗ gzip", -1, 0, "err") }) {
            // Явно убираем Accept-Encoding
            override fun getHeaders() = mapOf("Accept-Encoding" to "identity")
        }
        Volley.newRequestQueue(this).add(req)
    }

    // ── Volley С gzip ─────────────────────────────────────────────────────
    private fun testVolleyGzip() {
        val start = System.currentTimeMillis()
        val req = object : StringRequest(Request.Method.GET, TEST_URL,
            { response ->
                record("Volley С gzip", System.currentTimeMillis() - start,
                    response.toByteArray().size, "gzip (Volley распаковал)")
            },
            { record("Volley С gzip", -1, 0, "err") }) {
            // Явно просим gzip
            override fun getHeaders() = mapOf("Accept-Encoding" to "gzip")
        }
        Volley.newRequestQueue(this).add(req)
    }

    @Synchronized
    private fun record(label: String, ms: Long, bytes: Int, encoding: String) {
        results[label] = "Время: ${ms}мс | Размер: ${bytes} байт | Encoding: $encoding"
        done++
        runOnUiThread {
            progress.progress = done
            if (done == TOTAL) {
                showResults()
                btnRun.isEnabled = true
            }
        }
    }

    private fun showResults() {
        val sb = StringBuilder()
        sb.appendLine("══════ Результаты теста gzip ══════\n")
        for ((k, v) in results) {
            sb.appendLine("▸ $k")
            sb.appendLine("  $v\n")
        }
        sb.appendLine("ВЫВОД:")
        sb.appendLine("С gzip сервер передаёт меньше байт →")
        sb.appendLine("быстрее при медленном интернете.")
        sb.appendLine("Без gzip — больше трафика, но меньше")
        sb.appendLine("нагрузки на CPU для распаковки.")
        tvResults.text = sb.toString()
    }
}
