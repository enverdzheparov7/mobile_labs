package com.example.animationapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class FragmentTransitionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_transition);

        // Загружаем первый фрагмент при старте
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragmentContainer, new FirstFragment())
                .commit();
        }
    }
}
