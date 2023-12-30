package com.example.myapplication.ui.gallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentGalleryBinding
import com.example.myapplication.R


class ImageFragment : Fragment() {
    private lateinit var imageAdapter: ImageAdapter

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    var images: ArrayList<Image> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root



        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(),3)
        recyclerView.setHasFixedSize(true)
        imageAdapter = ImageAdapter(requireContext(), images)
        recyclerView.adapter = imageAdapter

        binding.galleryBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            activityResult.launch(intent)
        }

        return root
    }

    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            if (it.data!!.clipData != null) {
                val count = it.data!!.clipData!!.itemCount
                for (index in 0 until count) {
                    val imageUri = it.data!!.clipData!!.getItemAt(index).uri
                    images.add(Image(imageUri))
                }
            }
            else {
                val imageUri = it.data!!.data
                images.add(Image(imageUri!!))
            }

            imageAdapter.notifyDataSetChanged()
        }
    }


}