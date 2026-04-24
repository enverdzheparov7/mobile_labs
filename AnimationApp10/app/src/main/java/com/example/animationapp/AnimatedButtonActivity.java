package com.example.animationapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

public class AnimatedButtonActivity extends Activity {

    int clickCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animated_button);

        Button animatedButton = findViewById(R.id.animatedButton);
        TextView tvClickCount = findViewById(R.id.tvClickCount);

        Animation scaleDown = AnimationUtils.loadAnimation(this, R.anim.btn_press_scale_down);
        Animation scaleUp   = AnimationUtils.loadAnimation(this, R.anim.btn_press_scale_up);

        // Touch listener для эффекта нажатия (scale down → scale up)
        animatedButton.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(scaleDown);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.startAnimation(scaleUp);
                v.performClick();
            }
            return true;
        });

        animatedButton.setOnClickListener(v -> {
            clickCount++;
            tvClickCount.setText("Нажатий: " + clickCount);
        });
    }
}
