package com.example.myapplication.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentGalleryBinding
import com.example.myapplication.R


class ImageFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val images = listOf<Image>(
            Image("Images 1", R.drawable.a),
            Image("Images 2", R.drawable.b),
            Image("Images 3", R.drawable.c),
            Image("Images 4", R.drawable.d),
            Image("Images 5", R.drawable.e)
        )

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ImageAdapter(requireContext(), images)

        return root
    }


}
