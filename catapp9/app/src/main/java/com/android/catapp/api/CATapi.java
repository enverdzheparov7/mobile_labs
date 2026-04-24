package com.android.catapp.api;

import com.android.catapp.model.Model;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CATapi {

    @GET("search")
    Call<List<Model>> getImgs(@Query("limit") int limit);
}
