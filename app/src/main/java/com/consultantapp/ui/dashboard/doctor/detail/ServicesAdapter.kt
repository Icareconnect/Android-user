package com.consultantapp.ui.dashboard.doctor.detail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Service
import com.consultantapp.databinding.RvItemServiceBinding
import com.consultantapp.utils.getCurrency
import com.consultantapp.utils.getUnitPrice


class ServicesAdapter(private val activity: DoctorDetailActivity, private val items: ArrayList<Service>) :
        RecyclerView.Adapter<ServicesAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ServicesAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesAdapter.ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.rv_item_service, parent, false
                )
        )
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: RvItemServiceBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.clService.setOnClickListener {
                activity.serviceClick(items[adapterPosition])
            }
        }

        fun bind(item: Service) = with(binding) {

            try {
                clService.setBackgroundColor(Color.parseColor(item.color_code))
            } catch (e: Exception) {
                clService.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimary))
            }

            tvName.text = item.service_name

            val price = if (item.price_type == "price_range")
                item.price
            else
                item.price

            tvPrice.text = binding.root.context.getString(R.string.price_s, getCurrency(price),
                    getUnitPrice(item.unit_price,binding.root.context))
        }
    }
}
