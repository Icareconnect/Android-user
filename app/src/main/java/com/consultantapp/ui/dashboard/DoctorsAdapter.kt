package com.consultantapp.ui.dashboard

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Doctor
import com.consultantapp.data.network.LoadingStatus.ITEM
import com.consultantapp.data.network.LoadingStatus.LOADING
import com.consultantapp.databinding.ItemPagingLoaderBinding
import com.consultantapp.databinding.RvItemPopularBinding
import com.consultantapp.ui.dashboard.doctor.detail.DoctorDetailActivity
import com.consultantapp.ui.dashboard.doctor.detail.DoctorDetailActivity.Companion.DOCTOR_ID
import com.consultantapp.utils.*


class DoctorsAdapter(private val items: ArrayList<Doctor>) :
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
                            R.layout.rv_item_popular, parent, false
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

    inner class ViewHolder(val binding: RvItemPopularBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.clDoctor.setOnClickListener {
                binding.root.context.startActivity(
                        Intent(binding.root.context, DoctorDetailActivity::class.java)
                                .putExtra(DOCTOR_ID, items[adapterPosition].doctor_data?.id)
                )
            }
        }

        fun bind(doctor: Doctor) = with(binding) {
            slideRecyclerItem(binding.root, binding.root.context)

            tvName.text = getDoctorName(doctor.doctor_data)
            binding.tvDesc.text = doctor.doctor_data?.categoryData?.name
                    ?: binding.root.context.getString(R.string.na)
            loadImage(
                binding.ivPic,
                doctor.doctor_data?.profile_image,
                R.drawable.ic_profile_placeholder
            )

            binding.tvRating.text = binding.root.context.getString(R.string.s_s_reviews,
                    getUserRating(doctor.doctor_data?.totalRating), doctor.doctor_data?.reviewCount)

            binding.ivActiveStatus.hideShowView(doctor.doctor_data?.isAvailable == true)

        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
