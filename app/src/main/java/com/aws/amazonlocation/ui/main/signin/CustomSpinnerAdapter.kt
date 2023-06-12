package com.aws.amazonlocation.ui.main.signin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.aws.amazonlocation.R

class CustomSpinnerAdapter(context: Context, private val items: ArrayList<String>) :
    ArrayAdapter<String>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_item, parent, false)

        val textView = view.findViewById<TextView>(R.id.spinner_item_text)
        val removeRegion = items[position].substring(0, items[position].lastIndexOf(" "))
        textView.text = removeRegion
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_dropdown_item, parent, false)

        val textView = view.findViewById<TextView>(R.id.spinner_item_text)
        textView.text = items[position]

        return view
    }
}