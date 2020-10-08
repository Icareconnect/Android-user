package com.consultantapp.ui.dashboard.appointment

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Request
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemAppointmentBinding
import com.consultantapp.utils.*


class AppointmentAdapter(private val fragment: AppointmentFragment, private val items: ArrayList<Request>) :
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
                            R.layout.rv_item_appointment, parent, false
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

    inner class ViewHolder(val binding: RvItemAppointmentBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvCancel.setOnClickListener {
                fragment.cancelAppointment(items[adapterPosition])
            }

            binding.tvRate.setOnClickListener {
                fragment.rateUser(items[adapterPosition])
            }

            binding.tvTrack.setOnClickListener {
                fragment.checkStatus(items[adapterPosition])
            }
        }

        fun bind(request: Request) = with(binding) {
            val context = binding.root.context

            tvCancel.hideShowView(request.canCancel)
            tvRate.gone()

            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))

            tvName.text = getDoctorName(request.to_user)
            loadImage(binding.ivPic, request.to_user?.profile_image,
                R.drawable.ic_profile_placeholder)

            tvDateTime.text = "${DateUtils.dateTimeFormatFromUTC(DateFormat.MON_YEAR_FORMAT, request.bookingDateUTC)} · " +
                    "${DateUtils.dateTimeFormatFromUTC(DateFormat.TIME_FORMAT, request.bookingDateUTC)}"

            tvPrice.text = getCurrency(request.price)
            tvTrack.gone()

            when (request.status) {
                CallAction.ACCEPT -> {
                    tvStatus.text = context.getString(R.string.accepted)
                    tvCancel.gone()
                }
                CallAction.PENDING -> {
                    tvStatus.text = context.getString(R.string.neww)
                }
                CallAction.COMPLETED -> {
                    tvStatus.text = context.getString(R.string.done)
                    tvCancel.gone()
                    tvRate.visible()
                }
                CallAction.START -> {
                    tvStatus.text = context.getString(R.string.inprogess)
                    tvTrack.visible()
                    tvCancel.gone()
                }
                CallAction.REACHED -> {
                    tvStatus.text = context.getString(R.string.reached_destination)
                    tvTrack.visible()
                    tvCancel.gone()
                }
                CallAction.START_SERVICE -> {
                    tvStatus.text = context.getString(R.string.started)
                    tvTrack.visible()
                    tvCancel.gone()
                }
                CallAction.FAILED -> {
                    tvStatus.text = context.getString(R.string.no_show)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                }
                CallAction.CANCELED -> {
                    tvStatus.text = context.getString(R.string.canceled)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                    tvCancel.gone()
                }
                CallAction.CANCEL_SERVICE -> {
                    tvStatus.text = context.getString(R.string.canceled_service)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                    tvCancel.gone()
                }
                else -> {
                    tvStatus.text = context.getString(R.string.neww)
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
