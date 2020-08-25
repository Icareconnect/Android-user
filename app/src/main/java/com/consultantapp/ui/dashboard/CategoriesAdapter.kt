package com.consultantapp.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Categories
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemCategoryBinding
import com.consultantapp.ui.classes.CategoriesFragment
import com.consultantapp.ui.dashboard.home.HomeFragment
import com.consultantapp.ui.dashboard.subcategory.SubCategoryFragment
import com.consultantapp.utils.loadImage
import com.consultantapp.utils.slideRecyclerItem


class CategoriesAdapter(private val fragment: Fragment, private val items: ArrayList<Categories>) :
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
                            R.layout.rv_item_category, parent, false
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

    inner class ViewHolder(val binding: RvItemCategoryBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (fragment is HomeFragment)
                    fragment.clickItem(items[adapterPosition])
                else if (fragment is CategoriesFragment)
                    fragment.clickItem(items[adapterPosition])
                else if (fragment is SubCategoryFragment)
                    fragment.clickItem(items[adapterPosition])

            }
        }

        fun bind(item: Categories) = with(binding) {
            slideRecyclerItem(binding.root, binding.root.context)

            if (item.color_code.isNullOrEmpty())
                clCategory.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.colorPrimary))
            else
                clCategory.setBackgroundColor(Color.parseColor(item.color_code))

            tvName.text = item.name

            loadImage(binding.ivCategory, item.image, 0)

        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
