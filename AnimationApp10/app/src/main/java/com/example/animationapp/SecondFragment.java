package com.example.animationapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

public class SecondFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        Button btnBack = view.findViewById(R.id.btnBackToFragment1);
        btnBack.setOnClickListener(v ->
            requireActivity().getSupportFragmentManager().popBackStack()
        );

        return view;
    }
}
