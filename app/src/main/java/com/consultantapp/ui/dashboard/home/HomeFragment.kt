package com.consultantapp.ui.dashboard.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Banner
import com.consultantapp.data.models.responses.Categories
import com.consultantapp.data.models.responses.FilterOption
import com.consultantapp.data.network.ApiKeys.AFTER
import com.consultantapp.data.network.ApiKeys.PER_PAGE
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PER_PAGE_LOAD
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentHomeBinding
import com.consultantapp.ui.adapter.CommonFragmentPagerAdapter
import com.consultantapp.ui.classes.ClassesViewModel
import com.consultantapp.ui.dashboard.CategoriesAdapter
import com.consultantapp.ui.dashboard.MainActivity
import com.consultantapp.ui.dashboard.doctor.listing.DoctorListActivity
import com.consultantapp.ui.dashboard.home.banner.BannerFragment
import com.consultantapp.ui.dashboard.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantapp.ui.dashboard.subcategory.SubCategoryFragment.Companion.CLASSES_PAGE
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.ui.loginSignUp.welcome.WelcomeFragment
import com.consultantapp.utils.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class HomeFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentHomeBinding

    private var rootView: View? = null

    private lateinit var viewModel: ClassesViewModel

    private var items = ArrayList<FilterOption>()

    private lateinit var adapter: CategoriesAdapter


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
            rootView = binding.root

            initialise()
            handleUserData()
            setAdapter()
            listeners()
            bindObservers()
            hitApi()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        binding.clLoader.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.colorWhite)
        )

    }

    private fun setAdapter() {
        adapter = CategoriesAdapter(this, items)
        binding.rvCategory.adapter = adapter
    }

    private fun handleUserData() {
        if (userRepository.isUserLoggedIn()) {
            val userData = userRepository.getUser()

            binding.tvName.text = "${getString(R.string.hi)} ${userData?.name}"
            loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)
        } else {
            binding.tvName.text = "${getString(R.string.hi)} ${getString(R.string.guest)}"
        }
    }

    fun setLocation(locationName: String) {
        binding.tvAddress.text = locationName
    }

    private fun listeners() {
        binding.swipeRefresh.setOnRefreshListener {
            hitApi()
        }

        binding.ivPic.setOnClickListener {
            goToProfile()
        }

        binding.tvName.setOnClickListener {
            goToProfile()
        }
    }

    private fun goToProfile() {
        if (userRepository.isUserLoggedIn()) {
            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.PROFILE))
        } else {
            val fragment = WelcomeFragment()
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)
        }
    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["category_id"] = "1"
            viewModel.getFilters(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.getFilters.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    val tempList = it.data?.filters ?: emptyList()

                    items.clear()
                    if (tempList.isNotEmpty())
                        items.addAll(tempList[0].options ?: emptyList())

                    binding.cvCategory.hideShowView(items.isNotEmpty())
                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    adapter.setAllItemsLoaded(true)
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                }
            }
        })
    }


    fun clickItem(item: FilterOption?) {
        if (!userRepository.isUserLoggedIn()) {
            val fragment = WelcomeFragment()
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)
        } else {
            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.REGISTER_SERVICE)
                    .putExtra(CATEGORY_PARENT_ID, item?.id))
        }
    }

    override fun onResume() {
        super.onResume()

        handleUserData()
    }
}
