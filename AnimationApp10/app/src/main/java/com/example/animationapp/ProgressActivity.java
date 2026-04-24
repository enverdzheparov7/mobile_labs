package com.example.animationapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;

public class ProgressActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        LottieAnimationView lottieProgress = findViewById(R.id.lottieProgress);
        TextView tvStatus   = findViewById(R.id.tvStatus);
        Button btnStartLoad = findViewById(R.id.btnStartLoad);

        btnStartLoad.setOnClickListener(v -> {
            // Показываем Lottie spinner и начинаем "загрузку"
            lottieProgress.setVisibility(View.VISIBLE);
            lottieProgress.playAnimation();
            tvStatus.setText("Загрузка данных...");
            btnStartLoad.setEnabled(false);

            // Через 3 секунды симулируем завершение загрузки
            new Handler().postDelayed(() -> {
                lottieProgress.setVisibility(View.GONE);
                lottieProgress.cancelAnimation();
                tvStatus.setText("✅ Загрузка завершена!");
                btnStartLoad.setEnabled(true);
            }, 3000);
        });
    }
}
