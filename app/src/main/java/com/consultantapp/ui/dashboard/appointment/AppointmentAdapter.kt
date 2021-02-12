package com.consultantapp.ui.dashboard.appointment

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Request
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemAppointmentBinding
import com.consultantapp.ui.drawermenu.DrawerActivity
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

        var userData: UserData? = null

        init {
            userData = fragment.userRepository.getUser()

            binding.tvCancel.setOnClickListener {
                fragment.cancelAppointment(items[adapterPosition])
            }

            binding.tvRate.setOnClickListener {
                if (items[adapterPosition].rating == null) {
                    fragment.startActivityForResult(Intent(fragment.requireActivity(), DrawerActivity::class.java)
                            .putExtra(PAGE_TO_OPEN, DrawerActivity.RATE)
                            .putExtra(EXTRA_REQUEST_ID, items[adapterPosition].id), AppRequestCode.APPOINTMENT_DETAILS)
                }
            }

            binding.tvApprove.setOnClickListener {
                if (items[adapterPosition].user_status == CallAction.PENDING) {
                    fragment.startActivityForResult(Intent(fragment.requireActivity(), DrawerActivity::class.java)
                            .putExtra(PAGE_TO_OPEN, DrawerActivity.APPROVE_HOUR)
                            .putExtra(EXTRA_REQUEST_ID, items[adapterPosition].id), AppRequestCode.APPOINTMENT_DETAILS)
                }
            }

            binding.tvTrack.setOnClickListener {
                fragment.checkStatus(items[adapterPosition])
            }

            binding.root.setOnClickListener {
                fragment.startActivityForResult(Intent(fragment.requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.APPOINTMENT_DETAILS)
                        .putExtra(EXTRA_REQUEST_ID, items[adapterPosition].id), AppRequestCode.APPOINTMENT_DETAILS)
            }
        }

        fun bind(request: Request) = with(binding) {
            val context = binding.root.context

            tvCancel.hideShowView(request.canCancel)
            tvRate.gone()
            binding.tvApprove.gone()

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
                    tvStatus.text = context.getString(R.string.new_request)
                }
                CallAction.COMPLETED -> {
                    tvStatus.text = context.getString(R.string.done)
                    tvCancel.gone()
                    tvRate.visible()
                    tvApprove.visible()

                    if (request.rating == null) {
                        tvRate.text = context.getString(R.string.rate)
                        tvRate.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        tvRate.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                    } else {
                        tvRate.text = request.rating
                        tvRate.setTextColor(ContextCompat.getColor(context, R.color.colorRate))
                        tvRate.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_star, 0, 0, 0)
                    }


                    when (request.user_status) {
                        CallAction.PENDING -> {
                            binding.tvApprove.visible()
                            tvApprove.text = context.getString(R.string.approve)
                        }
                        else -> {
                            binding.tvApprove.gone()
                        }
                    }
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
                    tvStatus.text = if (request.canceled_by?.id == userData?.id)
                        context.getString(R.string.canceled)
                    else context.getString(R.string.declined)

                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                    tvCancel.gone()
                }
                CallAction.CANCEL_SERVICE -> {
                    tvStatus.text = context.getString(R.string.canceled_service)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorNoShow))
                    tvCancel.gone()
                }
                else -> {
                    tvStatus.text = context.getString(R.string.new_request)
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
