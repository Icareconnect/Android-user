package com.consultantapp.ui.dashboard.home.banner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.consultantapp.R
import com.consultantapp.data.models.responses.Banner
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentBannerBinding
import com.consultantapp.ui.classes.ClassesDetailFragment.Companion.CLASS_ID
import com.consultantapp.ui.dashboard.doctor.detail.DoctorDetailActivity
import com.consultantapp.ui.dashboard.doctor.detail.DoctorDetailActivity.Companion.DOCTOR_ID
import com.consultantapp.ui.dashboard.doctor.listing.DoctorListActivity
import com.consultantapp.ui.dashboard.home.HomeFragment
import com.consultantapp.ui.dashboard.subcategory.SubCategoryFragment
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.utils.PAGE_TO_OPEN
import com.consultantapp.utils.getHtmlText
import com.consultantapp.utils.loadImage
import com.consultantapp.utils.visible
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class BannerFragment(private val fragment: Fragment?, private val banner: Banner) : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentBannerBinding

    private var rootView: View? = null


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_banner, container, false)
            rootView = binding.root

            initialise()
        }
        return rootView
    }

    private fun initialise() {
        if (fragment is HomeFragment) {
            binding.clBanner.setBackgroundResource(0)
            loadImage(binding.ivImage, banner.image_mobile, R.drawable.image_placeholder)


            binding.ivImage.setOnClickListener {
                when (banner.banner_type) {
                    BannerType.SERVICE_PROVIDER -> {
                        startActivity(Intent(binding.root.context, DoctorDetailActivity::class.java)
                                .putExtra(DOCTOR_ID, banner.sp_id))
                    }
                    BannerType.CATEGORY -> {
                        if (banner.category?.is_subcategory == true) {
                            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                                    .putExtra(PAGE_TO_OPEN, DrawerActivity.SUB_CATEGORY)
                                    .putExtra(SubCategoryFragment.CATEGORY_PARENT_ID, banner.category))
                        } else {
                            startActivity(Intent(requireContext(), DoctorListActivity::class.java)
                                    .putExtra(SubCategoryFragment.CATEGORY_PARENT_ID, banner.category))
                        }
                    }
                    BannerType.CLASS_ -> {

                            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                                    .putExtra(PAGE_TO_OPEN, DrawerActivity.CLASSES_DETAILS)
                                    .putExtra(CLASS_ID, banner.class_id))
                    }
                }
            }
        } else {
            binding.clBanner.setBackgroundResource(R.drawable.drawable_theme_trans)
            binding.tvCode.visible()
            binding.tvText.visible()
            binding.tvUsers.visible()

            val discount = if (banner.discount_type == "percentage")
                "${banner.discount_value}%"
            else
                banner.discount_value

            val service = when {
                banner.service != null -> banner.service?.name
                banner.category != null -> banner.category?.name
                else -> ""
            }

            binding.tvText.text = getHtmlText(getString(R.string.code_text, discount,
                    service, banner.end_date))
            binding.tvCode.text = getHtmlText(getString(R.string.use_code_s, banner.coupon_code))
            binding.tvUsers.text = getHtmlText(getString(R.string.s_user_remaining, banner.limit.toString()))
        }
    }
}

object BannerType {
    const val SERVICE_PROVIDER = "service_provider"
    const val CATEGORY = "category"
    const val CLASS_ = "class"
}
