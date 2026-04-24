package com.android.catapp.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.android.catapp.db.CatDatabase;
import com.android.catapp.db.CatingDao;
import com.android.catapp.model.Model;

import java.util.List;

public class Repository {
    public CatingDao catingDao;
    public LiveData<List<Model>> getAllCats;
    private CatDatabase database;

    public Repository(Application application) {
        database = CatDatabase.getInstance(application);
        catingDao = database.catingDao();
        getAllCats = catingDao.getCats();
    }

    public void insert(List<Model> cats) {
        new InsertAsyncTask(catingDao).execute(cats);
    }

    public LiveData<List<Model>> getAllCats() {
        return getAllCats;
    }

    private static class InsertAsyncTask extends AsyncTask<List<Model>, Void, Void> {
        private CatingDao catDao;

        public InsertAsyncTask(CatingDao catDao) {
            this.catDao = catDao;
        }

        @Override
        protected Void doInBackground(List<Model>... lists) {
            catDao.insert(lists[0]);
            return null;
        }
    }
}
