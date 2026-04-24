package com.android.mad.assignments

// ============================================================
// ЗАДАНИЕ 5 — Обработка ошибок и управление соединениями
// Реализовано:
//   • Retry-логика (3 попытки с задержкой 1-2-4 сек)
//   • Timeout настройки (connect / read / write)
//   • Разбор типов ошибок (нет сети, таймаут, 4xx, 5xx)
//   • Для Volley: кастомный RetryPolicy
//   • Для OkHttp: кастомный Interceptor с retry
// ============================================================
// Куда добавить файл:
//   app/src/main/java/com/android/mad/assignments/Task5_ErrorHandlingActivity.kt
//
// Зависимости: только okhttp (уже добавлен)
//
// Добавить в AndroidManifest.xml:
//   <activity android:name=".Task5_ErrorHandlingActivity" android:exported="false"/>
// ============================================================

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import okhttp3.*
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class Task5_ErrorHandlingActivity : AppCompatActivity() {

    private lateinit var tvLog:           TextView
    private lateinit var btnTestOkHttp:   Button
    private lateinit var btnTestVolley:   Button
    private lateinit var btnTestTimeout:  Button
    private lateinit var btnTestBadUrl:   Button

    private val logBuilder = StringBuilder()

    // ── OkHttp с ручной retry-логикой и таймаутами ────────────────────────
    private val robustOkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)   // время на TCP-соединение
        .readTimeout(15, TimeUnit.SECONDS)      // время на чтение ответа
        .writeTimeout(10, TimeUnit.SECONDS)     // время на отправку тела
        .addInterceptor(RetryInterceptor(maxRetries = 3)) // retry через интерсептор
        .build()

    // ── OkHttp с очень коротким таймаутом (чтобы спровоцировать ошибку) ───
    private val timeoutClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MILLISECONDS)
        .readTimeout(1, TimeUnit.MILLISECONDS)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        tvLog          = TextView(this).apply { textSize = 12f }
        btnTestOkHttp  = Button(this).apply { text = "OkHttp — нормальный запрос + retry" }
        btnTestVolley  = Button(this).apply { text = "Volley — запрос + RetryPolicy" }
        btnTestTimeout = Button(this).apply { text = "OkHttp — таймаут (1ms)" }
        btnTestBadUrl  = Button(this).apply { text = "OkHttp — несуществующий хост" }

        layout.addView(btnTestOkHttp)
        layout.addView(btnTestVolley)
        layout.addView(btnTestTimeout)
        layout.addView(btnTestBadUrl)
        layout.addView(ScrollView(this).apply { addView(tvLog) })
        setContentView(layout)

        btnTestOkHttp.setOnClickListener  { testOkHttpWithRetry() }
        btnTestVolley.setOnClickListener  { testVolleyWithRetryPolicy() }
        btnTestTimeout.setOnClickListener { testOkHttpTimeout() }
        btnTestBadUrl.setOnClickListener  { testOkHttpBadHost() }
    }

    // ── Тест 1: OkHttp с retry-интерсептором ──────────────────────────────
    private fun testOkHttpWithRetry() {
        log("[OkHttp] Запрос с retry-логикой…")
        val request = okhttp3.Request.Builder()
            .url("https://jsonplaceholder.typicode.com/posts/1")
            .build()

        robustOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val code = response.code
                response.close()
                runOnUiThread {
                    log("[OkHttp] Успех! HTTP $code")
                    if (code in 400..599) {
                        log("[OkHttp] Серверная ошибка: ${describeHttpError(code)}")
                    }
                }
            }
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { log("[OkHttp] ФИНАЛЬНАЯ ОШИБКА после retry: ${classifyError(e)}") }
            }
        })
    }

    // ── Тест 2: Volley с DefaultRetryPolicy ───────────────────────────────
    private fun testVolleyWithRetryPolicy() {
        log("[Volley] Запрос с RetryPolicy (3 попытки, timeout 5s)…")

        val req = object : StringRequest(Request.Method.GET,
            "https://jsonplaceholder.typicode.com/posts/1",
            { response ->
                log("[Volley] Успех! Длина ответа: ${response.length} символов")
            },
            { error ->
                val msg = when (error) {
                    is com.android.volley.NoConnectionError    -> "Нет подключения к сети"
                    is com.android.volley.TimeoutError         -> "Таймаут запроса"
                    is com.android.volley.AuthFailureError     -> "Ошибка аутентификации (401)"
                    is com.android.volley.ServerError          -> "Ошибка сервера (5xx): ${error.networkResponse?.statusCode}"
                    is com.android.volley.ClientError          -> "Ошибка клиента (4xx): ${error.networkResponse?.statusCode}"
                    else                                       -> "Неизвестная ошибка: ${error.message}"
                }
                log("[Volley] ОШИБКА: $msg")
            }) {}

        // DefaultRetryPolicy(initialTimeoutMs, maxNumRetries, backoffMultiplier)
        req.retryPolicy = DefaultRetryPolicy(
            5000,  // начальный таймаут 5 сек
            3,     // 3 повтора
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT // умножитель = 1.0
        )

        Volley.newRequestQueue(this).add(req)
    }

    // ── Тест 3: Намеренный таймаут ────────────────────────────────────────
    private fun testOkHttpTimeout() {
        log("[OkHttp-Timeout] Запрос с таймаутом 1мс…")
        val request = okhttp3.Request.Builder()
            .url("https://jsonplaceholder.typicode.com/posts/1")
            .build()

        timeoutClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.close()
                runOnUiThread { log("[OkHttp-Timeout] Неожиданно успешно!") }
            }
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { log("[OkHttp-Timeout] Поймана ошибка: ${classifyError(e)}") }
            }
        })
    }

    // ── Тест 4: Несуществующий хост ───────────────────────────────────────
    private fun testOkHttpBadHost() {
        log("[OkHttp-DNS] Запрос на несуществующий хост…")
        val request = okhttp3.Request.Builder()
            .url("https://this-host-definitely-does-not-exist-xyz.com/api")
            .build()

        robustOkHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.close()
                runOnUiThread { log("[OkHttp-DNS] Неожиданно успешно!") }
            }
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { log("[OkHttp-DNS] Поймана ошибка: ${classifyError(e)}") }
            }
        })
    }

    // ── Классификация ошибок ──────────────────────────────────────────────
    private fun classifyError(e: IOException): String = when (e) {
        is SocketTimeoutException -> "ТАЙМАУТ — сервер не ответил вовремя"
        is UnknownHostException   -> "DNS ОШИБКА — хост не найден (нет сети или хост не существует)"
        else                      -> "СЕТЕВАЯ ОШИБКА: ${e.javaClass.simpleName} — ${e.message}"
    }

    private fun describeHttpError(code: Int): String = when (code) {
        400 -> "Bad Request — неверный запрос"
        401 -> "Unauthorized — требуется авторизация"
        403 -> "Forbidden — доступ запрещён"
        404 -> "Not Found — ресурс не найден"
        408 -> "Request Timeout — сервер ждал слишком долго"
        429 -> "Too Many Requests — превышен лимит запросов"
        500 -> "Internal Server Error — ошибка на сервере"
        503 -> "Service Unavailable — сервис недоступен"
        else -> "HTTP $code"
    }

    private fun log(msg: String) {
        runOnUiThread {
            logBuilder.appendLine(msg)
            tvLog.text = logBuilder.toString()
        }
    }
}

// ============================================================
// RetryInterceptor — OkHttp интерсептор с exponential backoff
// Повторяет запрос при IOException до maxRetries раз
// Задержки: 1с → 2с → 4с (exponential backoff)
// ============================================================
class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null

        repeat(maxRetries) { attempt ->
            try {
                val response = chain.proceed(request)

                // Не повторяем при клиентских ошибках (4xx)
                if (response.code in 400..499) return response

                // Повторяем при серверных ошибках (5xx)
                if (response.code in 500..599) {
                    response.close()
                    if (attempt < maxRetries - 1) {
                        Thread.sleep(1000L * (1 shl attempt)) // 1с, 2с, 4с
                        return@repeat // следующая итерация
                    }
                    return response
                }

                return response // успех
            } catch (e: IOException) {
                lastException = e
                android.util.Log.w("RetryInterceptor",
                    "Attempt ${attempt + 1}/$maxRetries failed: ${e.message}")

                if (attempt < maxRetries - 1) {
                    try { Thread.sleep(1000L * (1 shl attempt)) } catch (_: InterruptedException) {}
                }
            }
        }

        throw lastException ?: IOException("Max retries ($maxRetries) exceeded")
    }
}
