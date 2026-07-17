package com.perez.horushealth.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.perez.horushealth.model.Country

/*
 * ============================================================================
 *  ADAPTADOR DE PAÍSES  (para el desplegable de país en Registro y en Mi Cuenta)
 * ============================================================================
 *  Aquí NO usamos RecyclerView sino ArrayAdapter, que es el adaptador simple
 *  para listas desplegables (AutoCompleteTextView).
 *
 *  Usa un layout que ya trae Android (android.R.layout.simple_dropdown_item_1line),
 *  por eso no hay un XML propio para esta lista.
 * ============================================================================
 */
class CountryAdapter(context: Context, countries: List<Country>) :
    ArrayAdapter<Country>(context, android.R.layout.simple_dropdown_item_1line, countries) {

    // getView: dibuja cada fila del desplegable.
    // Muestra el resultado de Country.toString() -> "🇪🇨 Ecuador (+593)"
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
        
        val country = getItem(position)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = country?.toString()
        
        return view
    }
}
