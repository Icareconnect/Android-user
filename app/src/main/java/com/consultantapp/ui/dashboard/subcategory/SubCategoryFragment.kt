package com.consultantapp.ui.dashboard.subcategory

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Banner
import com.consultantapp.data.models.responses.Categories
import com.consultantapp.data.network.ApiKeys.AFTER
import com.consultantapp.data.network.ApiKeys.PER_PAGE
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PER_PAGE_LOAD
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentSubCategoryBinding
import com.consultantapp.ui.adapter.CommonFragmentPagerAdapter
import com.consultantapp.ui.classes.ClassesViewModel
import com.consultantapp.ui.dashboard.CategoriesAdapter
import com.consultantapp.ui.dashboard.doctor.listing.DoctorListActivity
import com.consultantapp.ui.dashboard.home.BannerViewModel
import com.consultantapp.ui.dashboard.home.banner.BannerFragment
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.utils.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SubCategoryFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSubCategoryBinding

    private var rootView: View? = null

    private lateinit var viewModel: ClassesViewModel

    private lateinit var viewModelBanner: BannerViewModel

    private var items = ArrayList<Categories>()

    private lateinit var adapter: CategoriesAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var categoryData: Categories? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_sub_category, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()
            hitApi(true)
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        viewModelBanner = ViewModelProvider(this, viewModelFactory)[BannerViewModel::class.java]

        binding.rvListing.layoutManager = GridLayoutManager(requireContext(), 2)
        categoryData = requireActivity().intent.getSerializableExtra(CATEGORY_PARENT_ID) as Categories

        binding.tvConsult.text = categoryData?.name
    }

    private fun setAdapter() {
        //adapter = CategoriesAdapter(this, items)
        binding.rvListing.adapter = adapter
        binding.rvListing.itemAnimator = null
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.rvListing.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvListing.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMoreItems && !isLastPage && lastVisibleItemPosition >= totalItemCount) {
                    isLoadingMoreItems = true
                    hitApi(false)
                }
            }
        })
    }

    private fun hitApi(firstHit: Boolean) {
        if (firstHit) {
            isFirstPage = true
            isLastPage = false
        }


        if (isConnectedToInternet(requireContext(), true)) {
            var hashMap = HashMap<String, String>()
            if (!requireActivity().intent.getBooleanExtra(CLASSES_PAGE, false)) {
                hashMap["category_id"] = categoryData?.id ?: ""
                viewModelBanner.coupons(hashMap)
            }


            hashMap = HashMap()
            if (!isFirstPage && items.isNotEmpty())
                hashMap[AFTER] = items[items.size - 1].id ?: ""

            hashMap[PER_PAGE] = PER_PAGE_LOAD.toString()
            hashMap["parent_id"] = categoryData?.id ?: ""

            viewModel.categories(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.categories.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    isLoadingMoreItems = false

                    val tempList = it.data?.classes_category ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                        items.addAll(tempList)

                        adapter.notifyDataSetChanged()
                    } else {
                        val oldSize = items.size
                        items.addAll(tempList)

                        adapter.notifyItemRangeInserted(oldSize, items.size)
                    }

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)
                    binding.clLoader.gone()

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })


        viewModelBanner.coupons.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {

                    val itemsBanner = ArrayList<Banner>()
                    itemsBanner.addAll(it.data?.coupons ?: emptyList())

                    val adapter = CommonFragmentPagerAdapter(requireActivity().supportFragmentManager)
                    itemsBanner.forEach {
                        adapter.addTab("", BannerFragment(this, it))
                    }
                    binding.viewPagerBanner.adapter = adapter
                    binding.pageIndicatorView.setViewPager(binding.viewPagerBanner)

                    if(itemsBanner.isNotEmpty())
                        slideItem(binding.viewPagerBanner,requireContext())

                    binding.viewPagerBanner.hideShowView(itemsBanner.isNotEmpty())
                    binding.pageIndicatorView.hideShowView(itemsBanner.size > 1)
                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                }
            }
        })
    }


    fun clickItem(item: Categories?) {
        when {
            item?.is_subcategory == true -> {
                startActivity(Intent(requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.SUB_CATEGORY)
                        .putExtra(CLASSES_PAGE, requireActivity().intent.getBooleanExtra(CLASSES_PAGE, false))
                        .putExtra(CATEGORY_PARENT_ID, item))
            }
            requireActivity().intent.getBooleanExtra(CLASSES_PAGE, false) -> {
                    startActivity(
                        Intent(requireContext(), DrawerActivity::class.java)
                            .putExtra(PAGE_TO_OPEN, DrawerActivity.CLASSES)
                            .putExtra(CATEGORY_PARENT_ID, item)
                    )
            }
            else -> {
                startActivity(Intent(requireContext(), DoctorListActivity::class.java)
                        .putExtra(CATEGORY_PARENT_ID, item))
            }
        }
    }

    companion object {
        const val CATEGORY_PARENT_ID = "CATEGORY_PARENT_ID"
        const val CLASSES_PAGE = "CLASSES_PAGE"
    }
}
