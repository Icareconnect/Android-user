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
            binding.tvReSchedule.setOnClickListener {
                fragment.rescheduleAppointment(items[adapterPosition])
            }

            binding.tvCancel.setOnClickListener {
                fragment.cancelAppointment(items[adapterPosition])
            }

            binding.tvRate.setOnClickListener {
                fragment.rateUser(items[adapterPosition])
            }
        }

        fun bind(request: Request) = with(binding) {
            val context = binding.root.context

            slideRecyclerItem(binding.root, context)

            tvCancel.hideShowView(request.canCancel)
            tvReSchedule.hideShowView(request.canReschedule)
            tvReSchedule.text=context.getString(R.string.re_schedule)
            tvRate.gone()

            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))

            tvName.text = getDoctorName(request.to_user)
            loadImage(
                binding.ivPic, request.to_user?.profile_image,
                R.drawable.ic_profile_placeholder
            )

            tvDateTime.text = "${DateUtils.dateTimeFormatFromUTC(DateFormat.MON_YEAR_FORMAT, request.bookingDateUTC)} · " +
                    "${DateUtils.dateTimeFormatFromUTC(DateFormat.TIME_FORMAT, request.bookingDateUTC)}"

            tvRequestType.text = request.service_type
            tvPrice.text = getCurrency(request.price)

            when (request.status) {
                CallAction.ACCEPT -> {
                    tvStatus.text = context.getString(R.string.accepted)
                    tvReSchedule.gone()
                    tvCancel.gone()
                }
                CallAction.PENDING -> {
                    tvStatus.text = context.getString(R.string.neww)
                }
                CallAction.COMPLETED -> {
                    tvStatus.text = context.getString(R.string.done)
                    tvReSchedule.text=context.getString(R.string.book_again)
                    tvReSchedule.visible()
                    tvCancel.gone()
                    tvRate.visible()
                }
                CallAction.INPROGRESS, CallAction.BUSY -> {
                    tvStatus.text = context.getString(R.string.inprogess)
                    tvReSchedule.gone()
                    tvCancel.gone()
                }
                CallAction.FAILED -> {
                    tvStatus.text = context.getString(R.string.no_show)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                }
                CallAction.CANCELED -> {
                    tvStatus.text = context.getString(R.string.canceled)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                    tvReSchedule.gone()
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
