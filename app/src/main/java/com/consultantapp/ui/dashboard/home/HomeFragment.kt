package com.consultantapp.ui.dashboard.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
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
import com.consultantapp.databinding.FragmentHomeBinding
import com.consultantapp.ui.adapter.CommonFragmentPagerAdapter
import com.consultantapp.ui.classes.ClassesViewModel
import com.consultantapp.ui.dashboard.CategoriesAdapter
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

    private lateinit var viewModelBanner: BannerViewModel

    private var items = ArrayList<Categories>()

    private lateinit var adapter: CategoriesAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
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
        binding.clLoader.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorWhite))

        if (arguments?.getBoolean(CLASSES_PAGE) == true)
            binding.tvPopular.text = getString(R.string.classes)
        else
            binding.tvPopular.text = getString(R.string.meet)

        /*Set banner height*/
        val widthScreen = resources.displayMetrics.widthPixels - pxFromDp(requireContext(), 32f)
        val heightOfImage = (widthScreen * 0.6).toInt()

        binding.viewPagerBanner.layoutParams.height = heightOfImage
    }

    private fun setAdapter() {
        adapter = CategoriesAdapter(this, items)
        binding.rvCategory.adapter = adapter
    }

    private fun listeners() {
        binding.swipeRefresh.setOnRefreshListener {
            hitApi(true)
        }

        binding.rvCategory.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvCategory.layoutManager as LinearLayoutManager
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

        val hashMap = HashMap<String, String>()
        if (isConnectedToInternet(requireContext(), true)) {

            if (!isFirstPage && items.isNotEmpty())
                hashMap[AFTER] = items[items.size - 1].id ?: ""

            hashMap[PER_PAGE] = PER_PAGE_LOAD.toString()

            viewModel.categories(hashMap)

            /*Banner api*/
            viewModelBanner.banners()
        }
    }

    private fun bindObservers() {
        viewModel.categories.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    isLoadingMoreItems = false
                    binding.swipeRefresh.isRefreshing = false

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
                    binding.swipeRefresh.isRefreshing = false

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                }
            }
        })

        viewModelBanner.banners.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {

                    val itemsBanner = ArrayList<Banner>()
                    itemsBanner.addAll(it.data?.banners ?: emptyList())

                    val adapter = CommonFragmentPagerAdapter(childFragmentManager)
                    itemsBanner.forEach {
                        adapter.addTab("", BannerFragment(this, it))
                    }
                    binding.viewPagerBanner.adapter = adapter
                    binding.pageIndicatorView.setViewPager(binding.viewPagerBanner)

                    if (itemsBanner.isNotEmpty())
                        slideItem(binding.viewPagerBanner, requireContext())

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
        if (item?.is_subcategory == true) {
            startActivity(
                Intent(requireContext(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.SUB_CATEGORY)
                    .putExtra(CLASSES_PAGE, arguments?.getBoolean(CLASSES_PAGE))
                    .putExtra(CATEGORY_PARENT_ID, item)
            )
        } else if (arguments?.getBoolean(CLASSES_PAGE) == true) {
            if (userRepository.isUserLoggedIn()) {
                startActivity(
                        Intent(requireContext(), DrawerActivity::class.java)
                                .putExtra(PAGE_TO_OPEN, DrawerActivity.CLASSES)
                                .putExtra(CATEGORY_PARENT_ID, item)
                )
            } else {
                val fragment = WelcomeFragment()
                fragment.show(requireActivity().supportFragmentManager, fragment.tag)
            }
        } else {
            startActivity(
                Intent(requireContext(), DoctorListActivity::class.java)
                    .putExtra(CATEGORY_PARENT_ID, item)
            )
        }
    }
}
