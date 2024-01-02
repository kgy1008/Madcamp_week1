package com.example.myapplication.ui.contact

import ContactsData
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentContactBinding
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo

class ContactFragment : Fragment(), View.OnClickListener {

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    private var binding: FragmentContactBinding? = null
    private var contactsAdapter: ContactsAdapter? = null
    private var contactsList = ArrayList<ContactsData>()

    private val onItemClickListener = object : ContactsAdapter.OnItemClickListener {
        override fun onItemClickListener(position: Int) {
            val intent = Intent(requireContext(), ContactsEditActivity::class.java)
                .putExtra("contactsData", contactsList[position])
            startActivity(intent)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding?.btnPermission -> {
                requestPermission()
            }
            binding?.btnAddContacts -> {
                startActivity(Intent(requireContext(), ContactsAddActivity::class.java))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactBinding.inflate(inflater, container, false)
        binding?.btnClearImage?.isVisible = false
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLayout()
        initListener()
        onCheckContactsPermission()

        // Adapter 초기화
        contactsAdapter = ContactsAdapter(contactsList, onItemClickListener)
        binding?.contactsList?.adapter = contactsAdapter


        // EditText에 텍스트 변경 감지를 위한 TextWatcher 추가
        binding?.editTextSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 필요 없는 메소드, 여기에서는 사용하지 않음
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 필요 없는 메소드, 여기에서는 사용하지 않음
            }

            override fun afterTextChanged(s: Editable?) {
                showCancelButton(!s.isNullOrEmpty())
                // EditText의 텍스트가 변경될 때마다 호출되는 부분
                filterContacts(s.toString()) // 검색어에 따라 연락처 필터링
            }
        })

        // EditText에 텍스트 변경 감지를 위한 TextWatcher 대신에 setOnEditorActionListener 사용
        binding?.editTextSearch?.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 검색 버튼이 눌렸을 때 실행되는 부분
                filterContacts(binding?.editTextSearch?.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }


        binding?.btnClearImage?.setOnClickListener {
            binding?.editTextSearch?.setText("")
        }

        binding?.editTextSearch?.setOnFocusChangeListener { _, hasFocus ->
            showCancelButton(hasFocus)
        }
        binding?.btnClearImage?.isVisible = false
    }
    override fun onResume() {
        super.onResume()
        onCheckContactsPermission()
        binding?.btnClearImage?.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onPause() {
        super.onPause()
        binding?.editTextSearch?.setText("") // 다른 Fragment로 이동 시 EditText 내용 지우기
    }

    private fun initLayout() {
        binding?.apply {
            btnPermission.setOnClickListener { requestPermission() }
            btnAddContacts.setOnClickListener(this@ContactFragment)

            editTextSearch.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showCancelButton(true)
                }
            }
        }
    }

    private fun showCancelButton(show: Boolean) {
        binding?.btnClearImage?.isVisible = show
    }

    private fun initListener() {
        binding?.btnPermission?.setOnClickListener(this)
        binding?.btnAddContacts?.setOnClickListener(this)
    }

    private fun onCheckContactsPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED

        binding?.apply {
            if (permissionGranted) {
                getContactsList()
                editTextSearch.isVisible = true
            } else {
                editTextSearch.isVisible = false
            }

            btnPermission.isVisible = !permissionGranted
            txtDescription.isVisible = !permissionGranted
            btnAddContacts.isVisible = permissionGranted
            contactsList.isVisible = permissionGranted

            if (!permissionGranted) {
                txtDescription.text = "권한을 허용하셔야 이용하실 수 있습니다."
            }
        }
    }


    private fun requestPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.READ_CONTACTS
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    getContactsList()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }


    private fun getContactsList() {
        val contacts = requireContext().contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )
        val list = ArrayList<ContactsData>()
        contacts?.use {
            while (it.moveToNext()) {
                val contactsId =
                    it.getInt(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val name =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                list.add(ContactsData(contactsId, name, number))
            }
        }
        list.sortBy { it.name }
        contacts?.close()
        if (contactsList != list) {
            contactsList = list
            setContacts()
        }
    }

    private fun setContacts() {
        contactsAdapter = ContactsAdapter(contactsList, onItemClickListener)
        binding?.contactsList?.adapter = contactsAdapter
    }

    private fun filterContacts(query: String) {
        val filteredList = ArrayList<ContactsData>()

        for (contact in contactsList) {
            if (query.isEmpty() || contact.name.contains(query, ignoreCase = true)) {
                filteredList.add(contact)
            }
        }

        contactsAdapter?.filterList(filteredList)
    }
}