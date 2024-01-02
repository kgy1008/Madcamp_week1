package com.example.myapplication.ui.notifications
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
    lateinit var selectBtn: Button
    lateinit var predBtn: Button
    lateinit var resView: TextView
    lateinit var testImage: ImageView
    lateinit var bitmap: Bitmap


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        // 뷰바인딩을 사용하여 뷰 참조
        val selectBtn: Button = binding.selectBtn
        val predBtn: Button = binding.predictBtn
        val resView: TextView = binding.resView
        val testImage: ImageView = binding.testImage

        var imageProcessor = ImageProcessor.Builder()
            //.add(NormalizeOp(0.0f, 255.0f))
            //.add(TransformToGrayscaleOp())
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        //사진 선택 버튼
        selectBtn.setOnClickListener {
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.b)
            testImage.setImageBitmap(bitmap)
            //val intent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            //activityResult.launch(intent)
        }

        //분석 시작 버튼
        predBtn.setOnClickListener {

            //이미지 전처리 과정
            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)

            tensorImage = imageProcessor.process(tensorImage)

            val model = ModelUnquant.newInstance(requireContext())
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
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

    /*private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {

        if(it.resultCode == Activity.RESULT_OK && it.data != null) {
            val extras = it.data!!.extras
            bitmap = extras?.get("data") as Bitmap
            testImage.setImageBitmap(bitmap)
        }
    }*/



}