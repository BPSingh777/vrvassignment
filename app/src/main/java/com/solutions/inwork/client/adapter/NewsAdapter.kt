package com.solutions.inwork.client.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.solutions.inwork.R
import com.solutions.inwork.client.dataclasses.Article
import com.squareup.picasso.Picasso


class NewsAdapter(private val articles: List<Article>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.news_card_layout, parent, false)
        return NewsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val article = articles[position]

        holder.titleTextView.text = article.title
        holder.descriptionTextView.text = article.description
        holder.newsarticle.text = article.content

        if (article.description.isNullOrEmpty()){
            holder.descriptionTextView.visibility = View.GONE
        }
        if (article.urlToImage.isNullOrEmpty()){
            holder.newsImageView.visibility = View.GONE
        }

//        holder.viewLess.setOnClickListener {
//            holder.viewArticle.visibility = View.VISIBLE
//          //  holder.layout.visibility = View.GONE
//        }

        Picasso.get().load(article.urlToImage).into(holder.newsImageView)
        holder.viewArticle.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
            holder.itemView.context.startActivity(browserIntent)

           // holder.viewArticle.visibility = View.GONE
          //  holder.layout.visibility = View.VISIBLE

        }

//        Glide.with(holder.itemView)
//            .load(article.urlToImage)
//            .placeholder(R.drawable.placeholder_image)
//            .into(holder.newsImageView)
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newsImageView: ImageView = itemView.findViewById(R.id.newsImage)
        val titleTextView: TextView = itemView.findViewById(R.id.NewsHeadline)
        val descriptionTextView: TextView = itemView.findViewById(R.id.NewsDescription)
        val newsarticle : TextView = itemView.findViewById(R.id.NewsArticle)
     //   val viewLess : Button = itemView.findViewById(R.id.newsViewless)
        val viewArticle : Button = itemView.findViewById(R.id.NewsViewMorebtn)
      //  val layout : LinearLayout = itemView.findViewById(R.id.newslayout)
    }
}

