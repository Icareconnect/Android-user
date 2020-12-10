package com.consultantapp.ui.dashboard.subscription

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.Packages
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.databinding.FragmentSubscriptionDetailsBinding
import com.consultantapp.ui.dashboard.home.BannerViewModel
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SubscriptionDetailFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSubscriptionDetailsBinding

    private var rootView: View? = null

    private lateinit var viewModelBanner: BannerViewModel

    private lateinit var progressDialog: ProgressDialog

    private var packages: Packages? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_subscription_details, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }


    private fun initialise() {
        progressDialog = ProgressDialog(requireActivity())
        binding.clLoader.setBackgroundResource(R.color.colorWhite)
        viewModelBanner = ViewModelProvider(this, viewModelFactory)[BannerViewModel::class.java]

        packages = arguments?.getSerializable(EXTRA_NAME) as Packages

        /*get package details*/
        val hashMap = HashMap<String, String>()
        hashMap["package_id"] = packages?.id ?: ""
        viewModelBanner.packDetail(hashMap)
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.btnBuy.setOnClickListener {
            AlertDialog.Builder(requireContext())
                    .setCancelable(false)
                    .setTitle(getString(R.string.buy_subscriptions))
                    .setMessage(getString(R.string.buy_subscriptions))
                    .setPositiveButton(getString(R.string.buy_subscriptions)) { dialog, which ->
                        val hashMap = HashMap<String, String>()
                        hashMap["plan_id"] = packages?.id ?: ""
                        viewModelBanner.purchasePack(hashMap)

                    }.setNegativeButton(getString(R.string.cancel)) { dialog, which ->

                    }.show()
        }
    }

    private fun bindObservers() {
        viewModelBanner.packDetail.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()

                    packages = it.data?.detail
                    binding.tvTitle.text = packages?.title
                    binding.tvDesc.text = packages?.description

                    if (packages?.subscribe == true)
                        binding.btnBuy.gone()

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

        viewModelBanner.purchasePack.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    /*If amount not sufficient then add money*/
                    if (it.data?.amountNotSufficient == true) {
                        AlertDialog.Builder(requireContext())
                                .setCancelable(false)
                                .setTitle(getString(R.string.added_to_wallet))
                                .setMessage(getString(R.string.money_insufficient))
                                .setPositiveButton(getString(R.string.ok)) { dialog, which ->

                                }
                                .setNegativeButton(getString(R.string.add_money)) { dialog, which ->
                                    startActivity(
                                            Intent(requireContext(), DrawerActivity::class.java)
                                                    .putExtra(PAGE_TO_OPEN, DrawerActivity.WALLET))
                                }.show()

                    } else {
                        requireActivity().longToast(getString(R.string.added_subscriptions))
                        requireActivity().setResult(Activity.RESULT_OK)

                        binding.btnBuy.gone()
                    }

                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })
    }
}