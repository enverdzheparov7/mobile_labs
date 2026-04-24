package com.android.catapp.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.android.catapp.model.Model;

@Database(entities = {Model.class}, version = 5, exportSchema = false)
public abstract class CatDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "Cat";

    public abstract CatingDao catingDao();

    public static volatile CatDatabase INSTANCE = null;

    public static CatDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (CatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, CatDatabase.class, DATABASE_NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
