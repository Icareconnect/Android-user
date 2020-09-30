package com.consultantapp.ui.dashboard.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import com.consultantapp.ui.classes.ClassesViewModel
import com.consultantapp.ui.dashboard.CategoriesAdapter
import com.consultantapp.ui.dashboard.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
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

    private lateinit var viewModel: ClassesViewModel

    private var items = ArrayList<FilterOption>()

    private lateinit var adapter: CategoriesAdapter


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

        binding.ivPic.setOnClickListener {
            goToProfile()
        }

        binding.tvName.setOnClickListener {
            goToProfile()
        }

        binding.tvAddress.setOnClickListener {
            placePicker(this, requireActivity())
        }
    }

    private fun goToProfile() {
        startActivity(Intent(requireContext(), DrawerActivity::class.java)
                .putExtra(PAGE_TO_OPEN, DrawerActivity.PROFILE))
    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["category_id"] = CATEGORY_ID
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

                    adapter.notifyDataSetChanged()
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
        startActivity(Intent(requireContext(), DrawerActivity::class.java)
                .putExtra(PAGE_TO_OPEN, DrawerActivity.REGISTER_SERVICE)
                .putExtra(CATEGORY_PARENT_ID, item?.id))
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
                        address.locationName = getAddress(place)
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
