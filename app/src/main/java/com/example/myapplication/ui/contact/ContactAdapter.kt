package com.example.myapplication.ui.contact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class ContactAdapter(private var contactList: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    // ViewHolder 설정
    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView) // 예시: nameTextView는 연락처의 이름을 표시하는 TextView
        val numberTextView: TextView = itemView.findViewById(R.id.numberTextView) // 예시: numberTextView는 연락처의 번호를 표시하는 TextView
    }

    // onCreateViewHolder: 뷰 홀더 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false) // 예시: item_contact는 각 연락처 항목의 레이아웃
        return ContactViewHolder(view)
    }

    // onBindViewHolder: 뷰 홀더에 데이터 바인딩
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentContact = contactList[position]
        holder.nameTextView.text = currentContact.name // 예시: 연락처의 이름 설정
        holder.numberTextView.text = currentContact.number // 예시: 연락처의 번호 설정
    }

    // getItemCount: 데이터 개수 반환
    override fun getItemCount(): Int {
        return contactList.size
    }
}

