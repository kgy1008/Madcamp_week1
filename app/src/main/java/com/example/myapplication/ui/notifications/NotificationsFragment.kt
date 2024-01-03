package com.example.myapplication.ui.notifications

import android.app.Activity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.app.AlertDialog
import android.graphics.Color
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentNotificationsBinding
import com.example.myapplication.ml.ModelUnquant
import com.example.myapplication.ui.gallery.Image
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class NotificationsFragment : Fragment() {

    private val PERMISSION_CODE_GALLERY = 101
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectBtn: Button
    private lateinit var predBtn: Button
    private lateinit var resView: TextView
    private lateinit var testImage: ImageView

    private lateinit var bitmap: Bitmap
    var images: ArrayList<Image> = ArrayList()

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

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        selectBtn.setOnClickListener {
            checkPermissionAndSelectImage()
            resView.text = "prediction"
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
            val labels = arrayOf("개", "고양이", "곰", "사람")
            val barChart: BarChart = binding.barChartView

            val entries = ArrayList<BarEntry>()
            sortedResults.forEachIndexed { index, (_, fl) ->
                entries.add(BarEntry(index.toFloat(), fl * 100)) // 바 차트에 표시할 데이터 추가
            }

            val barDataSet = BarDataSet(entries, "Prediction Results")
            barDataSet.color = Color.parseColor("#2196F3") // 바 색상 설정

            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f

            val barData = BarData(barDataSet)
            barChart.data = barData
            barChart.invalidate() // 그래프 갱신

            model.close()
        }

        return root
    }


    private fun checkPermissionAndSelectImage() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openImageSelection()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                PERMISSION_CODE_GALLERY
            )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CODE_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelection()
                } else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES)) {
                    showPermissionAlertDialog()
                } else {
                    goSettingActivityAlertDialog()
                }
            }
        }
    }

    private fun goSettingActivityAlertDialog() {
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

    private fun openImageSelection() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        activityResult.launch(intent)
    }

    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 하나의 이미지가 선택된 경우
            val imageUri = result.data?.data
            bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
            testImage.setImageBitmap(bitmap)
            // 이미지 선택 후 배경 숨기기
            testImage.setBackgroundResource(android.R.color.transparent) // 배경을 투명하게 변경
        }
    }
}
