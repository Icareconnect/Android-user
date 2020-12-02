package com.consultantapp.ui.dashboard.covid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.requests.SetFilter
import com.consultantapp.data.models.responses.Filter
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PER_PAGE_LOAD
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentServiceBinding
import com.consultantapp.ui.AppVersionViewModel
import com.consultantapp.ui.dashboard.doctor.detail.prefrence.PrefrenceAdapter
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class CovidFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentServiceBinding

    private var rootView: View? = null

    private lateinit var viewModel: AppVersionViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<Filter>()

    private lateinit var adapter: PrefrenceAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service, container, false)
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
        viewModel = ViewModelProvider(this, viewModelFactory)[AppVersionViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        binding.tvTitle.text = "Covid 19"
    }

    private fun setAdapter() {
        adapter = PrefrenceAdapter(this, items)
        binding.rvListing.adapter = adapter
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            hitApi(true)
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

        binding.tvNext.setOnClickListener {
            /*Check selected Filter*/
            val filterArray = ArrayList<SetFilter>()

            var setFilter: SetFilter

            items.forEachIndexed { index, filter ->
                setFilter = SetFilter()

                /*Set filter Id*/
                setFilter.filter_id = filter.id
                setFilter.filter_option_ids = ArrayList()

                var selectedOption = false
                filter.options?.forEach {
                    if (it.isSelected) {
                        selectedOption = true

                        setFilter.filter_option_ids?.add(it.id ?: "")
                    }
                }

                if (selectedOption) {
                    filterArray.add(setFilter)
                } else {
                    binding.toolbar.showSnackBar(filter.preference_name ?: "")
                    return@setOnClickListener
                }
            }

        }
    }

    private fun hitApi(firstHit: Boolean) {
        if (isConnectedToInternet(requireContext(), true)) {
            if (firstHit) {
                isFirstPage = true
                isLastPage = false
            }

            val hashMap = HashMap<String, String>()
            hashMap["type"] = PreferencesType.ALL
            viewModel.preferences(hashMap)
        } else
            binding.swipeRefresh.isRefreshing = false
    }

    private fun bindObservers() {
        viewModel.preferences.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    isLoadingMoreItems = false

                    val tempList = it.data?.preferences ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                    }

                    items.addAll(tempList)
                    adapter.notifyDataSetChanged()

                    if (items.isNotEmpty())
                        binding.tvNext.visible()

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    binding.swipeRefresh.isRefreshing = false
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)
                    binding.clLoader.gone()

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                    binding.tvNext.gone()
                }
            }
        })
    }


    fun clickItem(item: Filter?) {

    }
}
