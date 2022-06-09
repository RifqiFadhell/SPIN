package com.spin.id.ui.home.shop.detail.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.spin.id.R
import com.spin.id.api.response.shop.detail.ProductsItem
import com.spin.id.utils.UnitFilter

class MerchantAdapter(private val context: Context, private val data: List<ProductsItem>) :
    BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val viewHolder: ViewHolder?
        val view = LayoutInflater.from(context).inflate(R.layout.merchant_item, parent, false)
        viewHolder = ViewHolder(view)

        viewHolder.textView.text = data[position].merchantName
        viewHolder.textPrice.text = UnitFilter.convertToRupiah(data[position].price)

        return view
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return data.size
    }

    private class ViewHolder(view: View) {
        val textView: TextView = view.findViewById(R.id.textTitleMerchant)
        val textPrice: TextView = view.findViewById(R.id.textSubMerchant)
    }
}