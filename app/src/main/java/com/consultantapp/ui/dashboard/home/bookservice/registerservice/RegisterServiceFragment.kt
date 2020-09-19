package com.consultantapp.ui.dashboard.home.bookservice.registerservice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.BookService
import com.consultantapp.data.models.requests.SaveAddress
import com.consultantapp.data.models.responses.FilterOption
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentRegisterServiceBinding
import com.consultantapp.ui.dashboard.home.bookservice.datetime.DateTimeFragment
import com.consultantapp.ui.dashboard.home.bookservice.location.AddAddressActivity
import com.consultantapp.utils.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class RegisterServiceFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentRegisterServiceBinding

    private var rootView: View? = null

    private var bookService = BookService()

    private lateinit var adapterServiceFor: CheckItemAdapter

    private var itemsServiceFor = ArrayList<FilterOption>()

    private lateinit var adapterHomeCare: CheckItemAdapter

    private var itemsHomeCare = ArrayList<FilterOption>()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register_service, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            setAdapter()
        }
        return rootView
    }

    private fun initialise() {

        binding.cvHomeCare.gone()
        binding.ilNotSelf.gone()
        binding.ilNameOther.gone()

        binding.cbTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.cbTerms.setText(setAcceptTerms(requireActivity(), getString(R.string.you_agree_to_our_terms)), TextView.BufferType.SPANNABLE)

        binding.etName.setText(userRepository.getUser()?.name ?: "")

    }

    private fun setAdapter() {
        val listServiceFor = resources.getStringArray(R.array.service_for)

        itemsServiceFor.clear()
        listServiceFor.forEach {
            val item = FilterOption()
            item.option_name = it
            itemsServiceFor.add(item)
        }

        adapterServiceFor = CheckItemAdapter(this, true, false, itemsServiceFor)
        binding.rvServiceFor.adapter = adapterServiceFor


        val listHomeCare = resources.getStringArray(R.array.home_care)
        itemsHomeCare.clear()
        listHomeCare.forEach {
            val item = FilterOption()
            item.option_name = it
            itemsHomeCare.add(item)
        }

        adapterHomeCare = CheckItemAdapter(this, false, true, itemsHomeCare)
        binding.rvHomeCare.adapter = adapterHomeCare
    }


    private fun listeners() {

        binding.tvSelectHomeCare.setOnClickListener {
            binding.cvHomeCare.hideShowView(binding.cvHomeCare.visibility == View.GONE)
        }

        binding.etAddress.setOnClickListener {
            val intent = Intent(requireContext(), AddAddressActivity::class.java)
            if (bookService.address != null)
                intent.putExtra(AddAddressActivity.EXTRA_ADDRESS, bookService.address)
            startActivityForResult(intent, AppRequestCode.ASK_FOR_LOCATION)
        }

        binding.tvContinue.setOnClickListener {
            var servicePos = -1
            itemsServiceFor.forEachIndexed { index, filterOption ->
                if (filterOption.isSelected) {
                    servicePos = index
                    return@forEachIndexed
                }
            }

            var homeCare = ""
            itemsHomeCare.forEachIndexed { index, filterOption ->
                if (filterOption.isSelected) {
                    homeCare += "${filterOption.option_name},"
                }
            }

            when {
                binding.etName.text.toString().trim().isEmpty() -> {
                    binding.etName.showSnackBar(getString(R.string.enter_name))
                }
                servicePos == -1 -> {
                    binding.tvRequestService.showSnackBar(getString(R.string.requesting_service_for))
                }
                servicePos != 0 && binding.etNameOther.text.toString().trim().isEmpty() -> {
                    binding.etName.showSnackBar(getString(R.string.enter_name_other))
                }
                homeCare.isEmpty() -> {
                    binding.tvHomeCare.showSnackBar(getString(R.string.select_home_care_requirement))
                }
                binding.etAddress.text.toString().trim().isEmpty() -> {
                    binding.etAddress.showSnackBar(getString(R.string.select_delivery_address))
                }
                else -> {
                    bookService.filter_id = requireActivity().intent.getStringExtra(EXTRA_REQUEST_ID)
                    bookService.service_for = itemsServiceFor[servicePos].option_name
                    bookService.home_care_req = homeCare

                    if (servicePos == 0)
                        bookService.personName = binding.etName.text.toString().trim()
                    else
                        bookService.personName = binding.etNameOther.text.toString().trim()

                    val fragment = DateTimeFragment()
                    val bundle = Bundle()
                    bundle.putSerializable(EXTRA_REQUEST_ID, bookService)
                    fragment.arguments = bundle
                    replaceFragment(requireActivity().supportFragmentManager, fragment, R.id.container)
                }
            }
        }
    }

    private fun bindObservers() {

    }

    fun onItemClick(serviceFor: Boolean, pos: Int) {
        if (serviceFor) {
            if (pos == 0) {
                binding.ilNameOther.gone()
                binding.ilNotSelf.gone()
            } else {
                binding.ilNameOther.visible()
                binding.ilNotSelf.visible()
            }
        } else {
            var selectedItem = 0
            itemsHomeCare.forEach {
                if (it.isSelected)
                    selectedItem += 1
            }

            if (selectedItem == 0)
                binding.tvSelectHomeCare.text = ""
            else
                binding.tvSelectHomeCare.text = "${getString(R.string.selected)} ($selectedItem)"
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ASK_FOR_LOCATION) {
                bookService.address = SaveAddress()
                bookService.address = data?.getSerializableExtra(AddAddressActivity.EXTRA_ADDRESS) as SaveAddress
                binding.etAddress.setText("${bookService?.address?.locationName} (${bookService?.address?.houseNumber})")
            }
        }
    }
}
