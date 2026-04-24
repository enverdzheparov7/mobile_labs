package com.android.mad.assignments

// ============================================================
// ЗАДАНИЕ 7 — Streaming при сетевых запросах
// OkHttp: читаем тело ответа чанками (streaming), не грузим всё в RAM
// Volley: не поддерживает истинный streaming, показываем имитацию
//         через InputStreamVolleyRequest (кастомный Request<InputStream>)
// ============================================================
// Куда добавить файл:
//   app/src/main/java/com/android/mad/assignments/Task7_StreamingActivity.kt
//
// Зависимости: только okhttp (уже добавлен)
//
// Добавить в AndroidManifest.xml:
//   <activity android:name=".Task7_StreamingActivity" android:exported="false"/>
//
// ЗАЧЕМ нужен streaming:
//   Без него весь ответ буферизируется в RAM до передачи в callback.
//   С streaming — читаем по кусочкам: меньше пиковое потребление памяти,
//   можно начать обработку до полной загрузки (например, парсить JSON-поток).
// ============================================================

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import okhttp3.*
import okio.Buffer
import java.io.IOException
import java.io.InputStream

class Task7_StreamingActivity : AppCompatActivity() {

    private lateinit var tvLog:         TextView
    private lateinit var btnOkStream:   Button
    private lateinit var btnOkNormal:   Button
    private lateinit var btnVolleyStream: Button
    private lateinit var progressBar:   ProgressBar

    // Большой JSON — все посты (~27KB) — чтобы streaming был заметен
    private val BIG_URL = "https://jsonplaceholder.typicode.com/posts"

    private val okHttpClient = OkHttpClient()
    private val logBuilder = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        tvLog          = TextView(this).apply { textSize = 11f }
        progressBar    = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply { max = 100 }
        btnOkStream    = Button(this).apply { text = "OkHttp — STREAMING (по чанкам)" }
        btnOkNormal    = Button(this).apply { text = "OkHttp — обычный (весь в RAM)" }
        btnVolleyStream = Button(this).apply { text = "Volley — InputStream запрос" }

        layout.addView(btnOkStream)
        layout.addView(btnOkNormal)
        layout.addView(btnVolleyStream)
        layout.addView(progressBar)
        layout.addView(ScrollView(this).apply { addView(tvLog) })
        setContentView(layout)

        btnOkStream.setOnClickListener    { doOkHttpStreaming() }
        btnOkNormal.setOnClickListener    { doOkHttpNormal() }
        btnVolleyStream.setOnClickListener { doVolleyInputStream() }
    }

    // ── OkHttp STREAMING: читаем тело по чанкам ────────────────────────────
    private fun doOkHttpStreaming() {
        clearLog()
        log("[OkHttp Streaming] Начало…")
        val request = okhttp3.Request.Builder().url(BIG_URL).build()
        val startMem = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        val startTime = System.currentTimeMillis()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val source = response.body?.source() ?: return
                val buffer = Buffer()
                var totalBytes = 0L
                var chunks = 0

                // КЛЮЧЕВОЕ: читаем по 4KB за раз, не грузим всё в память
                while (!source.exhausted()) {
                    val bytesRead = source.read(buffer, 4096) // 4KB chunk
                    if (bytesRead == -1L) break
                    totalBytes += bytesRead
                    chunks++

                    // Здесь можно обрабатывать данные прямо во время загрузки
                    buffer.clear() // освобождаем буфер (в реальности — парсили бы JSON)

                    // Обновляем прогресс (примерно, т.к. не знаем Content-Length заранее)
                    val approxProgress = minOf(100, (totalBytes / 300).toInt())
                    runOnUiThread { progressBar.progress = approxProgress }
                }

                response.close()
                val elapsed = System.currentTimeMillis() - startTime
                val afterMem = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
                val memDelta = (afterMem - startMem) / 1024

                runOnUiThread {
                    progressBar.progress = 100
                    log("[OkHttp Streaming] Готово!")
                    log("  Всего байт:    $totalBytes")
                    log("  Чанков:        $chunks")
                    log("  Время:         $elapsed мс")
                    log("  Прирост памяти: ~$memDelta KB")
                    log("  (пиковая нагрузка минимальна — буфер 4KB)")
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { log("[OkHttp Streaming] ОШИБКА: $e") }
            }
        })
    }

    // ── OkHttp ОБЫЧНЫЙ: весь ответ в RAM ─────────────────────────────────
    private fun doOkHttpNormal() {
        clearLog()
        log("[OkHttp Normal] Начало — грузим весь ответ в RAM…")
        val request   = okhttp3.Request.Builder().url(BIG_URL).build()
        val startMem  = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
        val startTime = System.currentTimeMillis()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                // .string() буферизирует ВСЁ тело в String
                val body    = response.body?.string() ?: ""
                val elapsed = System.currentTimeMillis() - startTime
                response.close()
                val afterMem = Runtime.getRuntime().let { it.totalMemory() - it.freeMemory() }
                val memDelta = (afterMem - startMem) / 1024

                runOnUiThread {
                    progressBar.progress = 100
                    log("[OkHttp Normal] Готово!")
                    log("  Размер String: ${body.length} символов")
                    log("  Время:         $elapsed мс")
                    log("  Прирост памяти: ~$memDelta KB")
                    log("  (весь ответ сразу в String — выше пик памяти)")
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { log("[OkHttp Normal] ОШИБКА: $e") }
            }
        })
    }

    // ── Volley через кастомный InputStreamRequest ─────────────────────────
    // Volley не поддерживает истинный streaming, но можно получить InputStream
    private fun doVolleyInputStream() {
        clearLog()
        log("[Volley InputStream] Начало…")
        val startTime = System.currentTimeMillis()

        val req = InputStreamVolleyRequest(
            Request.Method.GET, BIG_URL,
            { inputStream ->
                // inputStream — весь ответ, но мы можем читать его кусками
                val buffer = ByteArray(4096)
                var totalBytes = 0
                var chunks = 0
                try {
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        totalBytes += read
                        chunks++
                        // Здесь: обрабатываем buffer[0..read] — парсинг, сохранение и т.д.
                    }
                } finally {
                    inputStream.close()
                }
                val elapsed = System.currentTimeMillis() - startTime
                runOnUiThread {
                    log("[Volley InputStream] Готово!")
                    log("  Всего байт: $totalBytes")
                    log("  Чанков:     $chunks")
                    log("  Время:      $elapsed мс")
                    log("  Примечание: Volley буферизирует ответ до")
                    log("  вызова коллбека — это не полный streaming!")
                }
            },
            { error -> runOnUiThread { log("[Volley InputStream] ОШИБКА: $error") } }
        )

        Volley.newRequestQueue(this).add(req)
    }

    private fun clearLog() { logBuilder.clear(); tvLog.text = ""; progressBar.progress = 0 }

    private fun log(msg: String) {
        runOnUiThread {
            logBuilder.appendLine(msg)
            tvLog.text = logBuilder.toString()
        }
    }
}

// ── Кастомный Volley Request возвращающий InputStream ─────────────────────
// Volley в parseNetworkResponse получает NetworkResponse с байтами.
// Оборачиваем их в InputStream и передаём в callback.
class InputStreamVolleyRequest(
    method: Int,
    url: String,
    private val successListener: Response.Listener<InputStream>,
    errorListener: Response.ErrorListener
) : Request<InputStream>(method, url, errorListener) {

    override fun parseNetworkResponse(response: NetworkResponse): Response<InputStream> {
        return Response.success(
            response.data.inputStream(),
            com.android.volley.toolbox.HttpHeaderParser.parseCacheHeaders(response)
        )
    }

    override fun deliverResponse(response: InputStream) {
        successListener.onResponse(response)
    }
}
