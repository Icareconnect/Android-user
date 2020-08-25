package com.consultantapp.ui.dashboard.doctor.detail.prefrence

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.FilterOption
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemPrefrenceOptionBinding
import com.consultantapp.utils.gone
import com.consultantapp.utils.visible


class PrefrenceItemAdapter(private val multiSelect: Boolean, private val items: ArrayList<FilterOption>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.rv_item_prefrence_option, parent, false
                    )
            )
        } else {
            ViewHolderLoader(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_paging_loader, parent, false
                    )
            )
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) items.size else items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemPrefrenceOptionBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FilterOption) = with(binding) {
            if (multiSelect) {
                cbName.visible()
                rbName.gone()
            } else {
                cbName.gone()
                rbName.visible()
            }

            cbName.text = item.option_name
            rbName.text = item.option_name

            rbName.isChecked = item.isSelected
            cbName.isChecked = item.isSelected

            clMain.setOnClickListener {
                if (multiSelect) {
                    items[adapterPosition].isSelected = !items[adapterPosition].isSelected
                    notifyDataSetChanged()
                } else {
                    items.forEachIndexed { index, filterOption ->
                        items[index].isSelected = adapterPosition == index
                    }
                    notifyDataSetChanged()
                }
            }
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}

