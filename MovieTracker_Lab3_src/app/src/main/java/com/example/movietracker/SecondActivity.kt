package com.example.movietracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SecondActivity"
    }

    private lateinit var textViewTitle: TextView
    private lateinit var textViewGenre: TextView
    private lateinit var textViewId: TextView
    private lateinit var editTextReview: EditText
    private lateinit var buttonSaveReview: Button
    private lateinit var buttonBack: Button

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.activity_second)

        textViewTitle    = findViewById(R.id.textViewTitle)
        textViewGenre    = findViewById(R.id.textViewGenre)
        textViewId       = findViewById(R.id.textViewId)
        editTextReview   = findViewById(R.id.editTextReview)
        buttonSaveReview = findViewById(R.id.buttonSaveReview)
        buttonBack       = findViewById(R.id.buttonBack)

        // Получение данных из Intent
        val title = intent.getStringExtra(MainActivity.EXTRA_MOVIE_TITLE) ?: "Без названия"
        val genre = intent.getStringExtra(MainActivity.EXTRA_MOVIE_GENRE) ?: "Жанр не указан"
        val id    = intent.getLongExtra(MainActivity.EXTRA_MOVIE_ID, 0L)

        textViewTitle.text = "Фильм: $title"
        textViewGenre.text = "Жанр: $genre"
        textViewId.text    = "ID: $id"
        Log.d(TAG, "Received movie: title=$title, genre=$genre, id=$id")

        // Восстановление текста рецензии при повороте экрана
        savedInstanceState?.let {
            editTextReview.setText(it.getString("saved_review", ""))
            Log.d(TAG, "Review restored from savedInstanceState")
        }

        buttonSaveReview.setOnClickListener {
            val review = editTextReview.text.toString()
            Toast.makeText(this, "Рецензия сохранена!", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Review saved: $review")
        }

        buttonBack.setOnClickListener {
            Log.d(TAG, "Back button pressed")
            finish()
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
        outState.putString("saved_review", editTextReview.text.toString())
        Log.d(TAG, "onSaveInstanceState: review saved")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        editTextReview.setText(savedInstanceState.getString("saved_review", ""))
        Log.d(TAG, "onRestoreInstanceState: review restored")
    }
}
