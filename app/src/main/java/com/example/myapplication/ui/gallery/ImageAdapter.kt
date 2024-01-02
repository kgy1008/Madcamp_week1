package com.example.myapplication.ui.gallery

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemImagesBinding
import com.example.myapplication.R

class ImageAdapter(
    private val context: Context,
    private val imageList: ArrayList<Image>
) : RecyclerView.Adapter<ImageAdapter.MyViewHolder>() {

    private val selectedImages = mutableListOf<Image>() // 선택된 이미지를 추적하는 리스트 추가

    var onItemClick: ((Image) -> Unit)? = null

    // 선택된 이미지를 반환하는 함수
    fun getSelectedImages(): List<Image> {
        return selectedImages.toList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemImagesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentImage = imageList[position]
        val isSelected = selectedImages.contains(currentImage)
        holder.bind(currentImage, isSelected)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(currentImage)
        }

        // 이미지를 선택 및 해제할 때마다 selectedImages 리스트 업데이트
        // 길게 클릭해서 지워질 사진 선택
        holder.itemView.setOnLongClickListener {
            toggleSelection(currentImage)
            true
        }
    }


    override fun getItemCount(): Int {
        return imageList.size
    }

    private fun toggleSelection(image: Image) {
        if (selectedImages.contains(image)) {
            selectedImages.remove(image)
        } else {
            selectedImages.add(image)
        }
        notifyDataSetChanged()
    }
    // 모든 이미지의 선택 상태를 초기화하는 메서드 추가
    fun clearSelection() {
        selectedImages.clear()
        notifyDataSetChanged()
    }

    class MyViewHolder(private val binding: ItemImagesBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(image: Image, isSelected: Boolean) {
            // 이미지를 어둡게 만들기 위한 ColorMatrix 생성
            val colorMatrix = ColorMatrix().apply {
                if (isSelected) {
                    setScale(0.7f, 0.7f, 0.7f, 1f) // 색을 어둡게 만들기 위한 값 설정
                } else {
                    reset() // 선택이 해제되면 적용된 필터를 제거
                }
            }

            // 선택 여부에 따라 이미지를 어둡게 하거나 복원
            val filter = ColorMatrixColorFilter(colorMatrix)
            binding.image.colorFilter = filter
            binding.image.setImageURI(image.imageSrc)


        }

    }

}
