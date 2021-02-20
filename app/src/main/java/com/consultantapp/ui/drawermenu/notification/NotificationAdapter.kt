package com.consultantapp.ui.drawermenu.notification

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Notification
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemNotificationBinding
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.DateUtils.getTimeAgo


class NotificationAdapter(private val fragment: NotificationFragment, private val items: ArrayList<Notification>) :
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
                            R.layout.rv_item_notification, parent, false
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

    inner class ViewHolder(val binding: RvItemNotificationBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                when (items[adapterPosition].module) {
                    NotificationType.REQUEST ->
                        fragment.startActivityForResult(Intent(fragment.requireContext(), DrawerActivity::class.java)
                                .putExtra(PAGE_TO_OPEN, DrawerActivity.APPOINTMENT_DETAILS)
                                .putExtra(EXTRA_REQUEST_ID, items[adapterPosition].module_id), AppRequestCode.APPOINTMENT_DETAILS)
                    NotificationType.PAYMENT ->
                        fragment.startActivity(Intent(fragment.requireContext(), DrawerActivity::class.java)
                                .putExtra(PAGE_TO_OPEN, DrawerActivity.WALLET))
                }
            }
        }

        fun bind(item: Notification) = with(binding) {
            slideRecyclerItem(binding.root, binding.root.context)
            tvName.text = item.message

            loadImage(binding.ivPic, item.form_user?.profile_image,
                    R.drawable.image_placeholder)

            tvCallDuration.text = getTimeAgo(item.created_at)
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
