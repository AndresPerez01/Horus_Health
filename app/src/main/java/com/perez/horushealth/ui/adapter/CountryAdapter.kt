package com.perez.horushealth.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.perez.horushealth.model.Country

class CountryAdapter(context: Context, countries: List<Country>) :
    ArrayAdapter<Country>(context, android.R.layout.simple_dropdown_item_1line, countries) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        
        val country = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = country?.toString()
        
        return view
    }
}
