package com.example.myapplication.ui.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemImagesBinding

class ImageAdapter(
    private val context: Context,
    private val imageList: List<Image>
) : RecyclerView.Adapter<ImageAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentImage = imageList[position]
        holder.bind(currentImage)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    class MyViewHolder(private val binding: ItemImagesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(image: Image) {
            binding.image.setImageResource(image.imageSrc)
            binding.imageTitle.text = image.title

            binding.cardView.setOnClickListener {
                Toast.makeText(binding.root.context, image.title, Toast.LENGTH_LONG).show()
            }
        }
    }
}
