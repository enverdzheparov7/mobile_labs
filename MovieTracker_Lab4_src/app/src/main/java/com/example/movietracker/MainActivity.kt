package com.example.movietracker

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

    // Шаг 3: ActivityResultLauncher — современный способ получить результат из SecondActivity
    private val secondActivityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val returnedTitle  = data?.getStringExtra(EXTRA_RESULT_TITLE)  ?: ""
                val returnedReview = data?.getStringExtra(EXTRA_RESULT_REVIEW) ?: ""
                Log.d(TAG, "Result received: title=$returnedTitle, review=$returnedReview")
                textViewLastReview.text = "Последняя рецензия:\n\"$returnedTitle\" — $returnedReview"
                Toast.makeText(this, "Рецензия получена от SecondActivity!", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "SecondActivity returned RESULT_CANCELED")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.activity_main)

        editTextTitle      = findViewById(R.id.editTextTitle)
        editTextGenre      = findViewById(R.id.editTextGenre)
        buttonAdd          = findViewById(R.id.buttonAdd)
        buttonGoToSecond   = findViewById(R.id.buttonGoToSecond)
        buttonOpenImdb     = findViewById(R.id.buttonOpenImdb)
        textViewLastReview = findViewById(R.id.textViewLastReview)
        recyclerView       = findViewById(R.id.recyclerView)

        adapter = MovieAdapter(movieList) { movie -> openMovieDetail(movie) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        buttonAdd.setOnClickListener { addMovie() }
        buttonGoToSecond.setOnClickListener { goToSecondActivityForResult() }
        // Шаг 4: неявный Intent
        buttonOpenImdb.setOnClickListener { openImdb() }

        savedInstanceState?.let {
            editTextTitle.setText(it.getString("saved_title", ""))
            editTextGenre.setText(it.getString("saved_genre", ""))
        }
    }

    override fun onStart()   { super.onStart();   Log.d(TAG, "onStart called")   }
    override fun onResume()  { super.onResume();  Log.d(TAG, "onResume called")  }
    override fun onPause()   { super.onPause();   Log.d(TAG, "onPause called")   }
    override fun onStop()    { super.onStop();    Log.d(TAG, "onStop called")    }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy called") }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("saved_title", editTextTitle.text.toString())
        outState.putString("saved_genre", editTextGenre.text.toString())
        Log.d(TAG, "onSaveInstanceState: state saved")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        editTextTitle.setText(savedInstanceState.getString("saved_title", ""))
        editTextGenre.setText(savedInstanceState.getString("saved_genre", ""))
        Log.d(TAG, "onRestoreInstanceState: state restored")
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
        editTextTitle.setText("")
        editTextGenre.setText("")
        Log.d(TAG, "Movie added: $title ($genre)")
    }

    // Шаг 2: явный (Explicit) Intent + Шаг 3: запуск с ожиданием результата
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
        Log.d(TAG, "Opening detail for: ${movie.title}")
    }

    // Шаг 4: неявный (Implicit) Intent — система сама выбирает браузер
    private fun openImdb() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.imdb.com"))
        try {
            startActivity(intent)
            Log.d(TAG, "Implicit Intent: opening IMDb")
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Браузер не найден", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "No browser found: ${e.message}")
        }
    }
}
