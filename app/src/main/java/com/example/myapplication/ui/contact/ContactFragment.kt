package com.example.myapplication.ui.contact

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentHomeBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

class ContactFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        recyclerView = binding.recyclerView // RecyclerView 연결

        // 레이아웃 매니저 설정
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val jsonString = getJsonDataFromAsset(requireContext(), "phoneNumber.json")
        val contactList = parseJson(jsonString)

        adapter = ContactAdapter(contactList) // 어댑터 설정
        recyclerView.adapter = adapter

        return root
    }


    // JSON 파일 읽기
    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            val inputStream: InputStream = context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, Charsets.UTF_8)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    // JSON 데이터 파싱
    private fun parseJson(jsonString: String?): List<Contact> {
        val contactList = mutableListOf<Contact>()
        try {
            val jsonObject = JSONObject(jsonString)
            val jsonArray = jsonObject.getJSONArray("contacts")
            for (i in 0 until jsonArray.length()) {
                val contactObject = jsonArray.getJSONObject(i)
                val name = contactObject.getString("name")
                val number = contactObject.getString("number")
                val contact = Contact(name, number)
                contactList.add(contact)

            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return contactList
    }

}

