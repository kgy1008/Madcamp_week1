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
import android.provider.Settings
import android.widget.LinearLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentNotificationsBinding
import com.example.myapplication.ml.ModelUnquant
import com.example.myapplication.ui.gallery.Image
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class NotificationsFragment : Fragment() {

    private val PERMISSION_CODE_GALLERY = 101
    private var isImageSelected = false

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var selectBtn: Button
    private lateinit var predBtn: Button
    private lateinit var resView: TextView
    private lateinit var testImage: ImageView
    private lateinit var hideBtn: Button
    private lateinit var detailtxt: TextView
    private lateinit var llayout: LinearLayout
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
        hideBtn = binding.hide
        detailtxt = binding.detail
        llayout = binding.llayout

        //


        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        hideBtn.setOnClickListener {
            if (llayout.visibility == View.GONE) {
                llayout.visibility = View.VISIBLE
            } else {
                llayout.visibility = View.GONE
            }

            if (detailtxt.visibility == View.GONE) {
                detailtxt.visibility = View.VISIBLE
            } else {
                detailtxt.visibility = View.GONE
            }

            if (resView.text.toString().substring(0,3) == "태양인"){
                detailtxt.text = "소수의 사람만이 태양인에 속해 감별이 쉽지는 않다는 특징이 있다.\n\n" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<외형>\n"+
                        "대체적으로 태양인에 속하는 사람들은 하체 보단 상체가 발달되었으며, 머리는 큰편이다.\n\n" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<성격>\n"+
                        "태양인에 속하는 사람들은 머리가 좋으며 원만한 사회관계를 유지하지만 자존심등이 세고 의욕이 너무앞서다 보니 독선적이기도 한다.\n\n"+"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<건강>\n"+
                        "하체가 약하므로 오래 걸으면 무리가 온다. 태양인에 속하는 여자는 대체적으로 약한 자궁으로 인해 아이를 출산하기 힘들다."
            }
            else if (resView.text.toString().substring(0,3) == "태음인") {
                detailtxt.text = "인구의 반정도가 태음인에 속할정도로 보편적인 체질이다.\n\n" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<외형>\n"+
                        "태음인들은 대체적으로 골격이 크며 체격이 좋고, 상체보다 하체가 발달했다.\n" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<성격>\n"+
                        "성격은 조용조용하며 집념과 지구력이 강하지만 고집이 세며 게으르고 속마음을 잘 드러내지 않는다는 특징이 있다. 겁이 많아 속이 울렁거리는 증상이 있다.\n\n" +"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<건강>\n"+
                        "심장이 약해 심장병을 조심해야 하며 고혈압, 폐렴, 천식, 알레르기등이 발병하기 쉽다. 또한 땀이 많으며 땀을 흘려야 건강에 좋기 때문에 땀이 잘 안나온다면 큰 병으로 이어지기 쉽다. 폭음, 폭식에도 주의해야 한다"
            }
            else if (resView.text.toString().substring(0,3) == "소양인") {
                detailtxt.text = "인구의 30%정도가 소양인에 속한다.\n\n " + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<외형>\n"+
                        "소양인에 속하는 사람들은 대체로 상체에 비해 하체가 약하고 머리가 작으며 피부는 흰 편이다.\n\n" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<성격>\n"+
                        "성격은 명량하며 쾌활하나 성질이 급해 실수가 잦다. 판단력은 빠르지만 침착하지 않다. 원만한 대인관계를 유지하지만 가정생활에는 소홀하다.\n\n" + "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<건강>\n"+
                        "소양인들은 몸에 열이 많아 차가운 음식들을 좋아한다. 위장의 기능이 좋은 반면 신장의 기능은 약하므로 신장이나 방광과 관련된 질환을 조심해야한다. 생식기능 또한 약한 편이니 주의하자.\n"
            }
            else if (resView.text.toString().substring(0,3) == "소음인") {
                detailtxt.text = "인구의 20%만이 소음인에 속한다.\n\n" +"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<외형>\n"+
                        "소음인들은 상체에 비해 하체가 비만한 타입이 많다. 대체로 체구가 작으나 상체 하체 균형이 잘 맞는다.\n\n" +"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<성격>\n"+
                        "소음인들의 성격은 소극적이며 내성적이지만 사교성이 풍부하다. 머리가 좋고 판단력이 빠르며 조직적지만 자기주의적이며 질투가 심하다.\n\n" +"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t<건강>\n"+
                        "소음인에 속하는 사람들은 따뜻함 음식을 복용하고 찬음식을 피해야 소화에 도움을 준다. 신장의 기능은 좋으나 비위가 약하므로 위장병등 위와 관련된 질병에 조심해야 하며 더위에 약하기 때문에 더위를 타지않게 조심해야 한다.\n"
            }


        }

        selectBtn.setOnClickListener {
            hideBtn.visibility = View.GONE
            checkPermissionAndSelectImage()
            resView.text = "Prediction"

        }

        // 분석 시작 버튼
        predBtn.setOnClickListener {
            if (!isImageSelected) {
                showImageSelectionWarning()
            } else {

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
                var labels = resources.assets.open("labels.txt").bufferedReader().readLines()
                val resultText = buildString {

                    sortedResults.forEach { (index, fl) ->
                        val label = labels[index]
                        append("$label: %.1f%%\n".format(fl * 100))
                    }
                }
                hideBtn.visibility = View.VISIBLE
                resView.text = resultText
                model.close()
            }
        }
        return root
    }

    private fun showImageSelectionWarning() {
        AlertDialog.Builder(requireContext())
            .setTitle("이미지 미선택")
            .setMessage("분석하기 전에 이미지를 선택해주세요.")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
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
            testImage.setBackgroundResource(android.R.color.transparent)
            isImageSelected = true
        }
        else {
            // 선택된 이미지가 없는 경우 처리 (선택 사항)
            isImageSelected = false
        }

    }
}