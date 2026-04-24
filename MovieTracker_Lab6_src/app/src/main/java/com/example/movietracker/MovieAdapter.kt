package com.example.movietracker

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MovieAdapter(
    private val movies: MutableList<Movie>,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewMovieTitle)
        val textViewGenre: TextView = itemView.findViewById(R.id.textViewMovieGenre)
        val checkBoxWatched: CheckBox = itemView.findViewById(R.id.checkBoxWatched)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.textViewTitle.text = movie.title
        holder.textViewGenre.text = if (movie.genre.isNotEmpty()) movie.genre else "жанр не указан"
        holder.checkBoxWatched.isChecked = movie.isWatched

        // Зачёркиваем название, если фильм уже просмотрен
        if (movie.isWatched) {
            holder.textViewTitle.paintFlags = holder.textViewTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            holder.textViewTitle.paintFlags = holder.textViewTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.checkBoxWatched.setOnCheckedChangeListener { _, isChecked ->
            movie.isWatched = isChecked
            notifyItemChanged(position)
        }

        holder.itemView.setOnClickListener { onItemClick(movie) }
    }

    override fun getItemCount(): Int = movies.size
}
