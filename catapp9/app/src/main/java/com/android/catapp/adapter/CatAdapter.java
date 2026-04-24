package com.android.catapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.catapp.R;
import com.android.catapp.model.Model;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CatAdapter extends RecyclerView.Adapter<CatAdapter.CatViewHolder> {
    private final Context context;
    private List<Model> cats;

    public CatAdapter(Context context, List<Model> cats) {
        this.context = context;
        this.cats = cats;
    }

    @NonNull
    @Override
    public CatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CatViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pic, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull CatViewHolder holder, int position) {
        Model cat = cats.get(position);
        Picasso.get()
                .load(cat.getUrl())
                .resize(400, 400)       // уменьшаем до нужного размера перед декодированием
                .centerCrop()           // обрезаем по центру, не растягиваем
                .placeholder(android.R.drawable.ic_menu_gallery) // заглушка пока грузится
                .error(android.R.drawable.ic_menu_report_image)  // если ошибка загрузки
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return cats.size();
    }

    public void setAllDatas(List<Model> cats) {
        this.cats = cats;
        notifyDataSetChanged();
    }

    public static class CatViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;

        public CatViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itempic);
        }
    }
}
