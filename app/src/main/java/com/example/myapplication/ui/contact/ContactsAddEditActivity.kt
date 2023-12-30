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
import com.example.myapplication.databinding.ActivityContactsAddEditBinding
import java.util.*
import kotlin.collections.ArrayList

class ContactsAddEditActivity : AppCompatActivity(), View.OnClickListener, TextWatcher {

    private val binding by lazy { ActivityContactsAddEditBinding.inflate(layoutInflater) }
    private var isEditMode = false
    private var contacts: ContactsData? = null

    override fun onClick(v: View?) {
        when (v) {
            binding.btnSave -> {
                if (isEditMode) {
                    setEditContacts()
                } else {
                    setAddContacts()
                }
            }
            binding.btnCancel -> {
                finish()
            }
        }
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        updateSaveButtonState()
    }

    override fun afterTextChanged(p0: Editable?) {}

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

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

        if (isEditMode) {
            binding.layoutTitle.txtTitle.text = "연락처 편집"
            binding.editName.setText(contacts?.name)
            binding.editNumber.setText(contacts?.number)
        } else {
            binding.layoutTitle.txtTitle.text = "연락처 추가"
        }
    }

    private fun initListener() {
        binding.btnSave.setOnClickListener(this)
        binding.btnCancel.setOnClickListener(this)
        binding.editNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        binding.editNumber.addTextChangedListener(this)
        binding.editName.addTextChangedListener(this)
    }

    private fun updateSaveButtonState() {
        binding.btnSave.isEnabled = if (isEditMode) {
            binding.editName.text.isNotBlank() && binding.editNumber.text.isNotBlank()
                    && !(contacts?.number == binding.editNumber.text.toString() && contacts?.name == binding.editName.text.toString())
        } else {
            binding.editName.text.isNotBlank() && binding.editNumber.text.isNotBlank()
        }
    }

    private fun setEditContacts() {
        val list = ArrayList<ContentProviderOperation>()

        var where = "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}='${ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE}'"
        var whereArgs = arrayOf(contacts?.contactsId.toString())

        list.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI).apply {
            withSelection(where, whereArgs)
            withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, "")
            withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, binding.editName.text.toString())
        }.build())

        where = "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}='${ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE}' AND ${
            ContactsContract.CommonDataKinds
                .Phone.NUMBER
        }=?"
        whereArgs = arrayOf(contacts?.contactsId.toString(), contacts?.number.toString())

        list.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI).apply {
            withSelection(where, whereArgs)
            withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, binding.editNumber.text.toString())
        }.build())

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, list)
            Toast.makeText(this, "연락처가 수정되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "연락처 수정에 실패하였습니다.", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun setAddContacts() {
        val list = ArrayList<ContentProviderOperation>()

        list.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI).apply {
            withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
        }.build())

        list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).apply {
            withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
            withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, binding.editName.text.toString())
        }.build())

        list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI).apply {
            withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
            withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, binding.editNumber.text.toString())
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
