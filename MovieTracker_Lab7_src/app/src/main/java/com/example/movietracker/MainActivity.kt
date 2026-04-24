package com.example.movietracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_MOVIE_TITLE   = "extra_movie_title"
        const val EXTRA_MOVIE_GENRE   = "extra_movie_genre"
        const val EXTRA_MOVIE_ID      = "extra_movie_id"
        const val EXTRA_RESULT_REVIEW = "extra_result_review"
        const val EXTRA_RESULT_TITLE  = "extra_result_title"

        const val PREF_NAME    = "settings"
        const val KEY_DARK_MODE = "dark_mode"
    }

    private lateinit var editTextTitle: EditText
    private lateinit var editTextGenre: EditText
    private lateinit var buttonAdd: Button
    private lateinit var buttonGoToSecond: Button
    private lateinit var buttonOpenImdb: Button
    private lateinit var textViewLastReview: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MovieAdapter

    private val movieList = mutableListOf<Movie>()

    private var boundService: BoundService? = null

    // Шаг 4: ServiceConnection для BoundService
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BoundService.LocalBinder
            boundService = binder.service
            val data = boundService?.data
            Toast.makeText(this@MainActivity, data, Toast.LENGTH_SHORT).show()
            Log.d(TAG, "BoundService connected: $data")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
            Log.d(TAG, "BoundService disconnected")
        }
    }

    private val secondActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val returnedTitle  = data?.getStringExtra(EXTRA_RESULT_TITLE)  ?: ""
                val returnedReview = data?.getStringExtra(EXTRA_RESULT_REVIEW) ?: ""
                Log.d(TAG, "Result received: title=$returnedTitle, review=$returnedReview")
                textViewLastReview.text = "Последняя рецензия:\n\"$returnedTitle\" — $returnedReview"
                Toast.makeText(this, "Рецензия получена!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "SecondActivity returned RESULT_CANCELED")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.activity_main)

        initViews()
        findViewById<Button>(R.id.buttonToggleTheme).setOnClickListener { toggleDarkMode() }
        setupRecyclerView()

        buttonAdd.setOnClickListener { addMovie() }
        buttonGoToSecond.setOnClickListener { goToSecondActivityForResult() }
        buttonOpenImdb.setOnClickListener { openImdb() }

        savedInstanceState?.let {
            editTextTitle.setText(it.getString("saved_title", ""))
            editTextGenre.setText(it.getString("saved_genre", ""))
        }

        // Шаг 2: запуск обычного сервиса
        val serviceIntent = Intent(this, MyService::class.java)
        startService(serviceIntent)

        // Шаг 3: канал уведомлений — только на Android 8.0+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
        val foregroundIntent = Intent(this, ForegroundMyService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(foregroundIntent) // API 26+ требует именно startForegroundService
        } else {
            startService(foregroundIntent)
        }

        // Шаг 4: привязка к BoundService
        val intent = Intent(this, BoundService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean(KEY_DARK_MODE, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun initViews() {
        editTextTitle      = findViewById(R.id.editTextTitle)
        editTextGenre      = findViewById(R.id.editTextGenre)
        buttonAdd          = findViewById(R.id.buttonAdd)
        buttonGoToSecond   = findViewById(R.id.buttonGoToSecond)
        buttonOpenImdb     = findViewById(R.id.buttonOpenImdb)
        textViewLastReview = findViewById(R.id.textViewLastReview)
        recyclerView       = findViewById(R.id.recyclerView)
    }

    private fun setupRecyclerView() {
        adapter = MovieAdapter(movieList) { movie -> openMovieDetail(movie) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun addMovie() {
        val title = editTextTitle.text.toString().trim()
        val genre = editTextGenre.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название фильма", Toast.LENGTH_SHORT).show()
            return
        }
        val movie = Movie(id = System.currentTimeMillis(), title = title, genre = genre)
        movieList.add(movie)
        adapter.notifyItemInserted(movieList.size - 1)
        editTextTitle.text.clear()
        editTextGenre.text.clear()
        Log.d(TAG, "Movie added: $title ($genre)")
    }

    private fun goToSecondActivityForResult() {
        val title = editTextTitle.text.toString().trim()
        val genre = editTextGenre.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название фильма", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, SecondActivity::class.java).apply {
            putExtra(EXTRA_MOVIE_TITLE, title)
            putExtra(EXTRA_MOVIE_GENRE, genre)
            putExtra(EXTRA_MOVIE_ID, System.currentTimeMillis())
        }
        secondActivityLauncher.launch(intent)
        Log.d(TAG, "Launched SecondActivity for result: $title")
    }

    private fun openMovieDetail(movie: Movie) {
        val intent = Intent(this, SecondActivity::class.java).apply {
            putExtra(EXTRA_MOVIE_TITLE, movie.title)
            putExtra(EXTRA_MOVIE_GENRE, movie.genre)
            putExtra(EXTRA_MOVIE_ID, movie.id)
        }
        secondActivityLauncher.launch(intent)
    }

    private fun openImdb() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.imdb.com"))
        try {
            startActivity(intent)
            Log.d(TAG, "Opening IMDb in browser")
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Браузер не найден", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "No browser found", e)
        }
    }

    fun toggleDarkMode() {
        val prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
        val isCurrentlyDark = prefs.getBoolean(KEY_DARK_MODE, false)
        val newMode = !isCurrentlyDark
        prefs.edit().putBoolean(KEY_DARK_MODE, newMode).apply()
        AppCompatDelegate.setDefaultNightMode(
            if (newMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }

    override fun onStart()   { super.onStart();   Log.d(TAG, "onStart called") }
    override fun onResume()  { super.onResume();  Log.d(TAG, "onResume called") }
    override fun onPause()   { super.onPause();   Log.d(TAG, "onPause called") }
    override fun onStop()    { super.onStop();    Log.d(TAG, "onStop called") }
    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
        Log.d(TAG, "onDestroy called")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("saved_title", editTextTitle.text.toString())
        outState.putString("saved_genre", editTextGenre.text.toString())
    }
}