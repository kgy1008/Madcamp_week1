package com.example.myapplication.ui.contact

import ContactsData
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemContactBinding
import java.util.Locale

class ContactsAdapter(private val contactList: List<ContactsData>, private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    interface OnItemClickListener {
        fun onItemClickListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contactList[position])
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    inner class ContactViewHolder(private val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: ContactsData) {
            binding.txtName.text = contact.name
            binding.txtPhoneNumber.text = PhoneNumberUtils.formatNumber(contact.number, Locale.getDefault().country)

            itemView.setOnClickListener {
                onItemClickListener.onItemClickListener(adapterPosition)
            }
        }
    }
}

