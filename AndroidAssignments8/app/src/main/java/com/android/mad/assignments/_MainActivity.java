package com.android.mad.assignments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.LruCache;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import com.android.mad.assignments.Task2_NetworkBenchmarkActivity;

public class _MainActivity extends AppCompatActivity {

    Button buttonNext;
    Button buttonPrev;
    ImageView imageView;
    List<String> listOfCats;

    RequestQueue queue;
    String getCatUrl = "https://api.thecatapi.com/v1/images/search";

    private LruCache<String, Bitmap> memoryCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMemoryCache();

        buttonNext = findViewById(R.id.buttonNext);
        buttonPrev = findViewById(R.id.buttonPrev);
        imageView = findViewById(R.id.imageView);

        listOfCats = new ArrayList<>();

        queue = Volley.newRequestQueue(this);

        buttonNext.setOnClickListener(v -> getNextCat());
        buttonPrev.setOnClickListener(v -> getPrevCat());
        Button btnTask2 = findViewById(R.id.btnTask2);
        Button btnTask3 = findViewById(R.id.btnTask3);
        Button btnTask4 = findViewById(R.id.btnTask4);
        Button btnTask5 = findViewById(R.id.btnTask5);
        Button btnTask7 = findViewById(R.id.btnTask7);

        btnTask2.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, Task2_NetworkBenchmarkActivity.class)));

        btnTask3.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, Task3_CacheActivity.class)));

        btnTask4.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, Task4_GzipActivity.class)));

        btnTask5.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, Task5_ErrorHandlingActivity.class)));

        btnTask7.setOnClickListener(v ->
                startActivity(new android.content.Intent(this, Task7_StreamingActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        getNextCat();
    }

    private void initMemoryCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void getNextCat() {
        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(
                com.android.volley.Request.Method.GET, getCatUrl, null, response -> {
            try {
                listOfCats.add(response.getJSONObject(0).get("id").toString());
                displayCatImage(
                        response.getJSONObject(0).get("url").toString(),
                        listOfCats.get(listOfCats.size() - 1));
            } catch (JSONException e) {
                System.out.println(e);
            }
        }, error -> System.out.println(error));

        queue.add(jsonObjectRequest);
    }

    private void getPrevCat() {
        if (listOfCats.size() > 1) {
            listOfCats.remove(listOfCats.size() - 1);
            Bitmap decoded = getBitmapFromMemCache(listOfCats.get(listOfCats.size() - 1));
            imageView.setImageBitmap(decoded);
        }
    }

    private void displayCatImage(String catUrl, final String id) {
        ImageRequest imageRequest = new ImageRequest(catUrl,
                bitmap -> {
                    Bitmap decompressed = compressCatImage(bitmap);
                    imageView.setImageBitmap(decompressed);
                    addBitmapToMemoryCache(id, decompressed);
                    imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                },
                0, 0, ImageView.ScaleType.CENTER_INSIDE, Bitmap.Config.RGB_565,
                error -> System.out.println(error));

        queue.add(imageRequest);
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return memoryCache.get(key);
    }

    private Bitmap compressCatImage(Bitmap origBitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int byteCountInMB = origBitmap.getByteCount() / 1024 / 1024;

        Bitmap decompressed;
        if (byteCountInMB < 2) {
            origBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            decompressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        } else if (byteCountInMB < 3) {
            origBitmap.compress(Bitmap.CompressFormat.JPEG, 75, out);
            decompressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        } else if (byteCountInMB < 4) {
            origBitmap.compress(Bitmap.CompressFormat.JPEG, 40, out);
            decompressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        } else {
            origBitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
            decompressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        }

        return decompressed;
    }
}