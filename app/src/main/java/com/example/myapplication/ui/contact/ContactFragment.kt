package com.example.myapplication.ui.contact

import ContactsData
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentContactBinding
import android.provider.Settings

class ContactFragment : Fragment() {
    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    private var binding: FragmentContactBinding? = null
    private var contactsAdapter: ContactsAdapter? = null
    private var contactsList = ArrayList<ContactsData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLayout()
        initListener()
        onCheckContactsPermission()
    }

    override fun onResume() {
        super.onResume()
        onCheckContactsPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initLayout() {
        binding?.apply {
            includeTitle.txtTitle.text = "주소록"
            btnPermission.setOnClickListener { requestPermission() }
        }
    }

    private fun initListener() {
        binding?.apply {
            btnPermission.setOnClickListener { requestPermission() }
        }
    }

    private fun  onCheckContactsPermission() {
        val readContactsPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        binding?.apply {
            btnPermission.isVisible = !readContactsPermissionGranted
            txtDescription.isVisible = !readContactsPermissionGranted
            contactsList.isVisible = readContactsPermissionGranted

            if (!readContactsPermissionGranted) {
                txtDescription.text = "연락처를 읽기 위해 권한이 필요합니다."
            } else {
                getContactsList()
            }
        }
    }


    private fun requestPermission() {
        requestPermissions(
            arrayOf(
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되었을 때의 동작
                onCheckContactsPermission()
            } else {
                // 사용자가 권한을 거부했을 때의 동작
                showPermissionDeniedDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("권한 거부")
            setMessage("연락처를 가져오기 위해서는 권한을 허용해야 합니다.")
            setPositiveButton("확인") { _, _ ->
                requestPermission()
            }
            setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            show()
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
                val contactsId = it.getInt(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                list.add(ContactsData(contactsId, name, number))
            }
        }
        contacts?.close()
        list.sortBy { it.name }
        if (contactsList != list) {
            contactsList = list
            setContacts()
        }
    }

    private fun setContacts() {
        contactsAdapter = ContactsAdapter(contactsList)
        binding?.contactsList?.adapter = contactsAdapter
    }
}
