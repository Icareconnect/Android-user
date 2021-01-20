package com.consultantapp.ui.dashboard.home.bookservice.registerservice

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.FilterOption
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemCheckBinding
import com.consultantapp.utils.gone
import com.consultantapp.utils.visible


class CheckItemAdapter(private val fragment: Fragment?, private val serviceFor: Boolean,
                       private val multiSelect: Boolean, private val items: ArrayList<FilterOption>) :
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
                            R.layout.rv_item_check, parent, false
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

    inner class ViewHolder(val binding: RvItemCheckBinding) :
            RecyclerView.ViewHolder(binding.root) {

        val context=binding.root.context
        init {

        }

        fun bind(item: FilterOption) = with(binding) {
            if (fragment is RegisterServiceFragment) {
                if (multiSelect) {
                    cbName.visible()
                    rbName.gone()
                } else {
                    cbName.gone()
                    rbName.visible()
                }
            } else {
                tvName.visible()
                cbName.gone()
                rbName.gone()
                if (item.isSelected) {
                    tvName.setBackgroundResource(R.drawable.drawable_theme_60)
                    tvName.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                } else {
                    tvName.setBackgroundResource(R.drawable.drawable_stroke_inactive)
                    tvName.setTextColor(ContextCompat.getColor(context, R.color.colorBlack))
                }
            }

            cbName.text = item.option_name
            rbName.text = item.option_name
            tvName.text = item.option_name

            rbName.isChecked = item.isSelected
            cbName.isChecked = item.isSelected

            clMain.setOnClickListener {
                val pos = adapterPosition
                if (multiSelect) {
                    items[pos].isSelected = !items[pos].isSelected
                    notifyDataSetChanged()
                } else {
                    items.forEachIndexed { index, filterOption ->
                        items[index].isSelected = pos == index
                    }
                    notifyDataSetChanged()
                }
                if (fragment is RegisterServiceFragment)
                    fragment.onItemClick(serviceFor, pos)
            }
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}

