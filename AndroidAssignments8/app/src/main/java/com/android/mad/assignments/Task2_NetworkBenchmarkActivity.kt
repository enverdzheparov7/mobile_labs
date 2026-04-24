package com.android.mad.assignments

// ============================================================
// ЗАДАНИЕ 2 — Сравнительный анализ OkHttp vs Volley
// Типы запросов: GET, POST, PUT
// Замеряем время выполнения каждого запроса в мс
// ============================================================
// Куда добавить файл:
//   app/src/main/java/com/android/mad/assignments/Task2_NetworkBenchmarkActivity.kt
//
// Что добавить в build.gradle (app):
//   implementation 'com.squareup.okhttp3:okhttp:4.12.0'
//   implementation 'com.google.code.gson:gson:2.10.1'
//
// Что добавить в AndroidManifest.xml (внутри <application>):
//   <activity android:name=".Task2_NetworkBenchmarkActivity" android:exported="false"/>
//
// Как запустить из MainActivity — добавить кнопку и:
//   startActivity(Intent(this, Task2_NetworkBenchmarkActivity::class.java))
// ============================================================

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class Task2_NetworkBenchmarkActivity : AppCompatActivity() {

    // Публичное тестовое API (jsonplaceholder — бесплатно, без ключа)
    private val BASE_URL = "https://jsonplaceholder.typicode.com"

    private lateinit var tvResults: TextView
    private lateinit var btnRun:    Button
    private lateinit var progress:  ProgressBar

    // OkHttp — создаём один раз, переиспользуем (как рекомендует документация)
    private val okHttpClient = OkHttpClient()

    // Результаты замеров: название → время в мс
    private val results = mutableMapOf<String, Long>()
    private var completedCount = 0
    private val TOTAL_TESTS = 6 // 3 типа × 2 библиотеки

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Простой layout — создаём программно
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        tvResults = TextView(this).apply {
            text = "Нажмите «Запустить тесты»"
            textSize = 14f
        }
        progress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = TOTAL_TESTS
            progress = 0
        }
        btnRun = Button(this).apply { text = "Запустить тесты (GET / POST / PUT)" }

        layout.addView(btnRun)
        layout.addView(progress)
        layout.addView(tvResults)
        setContentView(layout)

        btnRun.setOnClickListener {
            btnRun.isEnabled = false
            results.clear()
            completedCount = 0
            progress.progress = 0
            tvResults.text = "Выполняется…"
            runAllBenchmarks()
        }
    }

    private fun runAllBenchmarks() {
        // --- Volley ---
        benchmarkVolleyGet()
        benchmarkVolleyPost()
        benchmarkVolleyPut()

        // --- OkHttp ---
        benchmarkOkHttpGet()
        benchmarkOkHttpPost()
        benchmarkOkHttpPut()
    }

    // ── VOLLEY GET ─────────────────────────────────────────────────────────
    private fun benchmarkVolleyGet() {
        val url = "$BASE_URL/posts/1"
        val start = System.currentTimeMillis()

        val req = JsonObjectRequest(Request.Method.GET, url, null,
            { _ ->
                recordResult("Volley GET", System.currentTimeMillis() - start)
            },
            { error ->
                recordResult("Volley GET", -1L)
                log("Volley GET error: $error")
            })

        Volley.newRequestQueue(this).add(req)
    }

    // ── VOLLEY POST ────────────────────────────────────────────────────────
    private fun benchmarkVolleyPost() {
        val url = "$BASE_URL/posts"
        val body = JSONObject().apply {
            put("title", "test post")
            put("body", "benchmark body content")
            put("userId", 1)
        }
        val start = System.currentTimeMillis()

        val req = JsonObjectRequest(Request.Method.POST, url, body,
            { _ ->
                recordResult("Volley POST", System.currentTimeMillis() - start)
            },
            { error ->
                recordResult("Volley POST", -1L)
                log("Volley POST error: $error")
            })

        Volley.newRequestQueue(this).add(req)
    }

    // ── VOLLEY PUT ─────────────────────────────────────────────────────────
    private fun benchmarkVolleyPut() {
        val url = "$BASE_URL/posts/1"
        val body = JSONObject().apply {
            put("id", 1)
            put("title", "updated title")
            put("body", "updated body")
            put("userId", 1)
        }
        val start = System.currentTimeMillis()

        val req = JsonObjectRequest(Request.Method.PUT, url, body,
            { _ ->
                recordResult("Volley PUT", System.currentTimeMillis() - start)
            },
            { error ->
                recordResult("Volley PUT", -1L)
                log("Volley PUT error: $error")
            })

        Volley.newRequestQueue(this).add(req)
    }

    // ── OKHTTP GET ─────────────────────────────────────────────────────────
    private fun benchmarkOkHttpGet() {
        val request = okhttp3.Request.Builder()
            .url("$BASE_URL/posts/1")
            .get()
            .build()

        val start = System.currentTimeMillis()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.close()
                recordResult("OkHttp GET", System.currentTimeMillis() - start)
            }
            override fun onFailure(call: Call, e: IOException) {
                recordResult("OkHttp GET", -1L)
                log("OkHttp GET error: $e")
            }
        })
    }

    // ── OKHTTP POST ────────────────────────────────────────────────────────
    private fun benchmarkOkHttpPost() {
        val json = """{"title":"test post","body":"benchmark body","userId":1}"""
        val body = json.toRequestBody("application/json".toMediaType())

        val request = okhttp3.Request.Builder()
            .url("$BASE_URL/posts")
            .post(body)
            .build()

        val start = System.currentTimeMillis()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.close()
                recordResult("OkHttp POST", System.currentTimeMillis() - start)
            }
            override fun onFailure(call: Call, e: IOException) {
                recordResult("OkHttp POST", -1L)
                log("OkHttp POST error: $e")
            }
        })
    }

    // ── OKHTTP PUT ─────────────────────────────────────────────────────────
    private fun benchmarkOkHttpPut() {
        val json = """{"id":1,"title":"updated","body":"updated body","userId":1}"""
        val body = json.toRequestBody("application/json".toMediaType())

        val request = okhttp3.Request.Builder()
            .url("$BASE_URL/posts/1")
            .put(body)
            .build()

        val start = System.currentTimeMillis()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.close()
                recordResult("OkHttp PUT", System.currentTimeMillis() - start)
            }
            override fun onFailure(call: Call, e: IOException) {
                recordResult("OkHttp PUT", -1L)
                log("OkHttp PUT error: $e")
            }
        })
    }

    // ── Запись результата и отображение итогов ─────────────────────────────
    @Synchronized
    private fun recordResult(label: String, timeMs: Long) {
        results[label] = timeMs
        completedCount++

        runOnUiThread {
            progress.progress = completedCount

            if (completedCount == TOTAL_TESTS) {
                showResults()
                btnRun.isEnabled = true
            }
        }
    }

    private fun showResults() {
        val sb = StringBuilder()
        sb.appendLine("═══════ Результаты замеров ═══════\n")

        val groups = listOf("GET", "POST", "PUT")
        for (method in groups) {
            sb.appendLine("── $method ──")
            val volley = results["Volley $method"] ?: -1
            val okhttp = results["OkHttp $method"] ?: -1
            sb.appendLine("  Volley : ${if (volley >= 0) "${volley} мс" else "ОШИБКА"}")
            sb.appendLine("  OkHttp : ${if (okhttp >= 0) "${okhttp} мс" else "ОШИБКА"}")
            if (volley > 0 && okhttp > 0) {
                val faster = if (okhttp < volley) "OkHttp" else "Volley"
                val diff   = Math.abs(volley - okhttp)
                sb.appendLine("  Быстрее: $faster (на $diff мс)")
            }
            sb.appendLine()
        }

        sb.appendLine("Примечание: первый запуск может быть")
        sb.appendLine("медленнее из-за DNS-кэша и TCP handshake.")
        tvResults.text = sb.toString()
    }

    private fun log(msg: String) = android.util.Log.d("Benchmark", msg)
}
