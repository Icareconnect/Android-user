package com.consultantapp.ui.classes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.ClassData
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemClassBinding
import com.consultantapp.utils.*


class ClassesAdapter(
        private val fragment: ClassesFragment,
        private val items: ArrayList<ClassData>
) :
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
                            R.layout.rv_item_class, parent, false
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

    inner class ViewHolder(val binding: RvItemClassBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvStartClass.setOnClickListener {
                fragment.startCall(items[adapterPosition])
            }

            binding.clClass.setOnClickListener {
                fragment.clickItem(items[adapterPosition])
            }
        }

        fun bind(item: ClassData) = with(binding) {
            val context = binding.root.context
            slideRecyclerItem(binding.root, context)

            tvName.text = getDoctorName(item.created_by)
            tvDesc.text = item.created_by?.categoryData?.name ?: context.getString(R.string.na)

            loadImage(
                binding.ivPic,
                item.created_by?.profile_image,
                R.drawable.ic_profile_placeholder
            )

            tvClassName.text = item.name

            val classTime = DateUtils.dateTimeFormatFromUTC(DateFormat.DATE_TIME_FORMAT, item.bookingDateUTC)
            tvClassTime.text = classTime
            tvClassPrice.text = getCurrency(item.price)

            if (item.status == ClassType.COMPLETED) {
                tvStartClass.gone()
            } else {
                if (item.isOccupied) {
                    tvStartClass.text = context.getString(R.string.join_class)
                } else {
                    tvStartClass.text = context.getString(R.string.occupy_class)
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
