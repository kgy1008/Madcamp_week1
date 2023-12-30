package com.example.myapplication.ui.contact

import ContactsData
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import java.util.Locale

class ContactsAdapter(private val contactsList: ArrayList<ContactsData>) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(contactsList[position])
    }

    override fun getItemCount() = contactsList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.txtName)
        private val phoneNumberTextView: TextView = itemView.findViewById(R.id.txtPhoneNumber)

        fun bind(contact: ContactsData) {
            nameTextView.text = contact.name
            phoneNumberTextView.text = PhoneNumberUtils.formatNumber(contact.number, Locale.getDefault().country)
        }
    }
}
