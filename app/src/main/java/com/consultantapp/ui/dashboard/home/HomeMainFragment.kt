package com.consultantapp.ui.dashboard.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentHomeMainBinding
import com.consultantapp.ui.adapter.CommonFragmentPagerAdapter
import com.consultantapp.ui.dashboard.subcategory.SubCategoryFragment
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.ui.drawermenu.DrawerActivity.Companion.WALLET
import com.consultantapp.ui.drawermenu.wallet.WalletViewModel
import com.consultantapp.utils.PAGE_TO_OPEN
import com.consultantapp.utils.PrefsManager
import com.consultantapp.utils.getCurrency
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class HomeMainFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentHomeMainBinding

    private var rootView: View? = null

    private lateinit var adapter: CommonFragmentPagerAdapter

    private lateinit var viewModel: WalletViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_home_main, container, false)
            rootView = binding.root

            initialise()
            setTabs()
            listeners()
            bindObservers()

        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[WalletViewModel::class.java]

    }

    private fun listeners() {
        binding.tvWallet.setOnClickListener {
            if (userRepository.isUserLoggedIn()) {
                startActivity(
                    Intent(requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, WALLET)
                )
            }
        }
    }

    private fun setTabs() {
        adapter = CommonFragmentPagerAdapter(requireActivity().supportFragmentManager)
        val titles = arrayOf(getString(R.string.consult), getString(R.string.classes))

        var fragment: HomeFragment
        titles.forEachIndexed { index, s ->

            val bundle = Bundle()
            when (index) {
                0 -> bundle.putBoolean(SubCategoryFragment.CLASSES_PAGE, false)
                1 -> bundle.putBoolean(SubCategoryFragment.CLASSES_PAGE, true)
                else -> bundle.putBoolean(SubCategoryFragment.CLASSES_PAGE, false)
            }

            fragment = HomeFragment()
            fragment.arguments = bundle
            adapter.addTab(titles[index], fragment)
        }

        binding.viewPager.adapter = adapter

        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    private fun bindObservers() {

        viewModel.wallet.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.tvWallet.text = getCurrency(it.data?.balance)

                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {

                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (userRepository.isUserLoggedIn())
            viewModel.wallet(HashMap())
    }
}
