package com.example.animationapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import com.airbnb.lottie.LottieAnimationView;

public class LottieActivity extends Activity {

    LottieAnimationView animationView;
    Button btnPlay, btnPause, btnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottie);

        animationView = findViewById(R.id.animation_view);
        btnPlay  = findViewById(R.id.btnPlay);
        btnPause = findViewById(R.id.btnPause);
        btnStop  = findViewById(R.id.btnStop);

        // Запустить анимацию
        btnPlay.setOnClickListener(v -> animationView.playAnimation());

        // Поставить на паузу
        btnPause.setOnClickListener(v -> animationView.pauseAnimation());

        // Остановить и сбросить в начало
        btnStop.setOnClickListener(v -> {
            animationView.cancelAnimation();
            animationView.setProgress(0);
        });
    }
}
