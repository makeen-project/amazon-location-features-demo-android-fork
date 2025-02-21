package com.aws.amazonlocation.ui.main.signin

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aws.amazonlocation.R

class CustomSpinnerAdapter(context: Context, private val items: ArrayList<String>) :
    ArrayAdapter<String>(context, 0, items) {
    private var mSelectedIndex = -1

    fun setSelection(position: Int) {
        mSelectedIndex = position
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_item, parent, false)

        val textView = view.findViewById<TextView>(R.id.spinner_item_text)
        textView.text = items[position]
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_dropdown_item, parent, false)

        val textView = view.findViewById<TextView>(R.id.spinner_item_text)
        textView.text = items[position]

        if (position == mSelectedIndex) {
            view.setBackgroundColor(
                ContextCompat.getColor(context, R.color.color_selected_spinner_bg)
            )
        } else {
            view.setBackgroundColor(Color.TRANSPARENT)
        }
        return view
    }
}
