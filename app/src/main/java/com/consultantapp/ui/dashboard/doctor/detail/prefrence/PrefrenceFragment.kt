package com.consultantapp.ui.dashboard.doctor.detail.prefrence

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.Categories
import com.consultantapp.data.models.responses.Filter
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentServiceBinding
import com.consultantapp.ui.LoginViewModel
import com.consultantapp.ui.classes.ClassesViewModel
import com.consultantapp.ui.dashboard.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class PrefrenceFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentServiceBinding

    private var rootView: View? = null

    private lateinit var viewModel: ClassesViewModel

    private lateinit var viewModelLogin: LoginViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<Filter>()

    private lateinit var adapter: PrefrenceAdapter

    private var categoryData: Categories? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()

            if (requireActivity().intent.hasExtra(FILTER_DATA)) {
                val filterSet = requireActivity().intent.getSerializableExtra(FILTER_DATA) as ArrayList<Filter>
                if (filterSet.isNotEmpty()) {
                    items.clear()
                    items.addAll(filterSet)
                    adapter.notifyDataSetChanged()
                } else
                    hitApi()
            } else
                hitApi()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        viewModelLogin = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        binding.tvTitle.text = getString(R.string.set_prefrences)
        binding.tvClearFilter.visible()
        categoryData = requireActivity().intent?.getSerializableExtra(CATEGORY_PARENT_ID) as Categories

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
            hitApi()
        }

        binding.tvNext.setOnClickListener {
            val intent = Intent()
            intent.putExtra(FILTER_DATA, items)
            requireActivity().setResult(Activity.RESULT_OK, intent)
            requireActivity().finish()
        }

        binding.tvClearFilter.setOnClickListener {
            hitApi()
        }
    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["category_id"] = categoryData?.id ?: ""
            viewModel.getFilters(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.getFilters.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()

                    val tempList = it.data?.filters ?: emptyList()
                    items.clear()

                    items.addAll(tempList)
                    adapter.notifyDataSetChanged()
                    if (items.isNotEmpty())
                        binding.tvNext.visible()

                    adapter.setAllItemsLoaded(true)

                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    adapter.setAllItemsLoaded(true)
                    binding.clLoader.gone()

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                    binding.tvNext.gone()
                }
            }
        })
    }


    fun clickItem(item: Filter?) {

    }

    companion object {
        const val FILTER_DATA = "FILTER_DATA"
    }
}
