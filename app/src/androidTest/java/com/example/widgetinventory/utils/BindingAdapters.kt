package com.example.widgetinventory.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.DecimalFormat

@BindingAdapter("formattedPrice")
fun bindFormattedPrice(textView: TextView, price: Double?) {
    price?.let {
        // Formato pedido: $ 5.888.000,00
        val formatter = DecimalFormat("$ #,###.00")
        textView.text = formatter.format(it)
    }
}