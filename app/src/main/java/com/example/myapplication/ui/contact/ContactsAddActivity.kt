package com.example.myapplication.ui.contact

import ContactsData
import android.content.ContentProviderOperation
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityContactsAddBinding
import kotlin.collections.ArrayList

class ContactsAddActivity : AppCompatActivity(), View.OnClickListener {

    private val binding by lazy { ActivityContactsAddBinding.inflate(layoutInflater) }
    private var isEditMode = false
    private var contacts: ContactsData? = null

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSave -> {
                checkAndSaveContact()
            }
            binding.btnCancel -> {
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
        initLayout()
        initListener()
    }

    private fun initData() {
        intent.getParcelableExtra<ContactsData>("contactsData")?.let {
            contacts = it
            isEditMode = true
        }
    }

    private fun initLayout() {
        setContentView(binding.root)
    }

    private fun initListener() {
        binding.btnSave.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.addNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        binding.addName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSaveButtonState()
            }
        })

        binding.addNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateSaveButtonState()
            }
        })
        updateSaveButtonState()
    }

    private fun updateSaveButtonState() {
        val name = binding.addName.text.toString().trim()
        val number = binding.addNumber.text.toString().trim()
        val enableSaveButton = name.isNotEmpty() && number.isNotEmpty()
        binding.btnSave.isEnabled = enableSaveButton
    }

    private fun checkAndSaveContact() {
        val name = binding.addName.text.toString().trim()
        val number = binding.addNumber.text.toString().trim()

        if (name.isEmpty() || number.isEmpty()) {
            Toast.makeText(this, "이름과 번호를 입력해주세요", Toast.LENGTH_SHORT).show()
        } else {
            saveContact(name, number) // setAddContacts() 호출을 제거하고 saveContact()로 수정
        }
    }

    private fun saveContact(name: String, number: String) {
        val list = ArrayList<ContentProviderOperation>()

        list.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI).apply {
            withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
        }.build())

        list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).apply {
            withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name) // 수정된 부분
        }.build())

        list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).apply {
            withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
            withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
        }.build())

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, list)
            Toast.makeText(this, "연락처가 생성되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "연락처 생성에 실패하였습니다.", Toast.LENGTH_SHORT).show()
        }

        finish()
    }
}