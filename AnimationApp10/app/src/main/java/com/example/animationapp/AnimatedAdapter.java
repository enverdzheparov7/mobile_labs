package com.example.animationapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AnimatedAdapter extends RecyclerView.Adapter<AnimatedAdapter.ViewHolder> {

    private final Context context;
    private final List<String> items;
    // Запоминаем последнюю анимированную позицию — не повторяем анимацию
    private int lastAnimatedPosition = -1;

    public AnimatedAdapter(Context context, List<String> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvItemNumber.setText(String.valueOf(position + 1));
        holder.tvItemTitle.setText(items.get(position));

        // Анимация только для новых элементов при прокрутке вниз
        if (position > lastAnimatedPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_fade_in);
            holder.itemView.startAnimation(animation);
            lastAnimatedPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemNumber, tvItemTitle;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemNumber = itemView.findViewById(R.id.tvItemNumber);
            tvItemTitle  = itemView.findViewById(R.id.tvItemTitle);
        }
    }
}
