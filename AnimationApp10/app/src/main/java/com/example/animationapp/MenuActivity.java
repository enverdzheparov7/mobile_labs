package com.example.animationapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Часть II
        Button btnBasicAnim = findViewById(R.id.btnBasicAnim);
        btnBasicAnim.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        // Часть III — Lottie
        Button btnLottie = findViewById(R.id.btnLottie);
        btnLottie.setOnClickListener(v ->
                startActivity(new Intent(this, LottieActivity.class)));

        // Задание 1
        Button btnTask1 = findViewById(R.id.btnTask1);
        btnTask1.setOnClickListener(v -> {
            Intent intent = new Intent(this, SecondActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Задание 2
        Button btnTask2 = findViewById(R.id.btnTask2);
        btnTask2.setOnClickListener(v ->
                startActivity(new Intent(this, AnimatedButtonActivity.class)));

        // Задание 3
        Button btnTask3 = findViewById(R.id.btnTask3);
        btnTask3.setOnClickListener(v ->
                startActivity(new Intent(this, RecyclerActivity.class)));

        // Задание 4
        Button btnTask4 = findViewById(R.id.btnTask4);
        btnTask4.setOnClickListener(v ->
                startActivity(new Intent(this, ProgressActivity.class)));

        // Задание 5
        Button btnTask5 = findViewById(R.id.btnTask5);
        btnTask5.setOnClickListener(v ->
                startActivity(new Intent(this, AnimatedIconActivity.class)));

        // Задание 6
        Button btnTask6 = findViewById(R.id.btnTask6);
        btnTask6.setOnClickListener(v ->
                startActivity(new Intent(this, FragmentTransitionActivity.class)));

        // Задание 7
        Button btnTask7 = findViewById(R.id.btnTask7);
        btnTask7.setOnClickListener(v ->
                startActivity(new Intent(this, AnimatedCardActivity.class)));
    }
}
