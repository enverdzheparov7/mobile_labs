package com.example.animationapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class SecondActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
            // Анимация обратного перехода: текущий экран уходит вправо,
            // предыдущий возвращается слева
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }
}
