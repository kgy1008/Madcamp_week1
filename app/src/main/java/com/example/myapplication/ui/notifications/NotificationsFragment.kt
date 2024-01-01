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

        selectBtn.setOnClickListener {

            bitmap = BitmapFactory.decodeResource(resources, R.drawable.a)
            testImage.setImageBitmap(bitmap)

        }
        predBtn.setOnClickListener {

            var tensorImage = TensorImage(DataType.FLOAT32)
            tensorImage.load(bitmap)

            tensorImage = imageProcessor.process(tensorImage)

            val model = ModelUnquant.newInstance(requireContext())
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1,224,224,3),DataType.FLOAT32)
            inputFeature0.loadBuffer(tensorImage.buffer)

            val outputs = model.process(inputFeature0)
            val outputFeatureO = outputs.outputFeature0AsTensorBuffer.floatArray

            var maxIdx = 0
            outputFeatureO.forEachIndexed { index, fl ->
                if(outputFeatureO[maxIdx] < fl) {
                    maxIdx = index
                }
            }

            val labels = arrayOf("cat", "dog")
            resView.setText(labels[maxIdx])

            model.close()
        }


        return root
    }



}