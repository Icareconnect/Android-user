package com.consultantapp.ui.dashboard.doctor.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.requests.DatesAvailability
import com.consultantapp.databinding.ItemDatesBinding
import com.consultantapp.utils.DateFormat.DATE_MON_YEAR
import com.consultantapp.utils.DateUtils.dateFormatFromMillis
import com.consultantapp.utils.gone
import com.consultantapp.utils.visible
import java.util.*

class DatesAdapter(private val fragment: ScheduleFragment, private val items: ArrayList<DatesAvailability>) :
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

            cbName.text = when (adapterPosition) {
                0 -> fragment.getString(R.string.today)
                1 -> fragment.getString(R.string.tomorrow)
                else -> item.displayName
            }

            cbDate.text = dateFormatFromMillis(DATE_MON_YEAR, item.date ?: 0)

            cbName.isChecked = item.isSelected
            cbDate.isChecked = item.isSelected

            if (item.isSelected) {
                view.visible()
            } else {
                view.gone()
            }


            clDate.setOnClickListener {
                if (!item.isSelected) {
                    for (count: Int in 0 until items.size) {
                        items[count].isSelected = count == adapterPosition
                        notifyItemChanged(count)
                    }
                    fragment.onDateSelected(item)
                }
            }
        }
    }
}
