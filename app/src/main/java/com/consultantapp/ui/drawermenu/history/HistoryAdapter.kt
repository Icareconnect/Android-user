package com.consultantapp.ui.drawermenu.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Wallet
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemHistoryBinding
import com.consultantapp.utils.*


class HistoryAdapter(private val items: ArrayList<Wallet>) :
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
                            R.layout.rv_item_history, parent, false
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

    inner class ViewHolder(val binding: RvItemHistoryBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Wallet) = with(binding) {
            tvName.text = getDoctorName(item.from)
            loadImage(binding.ivPic, item.from?.profile_image,
                R.drawable.image_placeholder)

            tvPrice.text = "-${getCurrency(item.amount)}"
            tvCallTime.text = DateUtils.dateTimeFormatFromUTC(DateFormat.DATE_TIME_FORMAT, item.created_at)

            val callTime = item.call_duration?.toLong() ?: 0
            tvCallDuration.text = convertSecondsToMinute(callTime)

            when (item.service_type) {
                CallType.CHAT -> ivRequestType.setImageResource(R.drawable.ic_chat)
                CallType.CALL -> ivRequestType.setImageResource(R.drawable.ic_call)
                else -> ivRequestType.setImageResource(R.drawable.ic_chat)
            }

        }
    }

    private fun convertSecondsToMinute(seconds: Long): String {
        val s = seconds % 60
        val m = seconds / 60 % 60
        return String.format("%02d:%02d min.", m, s)
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
