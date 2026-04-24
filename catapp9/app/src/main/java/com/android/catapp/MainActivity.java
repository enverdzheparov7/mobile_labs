package com.android.catapp;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.catapp.adapter.CatAdapter;
import com.android.catapp.api.CATapi;
import com.android.catapp.model.Model;
import com.android.catapp.repository.Repository;
import com.android.catapp.viewmodel.CatViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private CatAdapter catAdapter;
    private RecyclerView recyclerView;
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = new Repository(getApplication());
        List<Model> getCats = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);

        CatViewModel catViewModel = new ViewModelProvider(this).get(CatViewModel.class);

        catAdapter = new CatAdapter(this, getCats);
        makeRequest();
        catViewModel.getAllCats().observe(this, cats -> {
            catAdapter.setAllDatas(cats);
            catAdapter.notifyDataSetChanged();
            Log.d("main", "onChanged: " + cats);
        });

        recyclerView.setAdapter(catAdapter);
    }

    private void makeRequest() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.thecatapi.com/v1/images/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CATapi api = retrofit.create(CATapi.class);
        Call<List<Model>> call = api.getImgs(10);
        call.enqueue(new Callback<List<Model>>() {
            @Override
            public void onResponse(Call<List<Model>> call, Response<List<Model>> response) {
                if (response.isSuccessful()) {
                    repository.insert(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<Model>> call, Throwable t) {
                Log.d("main", "onFailure: " + t.getMessage());
            }
        });
    }
}
