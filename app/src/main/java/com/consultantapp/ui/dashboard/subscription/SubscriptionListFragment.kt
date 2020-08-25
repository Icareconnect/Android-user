package com.consultantapp.ui.dashboard.subscription

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
import com.consultantapp.data.models.responses.Packages
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.databinding.ActivityListingToolbarBinding
import com.consultantapp.ui.dashboard.home.BannerViewModel
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SubscriptionListFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityListingToolbarBinding

    private var rootView: View? = null

    private lateinit var viewModelBanner: BannerViewModel

    private lateinit var progressDialog: ProgressDialog

    private val items = ArrayList<String>()

    private val itemsSubScription = ArrayList<Packages>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding =
                    DataBindingUtil.inflate(inflater, R.layout.activity_listing_toolbar, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setSubscriptionList()
            bindObservers()
        }
        return rootView
    }


    private fun initialise() {
        progressDialog = ProgressDialog(requireActivity())
        viewModelBanner = ViewModelProvider(this, viewModelFactory)[BannerViewModel::class.java]
        binding.tvTitle.gone()
        binding.rvListing.isEnabled = false
    }

    private fun setSubscriptionList() {

        when (arguments?.getString(PAGE_TO_OPEN)) {
            LIST -> {
                binding.toolbar.title = getString(R.string.subscription)
                binding.swipeRefresh.isEnabled = false

                items.add(getString(R.string.subscriptions))
                items.add(getString(R.string.packages))
                items.add(getString(R.string.buy_subscriptions))
                items.add(getString(R.string.buy_health_packages))
                val adapter = SubscriptionListAdapter(this, items)
                binding.rvListing.adapter = adapter
            }
            LIST_ITEM -> {
                binding.toolbar.title = arguments?.getString(EXTRA_NAME)
                /*Subscriptions*/
                val hashMap = HashMap<String, String>()
                hashMap["type"] = "open"
                viewModelBanner.packSub(hashMap)
            }
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }
    }

    private fun bindObservers() {
        viewModelBanner.packSub.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()

                    itemsSubScription.clear()
                    itemsSubScription.addAll(it.data?.packages ?: emptyList())
                    val adapter = SubscriptionAdapter(this, itemsSubScription)
                    binding.rvListing.adapter = adapter

                    binding.clNoData.hideShowView(itemsSubScription.isEmpty())

                }
                Status.ERROR -> {
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })
    }

    fun onItemClick(pos: Int) {
        when (arguments?.getString(PAGE_TO_OPEN)) {
            LIST -> {
                val fragment = SubscriptionListFragment()
                val bundle = Bundle()
                bundle.putString(PAGE_TO_OPEN, LIST_ITEM)
                bundle.putString(EXTRA_NAME, items[pos])
                fragment.arguments = bundle

                replaceFragment(requireActivity().supportFragmentManager,
                        fragment, R.id.container)
            }
            LIST_ITEM -> {
                val fragment = SubscriptionDetailFragment()
                val bundle = Bundle()
                bundle.putSerializable(EXTRA_NAME, itemsSubScription[pos])
                fragment.arguments = bundle

                replaceFragment(requireActivity().supportFragmentManager,
                        fragment, R.id.container)
            }
        }
    }

    companion object {
        const val LIST = "LIST"
        const val LIST_ITEM = "LIST_ITEM"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppRequestCode.PACKAGE_UPDATE -> {
                    /*Subscriptions*/
                    val hashMap = HashMap<String, String>()
                    hashMap["type"] = "open"
                    viewModelBanner.packSub(hashMap)
                }
            }
        }
    }
}