package com.android.catapp.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.android.catapp.model.Model;

import java.util.List;

@Dao
public interface CatingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<Model> cats);

    @Query("SELECT DISTINCT * FROM cating")
    LiveData<List<Model>> getCats();

    @Query("DELETE FROM cating")
    void deleteAll();
}
