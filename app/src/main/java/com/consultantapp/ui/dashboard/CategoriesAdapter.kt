package com.consultantapp.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.FilterOption
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemCategoryBinding


class CategoriesAdapter(private val fragment: Fragment, private val items: ArrayList<FilterOption>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                            R.layout.rv_item_category, parent, false))
        } else {
            ViewHolderLoader(DataBindingUtil.inflate(LayoutInflater.from(parent.context),
                            R.layout.item_paging_loader, parent, false))
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) items.size else items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemCategoryBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
            }
        }

        fun bind(item: FilterOption) = with(binding) {
            tvName.text = item.option_name
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
