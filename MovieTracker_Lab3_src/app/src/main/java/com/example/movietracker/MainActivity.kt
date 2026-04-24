package com.example.movietracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        const val EXTRA_MOVIE_TITLE = "extra_movie_title"
        const val EXTRA_MOVIE_GENRE = "extra_movie_genre"
        const val EXTRA_MOVIE_ID    = "extra_movie_id"
    }

    private lateinit var editTextTitle: EditText
    private lateinit var editTextGenre: EditText
    private lateinit var buttonAdd: Button
    private lateinit var buttonGoToSecond: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MovieAdapter

    private val movieList = mutableListOf<Movie>()

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.activity_main)

        editTextTitle    = findViewById(R.id.editTextTitle)
        editTextGenre    = findViewById(R.id.editTextGenre)
        buttonAdd        = findViewById(R.id.buttonAdd)
        buttonGoToSecond = findViewById(R.id.buttonGoToSecond)
        recyclerView     = findViewById(R.id.recyclerView)

        adapter = MovieAdapter(movieList) { movie -> openMovieDetail(movie) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        buttonAdd.setOnClickListener { addMovie() }
        buttonGoToSecond.setOnClickListener { goToSecondActivity() }

        // Восстановление полей ввода после поворота экрана
        savedInstanceState?.let {
            editTextTitle.setText(it.getString("saved_title", ""))
            editTextGenre.setText(it.getString("saved_genre", ""))
            Log.d(TAG, "State restored from savedInstanceState")
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
    }

    // ── State saving ───────────────────────────────────────────────────────────

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

    // ── Helpers ────────────────────────────────────────────────────────────────

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

    private fun goToSecondActivity() {
        val title = editTextTitle.text.toString().trim()
        val genre = editTextGenre.text.toString().trim()
        if (title.isEmpty()) {
            Toast.makeText(this, "Введите название фильма для передачи", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, SecondActivity::class.java).apply {
            putExtra(EXTRA_MOVIE_TITLE, title)
            putExtra(EXTRA_MOVIE_GENRE, genre)
            putExtra(EXTRA_MOVIE_ID, System.currentTimeMillis())
        }
        startActivity(intent)
        Log.d(TAG, "Navigating to SecondActivity with: $title")
    }

    private fun openMovieDetail(movie: Movie) {
        val intent = Intent(this, SecondActivity::class.java).apply {
            putExtra(EXTRA_MOVIE_TITLE, movie.title)
            putExtra(EXTRA_MOVIE_GENRE, movie.genre)
            putExtra(EXTRA_MOVIE_ID, movie.id)
        }
        startActivity(intent)
        Log.d(TAG, "Opening detail for movie: ${movie.title}")
    }
}
