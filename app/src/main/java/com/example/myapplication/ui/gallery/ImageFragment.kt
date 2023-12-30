package com.example.myapplication.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
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
            Image("Images 5", R.drawable.e),
            Image("Images 6", R.drawable.a),
            Image("Images 7", R.drawable.b),
            Image("Images 8", R.drawable.c),
            Image("Images 9", R.drawable.d),
            Image("Images 10", R.drawable.e),
            Image("Images 11", R.drawable.a),
            Image("Images 12", R.drawable.b)
        )

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(),3)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = ImageAdapter(requireContext(), images)

        return root
    }


}
