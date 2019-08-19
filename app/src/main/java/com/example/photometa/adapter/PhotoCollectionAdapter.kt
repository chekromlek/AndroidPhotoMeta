package com.example.photometa.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.photometa.R
import com.example.photometa.model.PhotoData
import kotlinx.android.synthetic.main.image_collection_cell.view.*

class PhotoCollectionAdapter(context: Context, photoData: List<PhotoData>?, onItemClickListener: (View) -> Unit): RecyclerView.Adapter<PhotoCellHolder>() {

    private val context: Context = context
    private val photoData: List<PhotoData>? = photoData
    private val itemClick: (View) -> Unit = onItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoCellHolder {
        val view = LayoutInflater.from(this.context).inflate(R.layout.image_collection_cell, parent, false)
        view.setOnClickListener(this.itemClick)
        return PhotoCellHolder(view)
    }

    override fun getItemCount(): Int {
        return photoData?.size ?: 0
    }

    override fun onBindViewHolder(holder: PhotoCellHolder, position: Int) {
        holder.itemView.tag = this.photoData!![position]
        Glide.with(this.context)
            .load(this.photoData!![position].imagePath)
            .centerCrop()
            .thumbnail(0.05f)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.itemView.imageView)
    }

}

class PhotoCellHolder(view: View): RecyclerView.ViewHolder(view)