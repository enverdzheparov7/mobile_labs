package com.example.movietracker

import android.content.Intent
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
    private lateinit var buttonSendReview: Button
    private lateinit var buttonBack: Button

    private var movieTitle = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.activity_second)

        textViewTitle    = findViewById(R.id.textViewTitle)
        textViewGenre    = findViewById(R.id.textViewGenre)
        textViewId       = findViewById(R.id.textViewId)
        editTextReview   = findViewById(R.id.editTextReview)
        buttonSendReview = findViewById(R.id.buttonSaveReview)
        buttonBack       = findViewById(R.id.buttonBack)

        // Шаг 2: получаем данные из явного Intent
        movieTitle       = intent.getStringExtra(MainActivity.EXTRA_MOVIE_TITLE) ?: "Без названия"
        val genre        = intent.getStringExtra(MainActivity.EXTRA_MOVIE_GENRE) ?: "Жанр не указан"
        val id           = intent.getLongExtra(MainActivity.EXTRA_MOVIE_ID, 0L)

        textViewTitle.text = "Фильм: $movieTitle"
        textViewGenre.text = "Жанр: $genre"
        textViewId.text    = "ID: $id"
        Log.d(TAG, "Received: title=$movieTitle, genre=$genre, id=$id")

        savedInstanceState?.let {
            editTextReview.setText(it.getString("saved_review", ""))
        }

        // Шаг 3: кнопка "Сохранить рецензию" — возвращает данные через setResult
        buttonSendReview.setOnClickListener {
            val review = editTextReview.text.toString().trim()
            if (review.isEmpty()) {
                Toast.makeText(this, "Введите рецензию для отправки", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Формируем Intent с результатом и отправляем обратно в MainActivity
            val resultIntent = Intent().apply {
                putExtra(MainActivity.EXTRA_RESULT_TITLE,  movieTitle)
                putExtra(MainActivity.EXTRA_RESULT_REVIEW, review)
            }
            setResult(RESULT_OK, resultIntent)
            Log.d(TAG, "setResult RESULT_OK: review=$review")
            finish()  // закрываем SecondActivity, результат уходит в MainActivity
        }

        // Кнопка "Назад" — закрываем без результата
        buttonBack.setOnClickListener {
            setResult(RESULT_CANCELED)
            Log.d(TAG, "RESULT_CANCELED, finishing")
            finish()
        }
    }

    override fun onStart()   { super.onStart();   Log.d(TAG, "onStart called")   }
    override fun onResume()  { super.onResume();  Log.d(TAG, "onResume called")  }
    override fun onPause()   { super.onPause();   Log.d(TAG, "onPause called")   }
    override fun onStop()    { super.onStop();    Log.d(TAG, "onStop called")    }
    override fun onDestroy() { super.onDestroy(); Log.d(TAG, "onDestroy called") }

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
