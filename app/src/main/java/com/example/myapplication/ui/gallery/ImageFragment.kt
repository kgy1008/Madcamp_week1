package com.example.myapplication.ui.gallery
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.databinding.FragmentGalleryBinding
import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class ImageFragment : Fragment() {
    private lateinit var imageAdapter: ImageAdapter
    private val PERMISSION_CODE_GALLERY = 101

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    var images: ArrayList<Image> = ArrayList()

    //checkbox
    private lateinit var radioGroup: RadioGroup
    private lateinit var check2: CheckBox
    private lateinit var check3: CheckBox

    //delete
    private lateinit var deleteButton: Button

    //for checkbox
    private lateinit var gridLayoutManager: GridLayoutManager

    //save
    private val imageViewModel: ImageViewModel by activityViewModels()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        imageViewModel.images = images
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            images = imageViewModel.images
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val recyclerView = binding.recyclerView

        //save
        images = imageViewModel.images

        //check box
        radioGroup = binding.radioGroup
        check2 = binding.check2
        check3 = binding.check3

        //initial status of grid number
        gridLayoutManager = GridLayoutManager(requireContext(),2)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.setHasFixedSize(true)
        imageAdapter = ImageAdapter(requireContext(), images)
        recyclerView.adapter = imageAdapter

        binding.galleryBtn.setOnClickListener {
            checkPermissionAndOpenGallery()
        }

        //checkbox
        check2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                check3.isChecked = false // 2가 체크되면 3은 해제
                updateLayoutManager()
            }
        }

        check3.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                check2.isChecked = false // 3이 체크되면 2는 해제
                updateLayoutManager()
            }
        }

        //initialize delete button
        deleteButton = binding.delete
        //delete button click event
        deleteButton.setOnClickListener { onDeleteButtonClick() }


        //detailed additional window
        imageAdapter.onItemClick = {
            val intent = Intent(requireContext(), DetailedActivity::class.java)
            intent.putExtra("image", it)
            startActivity(intent)
        }
        return root
    }

    private fun checkPermissionAndOpenGallery() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                PERMISSION_CODE_GALLERY
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                    showPermissionAlertDialog()
                } else {
                    goToSettingsAlertDialog()
                }
            }
        }
    }

    private fun showPermissionAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("권한 승인이 필요합니다.")
            .setMessage("사진을 선택하려면 권한이 필요합니다.")
            .setPositiveButton("허용하기") { _, _ ->
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    PERMISSION_CODE_GALLERY
                )
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

    private fun goToSettingsAlertDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("권한 승인이 필요합니다.")
            .setMessage("앨범에 접근하기 위한 권한이 필요합니다.\n권한 -> 사진 및 동영상 접근 허용")
            .setPositiveButton("허용하러 가기") { _, _ ->
                val goSettingPermission = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                goSettingPermission.data = Uri.parse("package:${requireContext().packageName}")
                startActivity(goSettingPermission)
            }
            .setNegativeButton("취소") { _, _ -> }
            .create()
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        activityResult.launch(intent)
    }

    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        //for printing repeat message one time
        var isrepeat = false

        if (it.resultCode == Activity.RESULT_OK) {
            if (it.data!!.clipData != null) {
                val count = it.data!!.clipData!!.itemCount
                for (index in 0 until count) {
                    val imageUri = it.data!!.clipData!!.getItemAt(index).uri
                    if (!isImageAlreadyAdded(imageUri)) {
                        images.add(Image(imageUri))
                    } else {
                        isrepeat = true
                    }
                }
            } else {
                val imageUri = it.data!!.data
                if (!isImageAlreadyAdded(imageUri)) {
                    images.add(Image(imageUri!!))
                } else {
                    isrepeat = true
                }
            }
            if (isrepeat) {
                showToast("중복되는 사진은 제외됐습니다")
            }

            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun isImageAlreadyAdded(newImageUri: Uri?): Boolean {
        return images.any { it.imageSrc == newImageUri }
    }
    private fun onDeleteButtonClick() {
        val selectedImages = imageAdapter.getSelectedImages()

        if (selectedImages.isNotEmpty()) {
            // 선택된 이미지가 있다면 삭제
            images.removeAll(selectedImages)
            imageAdapter.clearSelection() // 삭제 후 선택 초기화
            imageAdapter.notifyDataSetChanged()
        }
    }

    private fun updateLayoutManager() {
        val spanCount = if (check2.isChecked) 2 else if (check3.isChecked) 3 else 2
        gridLayoutManager.spanCount = spanCount
        imageAdapter.notifyDataSetChanged()
    }


}