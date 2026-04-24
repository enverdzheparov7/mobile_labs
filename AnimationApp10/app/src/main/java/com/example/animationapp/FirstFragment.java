package com.example.animationapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

public class FirstFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        Button btnGo = view.findViewById(R.id.btnGoToFragment2);
        btnGo.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                // enter: второй фрагмент въезжает справа
                // exit:  первый фрагмент уходит влево
                .setCustomAnimations(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left,
                    R.anim.slide_in_left,
                    R.anim.slide_out_right)
                .replace(R.id.fragmentContainer, new SecondFragment())
                .addToBackStack(null)
                .commit()
        );

        return view;
    }
}
