package com.example.animationapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;

public class AnimatedIconActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animated_icon);

        LottieAnimationView lottieIcon = findViewById(R.id.lottieIcon);
        TextView tvIconStatus = findViewById(R.id.tvIconStatus);

        // Клик по иконке запускает анимацию и сигнализирует о действии
        lottieIcon.setOnClickListener(v -> {
            if (lottieIcon.isAnimating()) {
                lottieIcon.cancelAnimation();
                lottieIcon.setProgress(0);
                tvIconStatus.setText("Анимация остановлена");
            } else {
                lottieIcon.playAnimation();
                tvIconStatus.setText("🎉 Действие выполнено!");
            }
        });

        // По завершении анимации — сброс
        lottieIcon.addAnimatorListener(new android.animation.Animator.AnimatorListener() {
            @Override public void onAnimationStart(android.animation.Animator a) {}
            @Override public void onAnimationCancel(android.animation.Animator a) {}
            @Override public void onAnimationRepeat(android.animation.Animator a) {}
            @Override
            public void onAnimationEnd(android.animation.Animator animator) {
                tvIconStatus.setText("Готово! Нажми снова");
                lottieIcon.setProgress(0);
            }
        });
    }
}
