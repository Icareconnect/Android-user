package com.consultantapp.ui.dashboard.doctor.detail.prefrence

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Filter
import com.consultantapp.data.models.responses.FilterOption
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemPrefrenceBinding
import com.consultantapp.ui.dashboard.home.HomeFragment
import com.consultantapp.ui.dashboard.home.bookservice.registerservice.RegisterServiceFragment
import com.consultantapp.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment
import com.consultantapp.utils.PreferencesType
import com.consultantapp.utils.gone
import com.consultantapp.utils.visible


class PrefrenceAdapter(private val fragment: Fragment, private val items: ArrayList<Filter>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.rv_item_prefrence, parent, false
                    )
            )
        } else {
            ViewHolderLoader(DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_paging_loader, parent, false
                    )
            )
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) items.size else items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemPrefrenceBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            when (fragment) {
                is MasterPrefrenceFragment -> {
                    when (fragment.prefrenceType) {
                        PreferencesType.WORK_ENVIRONMENT -> {
                            binding.tvName.gravity = Gravity.CENTER_HORIZONTAL

                            val layoutManager = LinearLayoutManager(fragment.requireContext())
                            binding.rvListing.layoutManager = layoutManager

                            binding.cbCheckAll.visible()
                        }
                    }
                }
                is HomeFragment -> {
                    binding.tvName.gone()
                    val layoutManager = LinearLayoutManager(fragment.requireContext())
                    binding.rvListing.layoutManager = layoutManager
                }
                is RegisterServiceFragment -> {
                    binding.tvName.gone()

                    val layoutManager = LinearLayoutManager(fragment.requireContext())
                    binding.rvListing.layoutManager = layoutManager
                }
            }

            binding.root.setOnClickListener {
                if (fragment is PrefrenceFragment)
                    fragment.clickItem(items[adapterPosition])
            }
            binding.cbCheckAll.setOnCheckedChangeListener { buttonView, isChecked ->
                items[adapterPosition].options?.forEachIndexed { index, filterOption ->
                    run {
                        items[adapterPosition].options?.get(index)?.isSelected = isChecked
                    }
                }
                notifyDataSetChanged()
            }
        }

        fun bind(item: Filter) = with(binding) {
            tvName.text = item.filter_name ?: item.preference_name


            val listOptions = ArrayList<FilterOption>()
            listOptions.addAll(item.options ?: emptyList())
            val prefrenceItemAdapter = PrefrenceItemAdapter(item.is_multi == "1", listOptions)
            rvListing.adapter = prefrenceItemAdapter

        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}

