package com.consultantapp.ui.dashboard.home.bookservice.datetime

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.requests.DatesAvailability
import com.consultantapp.databinding.ItemDatesBinding
import com.consultantapp.utils.DateFormat.DATE_ONLY
import com.consultantapp.utils.DateUtils.dateFormatFromMillis
import com.consultantapp.utils.invisible
import com.consultantapp.utils.visible
import java.util.*

class DatesAdapter(private val fragment: Fragment, private val items: ArrayList<DatesAvailability>) :
        RecyclerView.Adapter<DatesAdapter.ViewHolder>() {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                R.layout.item_dates, parent, false))
    }

    inner class ViewHolder(val binding: ItemDatesBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DatesAvailability) = with(binding) {

            cbName.text = item.displayName?.substring(0, 1)

            cbDate.text = dateFormatFromMillis(DATE_ONLY, item.date ?: 0)

            cbName.isChecked = item.isSelected
            cbDate.isChecked = item.isSelected

            if (item.isSelected) {
                viewSelect.visible()
            } else {
                viewSelect.invisible()
            }


            clDate.setOnClickListener {
                items[adapterPosition].isSelected = !items[adapterPosition].isSelected

                /*if (fragment is DateTimeFragment)
                    fragment.onDateSelected(items[adapterPosition])*/
                notifyDataSetChanged()
            }
        }
    }
}
