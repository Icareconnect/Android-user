package com.consultantapp.ui.dashboard.doctor.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Interval
import com.consultantapp.databinding.ItemIntervalBinding


class IntervalAdapter(private val fragment: ScheduleFragment, private val items: ArrayList<Interval>) :
        RecyclerView.Adapter<IntervalAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: IntervalAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntervalAdapter.ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_interval, parent, false
                )
        )

    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(val binding: ItemIntervalBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Interval) = with(binding) {

            cbName.text = item.time ?: ""
            clService.isEnabled = item.available ?: true
            if (item.available == true)
                clService.alpha = 1.0f
            else
                clService.alpha = 0.5f

            cbName.isChecked = item.isSelected
            if (item.isSelected) {
                clService.setBackgroundResource(R.drawable.drawable_stroke_theme)
            } else {
                clService.setBackgroundResource(R.drawable.drawable_stroke_inactive)
            }

            clService.setOnClickListener {
                items.forEachIndexed { index, service ->
                    items[index].isSelected = index == adapterPosition
                }
                notifyDataSetChanged()
                fragment.onIntervalSelected(item)
            }

        }
    }

}
