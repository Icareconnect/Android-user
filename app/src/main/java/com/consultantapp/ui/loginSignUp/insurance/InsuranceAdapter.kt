package com.consultantapp.ui.loginSignUp.insurance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.appdetails.Insurance
import com.consultantapp.databinding.ItemInsuranceBinding


class InsuranceAdapter(private val fragment: InsuranceFragment, private val items: ArrayList<Insurance>) :
        RecyclerView.Adapter<InsuranceAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: InsuranceAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsuranceAdapter.ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_insurance, parent, false
                )
        )

    }

    override fun getItemCount(): Int = items.size


    inner class ViewHolder(val binding: ItemInsuranceBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Insurance) = with(binding) {

            cbName.text = item.name ?: ""
            cbName.isChecked = item.isSelected
            if (item.isSelected) {
                clInsurance.setBackgroundResource(R.color.colorPrimary)
            } else {
                clInsurance.setBackgroundResource(R.color.colorWhite)
            }

            clInsurance.setOnClickListener {
                /*Single Selection*/
                /*items.forEachIndexed { index, service ->
                    items[index].isSelected = index == adapterPosition
                }*/

                /*Multiple Selection*/
                items[adapterPosition].isSelected = !items[adapterPosition].isSelected
                notifyDataSetChanged()
            }

        }
    }

}
