package com.consultantapp.ui.dashboard.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.SaveAddress
import com.consultantapp.data.models.responses.FilterOption
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentHomeBinding
import com.consultantapp.ui.AppVersionViewModel
import com.consultantapp.ui.dashboard.doctor.detail.prefrence.PrefrenceItemAdapter
import com.consultantapp.ui.dashboard.home.bookservice.registerservice.RegisterServiceFragment
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.utils.*
import com.google.android.libraries.places.widget.Autocomplete
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

    private lateinit var viewModelAppVersion: AppVersionViewModel

    private var items = ArrayList<FilterOption>()

    private val tempItems = ArrayList<FilterOption>()

    private lateinit var adapter: PrefrenceItemAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        viewModelAppVersion = ViewModelProvider(this, viewModelFactory)[AppVersionViewModel::class.java]
        binding.clLoader.setBackgroundResource(R.color.colorWhite)
    }

    private fun setAdapter() {
        adapter = PrefrenceItemAdapter(true, tempItems)
        binding.rvCategory.adapter = adapter
    }

    private fun handleUserData() {
        val userData = userRepository.getUser()

        binding.tvName.text = "${getString(R.string.hi)} ${userData?.name}"
        loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)
    }

    fun setLocation(locationName: String) {
        binding.tvAddress.text = locationName
    }

    private fun listeners() {
        binding.swipeRefresh.setOnRefreshListener {
            hitApi()
        }

        binding.tvAddress.setOnClickListener {
            placePicker(this, requireActivity())
        }

        binding.ivSearch.setOnClickListener {
            binding.etSearch.hideKeyboard()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                if (s.isNotEmpty())
                    filter(binding.etSearch.text.toString().toLowerCase())
                else
                    filter("")

            }
        })

        binding.tvContinue.setOnClickListener {
            var duties = ""
            items.forEach {
                if (it.isSelected) {
                    duties += "${it.id},"
                }
            }

            if (duties.isEmpty()) {
                binding.tvContinue.showSnackBar(getString(R.string.please_select_all_that_applies))
            } else {
                startActivity(Intent(requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.REGISTER_SERVICE)
                        .putExtra(RegisterServiceFragment.DUTIES, duties.removeSuffix(",")))
            }
        }
    }

    private fun filter(text: String) {
        tempItems.clear()
        items.forEach {
            if (it.option_name?.toLowerCase()?.contains(text) == true) {
                tempItems.add(it)
            }
        }
        adapter.notifyDataSetChanged()

        binding.cvCategory.hideShowView(tempItems.isNotEmpty())
        binding.tvNoData.hideShowView(tempItems.isEmpty())
    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            /* val hashMap = HashMap<String, String>()
             hashMap["category_id"] = CATEGORY_ID
             viewModel.getFilters(hashMap)*/

            val hashMap = HashMap<String, String>()
//            hashMap["filter_ids"] = "3"
            viewModelAppVersion.duty(hashMap)
        }
    }

    private fun bindObservers() {
        viewModelAppVersion.preferences.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    if (it.data?.preferences?.isNotEmpty() == true) {
                        items.clear()
                        items.addAll(it.data.preferences?.get(0)?.options ?: emptyList())

                        tempItems.clear()
                        tempItems.addAll(items)
                    }

                    adapter.notifyDataSetChanged()

                    binding.cvCategory.hideShowView(tempItems.isNotEmpty())
                    binding.tvNoData.hideShowView(tempItems.isEmpty())
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

    override fun onResume() {
        super.onResume()

        handleUserData()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppRequestCode.AUTOCOMPLETE_REQUEST_CODE -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)

                        binding.tvAddress.text = getAddress(place)

                        Log.i("Place===", "Place: " + place.name + ", " + place.id)

                        val address = SaveAddress()
                        address.address_name = getAddress(place)
                        address.location = ArrayList()
                        address.location?.add(place.latLng?.longitude ?: 0.0)
                        address.location?.add(place.latLng?.latitude ?: 0.0)

                        prefsManager.save(USER_ADDRESS, address)

                        //performAddressSelectAction(false, address)
                    }
                }
            }
        }
    }

}
