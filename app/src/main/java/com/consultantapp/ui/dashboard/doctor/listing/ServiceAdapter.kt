package com.consultantapp.ui.dashboard.doctor.listing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Service
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.ItemServiceBinding


class ServiceAdapter(private val activity: DoctorListActivity, private val items: ArrayList<Service>) :
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
                            R.layout.item_service, parent, false
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

    inner class ViewHolder(val binding: ItemServiceBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.clService.setOnClickListener {

            }
        }

        fun bind(item: Service) = with(binding) {

            cbName.text = item.name ?: ""

            cbName.isChecked = item.isSelected
            if (item.isSelected) {
                clService.setBackgroundResource(R.drawable.drawable_theme_60)
            } else {
                clService.setBackgroundResource(R.drawable.drawable_service_inactive)
            }

            clService.setOnClickListener {
                if (!item.isSelected) {
                    items.forEachIndexed { index, service ->
                        items[index].isSelected = index == adapterPosition
                    }
                    notifyDataSetChanged()
                    activity.onServiceSelected(item)
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
