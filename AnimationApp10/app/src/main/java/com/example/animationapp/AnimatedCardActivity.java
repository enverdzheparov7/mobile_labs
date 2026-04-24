package com.example.animationapp;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import androidx.cardview.widget.CardView;

public class AnimatedCardActivity extends Activity {

    boolean isShowingFront = true;
    boolean cardVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animated_card);

        CardView cardFront  = findViewById(R.id.cardFront);
        CardView cardBack   = findViewById(R.id.cardBack);
        Button btnShowCard  = findViewById(R.id.btnShowCard);

        // Устанавливаем дистанцию камеры для красивого 3D-эффекта
        float scale = getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);

        // Кнопка "Показать карточку" — анимация появления
        btnShowCard.setOnClickListener(v -> {
            if (!cardVisible) {
                cardFront.setVisibility(View.VISIBLE);
                Animation enterAnim = AnimationUtils.loadAnimation(this, R.anim.card_enter);
                cardFront.startAnimation(enterAnim);
                cardVisible = true;
                btnShowCard.setText("Скрыть карточку");
            } else {
                cardFront.setVisibility(View.GONE);
                cardBack.setVisibility(View.GONE);
                cardVisible = false;
                isShowingFront = true;
                btnShowCard.setText("Показать карточку");
            }
        });

        // Клик по лицевой стороне — переворот на обратную
        cardFront.setOnClickListener(v -> flipCard(cardFront, cardBack));

        // Клик по обратной стороне — переворот обратно
        cardBack.setOnClickListener(v -> flipCard(cardBack, cardFront));
    }

    private void flipCard(CardView outCard, CardView inCard) {
        AnimatorSet flipOut = (AnimatorSet) AnimatorInflater
                .loadAnimator(this, R.anim.card_flip_out);
        AnimatorSet flipIn  = (AnimatorSet) AnimatorInflater
                .loadAnimator(this, R.anim.card_flip_in);

        flipOut.setTarget(outCard);
        flipIn.setTarget(inCard);

        flipOut.start();
        flipIn.start();

        // Переключаем видимость в середине анимации (через 250мс)
        outCard.postDelayed(() -> {
            outCard.setVisibility(View.GONE);
            inCard.setVisibility(View.VISIBLE);
        }, 250);
    }
}
