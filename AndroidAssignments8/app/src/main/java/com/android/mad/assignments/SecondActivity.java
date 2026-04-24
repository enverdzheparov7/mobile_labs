package com.android.mad.assignments;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.second_activity);

        // Получаем переданные данные
        String transmittedString = getIntent().getStringExtra(TRANSMITTED_STRING);
        int transmittedInt = getIntent().getIntExtra(TRANSMITTED_INT, -1);           // -1 — значение по умолчанию
        boolean transmittedBoolean = getIntent().getBooleanExtra(TRANSMITTED_BOOLEAN, false);

        TextView textView = findViewById(R.id.second_activity_text_view);

        textView.setText(
                "These values were passed from previous screen\n\n" +
                        "transmittedString: " + transmittedString + "\n" +
                        "transmittedInt: " + transmittedInt + "\n" +
                        "transmittedBoolean: " + transmittedBoolean
        );
    }

    public static final String TRANSMITTED_STRING = "transmittedString";
    public static final String TRANSMITTED_INT    = "transmittedInt";
    public static final String TRANSMITTED_BOOLEAN = "transmittedBoolean";
}