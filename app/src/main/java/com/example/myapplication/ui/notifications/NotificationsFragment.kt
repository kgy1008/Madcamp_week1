package com.example.myapplication.ui.notifications

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentNotificationsBinding
import com.example.myapplication.ml.ModelUnquant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectBtn: Button
    private lateinit var predBtn: Button
    private lateinit var permBtn: Button
    private lateinit var resView: TextView
    private lateinit var testImage: ImageView
    private lateinit var myLinearLayout: LinearLayout

    private var isLinearLayoutVisible = false
    private lateinit var bitmap: Bitmap

    private val activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data != null) {
                val extras = it.data!!.extras
                bitmap = extras?.get("data") as Bitmap
                binding.testImage.setImageBitmap(bitmap)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        // 뷰바인딩을 사용하여 뷰 참조
        selectBtn = binding.selectBtn
        predBtn = binding.predictBtn
        resView = binding.resView
        testImage = binding.testImage
        permBtn = root.findViewById(R.id.permButton)
        myLinearLayout = root.findViewById(R.id.llayout)

        // 복원
        if (savedInstanceState != null) {
            isLinearLayoutVisible =
                savedInstanceState.getBoolean("isLinearLayoutVisible", false)
            updateLinearLayoutVisibility()
        }



        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        permBtn.setOnClickListener {
            isLinearLayoutVisible = true
            updateLinearLayoutVisibility()
        }

        // 사진 선택 버튼
        selectBtn.setOnClickListener {
            val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            activityResult.launch(intent)
        }

        // 분석 시작 버튼
        predBtn.setOnClickListener {

            // 이미지 전처리 과정
            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)

            tensorImage = imageProcessor.process(tensorImage)

            val model = ModelUnquant.newInstance(requireContext())
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(tensorImage.buffer)

            val outputs = model.process(inputFeature0)
            val outputFeatureO = outputs.outputFeature0AsTensorBuffer.floatArray

            // 결과를 정렬
            val sortedResults = outputFeatureO.mapIndexed { index, fl -> index to fl }
                .sortedByDescending { (_, fl) -> fl }

            // 정렬된 결과를 출력
            val labels = arrayOf("cat", "dog", "bear")
            val resultText = buildString {
                sortedResults.forEach { (index, fl) ->
                    val label = labels[index]
                    append("$label: %.1f%%\n".format(fl * 100))
                }
            }

            resView.text = resultText
            model.close()
        }

        return root
    }

    private fun updateLinearLayoutVisibility() {
        myLinearLayout.visibility = if (isLinearLayoutVisible) View.VISIBLE else View.GONE
        permBtn.visibility = if (isLinearLayoutVisible) View.GONE else View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // 현재 LinearLayout의 가시성 상태를 저장
        outState.putBoolean("isLinearLayoutVisible", isLinearLayoutVisible)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}