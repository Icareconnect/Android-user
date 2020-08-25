package com.consultantapp.ui.loginSignUp.insurance

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.consultantapp.R
import com.consultantapp.data.models.responses.CountryCity

class CustomSpinnerAdapter(private val context: Context, private var items: List<CountryCity>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_spinner, parent, false)
            vh = ViewHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ViewHolder
        }
        vh.tvTitle.text = items[position].name
        return view
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return items.size
    }

    private class ViewHolder(view: View?) {
        val tvTitle: TextView = view?.findViewById(R.id.tvSpinner) as TextView
    }

}