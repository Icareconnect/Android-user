package com.consultantapp.ui.loginSignUp.masterprefrence

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.SetFilter
import com.consultantapp.data.models.responses.Filter
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentServiceBinding
import com.consultantapp.ui.AppVersionViewModel
import com.consultantapp.ui.LoginViewModel
import com.consultantapp.ui.dashboard.MainActivity
import com.consultantapp.ui.dashboard.doctor.detail.prefrence.PrefrenceAdapter
import com.consultantapp.ui.dashboard.doctor.detail.prefrence.PrefrenceFragment.Companion.FILTER_DATA
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import com.google.gson.Gson
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class MasterPrefrenceFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentServiceBinding

    private var rootView: View? = null

    private lateinit var viewModel: LoginViewModel

    private lateinit var viewModelAppVersion: AppVersionViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<Filter>()

    private lateinit var adapter: PrefrenceAdapter

    var prefrenceType = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()
            hitApi()
        }
        return rootView
    }

    private fun initialise() {
        viewModelAppVersion = ViewModelProvider(this, viewModelFactory)[AppVersionViewModel::class.java]
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        binding.tvNext.text = getString(R.string.save)
        prefrenceType = arguments?.getString(MASTER_PREFRENCE_TYPE)
                ?: PreferencesType.PERSONAL_INTEREST
        when (prefrenceType) {
            PreferencesType.PERSONAL_INTEREST -> {
                binding.tvTitle.text = getString(R.string.personal_interests)
            }
            PreferencesType.PROVIDABLE_SERVICES -> {
                binding.tvTitle.text = getString(R.string.providable_services)
            }
            PreferencesType.WORK_ENVIRONMENT -> {
                binding.tvTitle.text = getString(R.string.work_environment)
            }
            PreferencesType.COVID -> {
                binding.tvTitle.text = getString(R.string.covid_19)
            }
        }
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
            /*Check selected Filter*/
            val filterArray = ArrayList<SetFilter>()

            var setFilter: SetFilter

            items.forEachIndexed { index, filter ->
                setFilter = SetFilter()

                /*Set filter Id*/
                setFilter.preference_id = filter.id
                setFilter.option_ids = ArrayList()

                var selectedOption = false
                filter.options?.forEach {
                    if (it.isSelected) {
                        selectedOption = true

                        setFilter.option_ids?.add(it.id ?: "")
                    }
                }

                if (filter.is_required ?:"1" == "1" && !selectedOption) {
                    binding.toolbar.showSnackBar(filter.preference_name ?: "")
                    return@setOnClickListener
                } else {
                    filterArray.add(setFilter)
                }
            }

            val hashMap = HashMap<String, Any>()
            hashMap["master_preferences"] = Gson().toJson(filterArray)
            viewModel.updateProfile(hashMap)
        }
    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            if (prefrenceType == PreferencesType.PROVIDABLE_SERVICES) {
                hashMap["filter_ids"] = requireActivity().intent.getStringExtra(FILTER_DATA)
                viewModelAppVersion.duty(hashMap)
            } else {
                hashMap["type"] = PreferencesType.ALL
                hashMap["preference_type"] = prefrenceType
                viewModelAppVersion.preferences(hashMap)
            }
        } else
            binding.swipeRefresh.isRefreshing = false
    }

    private fun bindObservers() {
        viewModelAppVersion.preferences.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false


                    val tempList = it.data?.preferences ?: emptyList()
                    items.clear()

                    items.addAll(tempList)
                    adapter.notifyDataSetChanged()

                    if (items.isNotEmpty())
                        binding.tvNext.visible()

                    adapter.setAllItemsLoaded(true)

                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    binding.swipeRefresh.isRefreshing = false
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

        viewModel.updateProfile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    if (arguments?.getBoolean(UPDATE_PROFILE, false) == true) {
                        resultFragmentIntent(this, targetFragment ?: this,
                                AppRequestCode.PROFILE_UPDATE, Intent())
                    } else {
                        when (prefrenceType) {
                            PreferencesType.WORK_ENVIRONMENT -> {
                                val fragment = MasterPrefrenceFragment()
                                val bundle = Bundle()
                                bundle.putString(MASTER_PREFRENCE_TYPE, PreferencesType.COVID)
                                fragment.arguments = bundle
                                replaceFragment(requireActivity().supportFragmentManager,
                                        fragment, R.id.container)
                            }
                            else -> {
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                                requireActivity().finish()
                            }
                        }
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


    fun clickItem(item: Filter?) {

    }

    companion object {
        const val MASTER_PREFRENCE_TYPE = "MASTER_PREFRENCE_TYPE"
    }
}
