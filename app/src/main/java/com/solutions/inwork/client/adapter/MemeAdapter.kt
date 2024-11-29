package com.solutions.inwork.client.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.R
import com.solutions.inwork.client.dataclasses.Meme
import com.solutions.inwork.client.dataclasses.MemeData
import com.squareup.picasso.Picasso

class MemeAdapter(private val MemeList : List<Meme>): RecyclerView.Adapter<MemeAdapter.ScreenViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScreenViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.news_card_layout,parent,false)
        return  ScreenViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ScreenViewHolder, position: Int) {

        val list = MemeList[position]

        holder.titleTextView.text = "Title : ${list.title}"
        Picasso.get().load(list.url).into(holder.newsImageView)

    }

    override fun getItemCount(): Int {
        return MemeList.size
    }

    class ScreenViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val newsImageView: ImageView = itemView.findViewById(R.id.newsImage)
        val titleTextView: TextView = itemView.findViewById(R.id.NewsHeadline)
        val descriptionTextView: TextView = itemView.findViewById(R.id.NewsDescription)

    }
}