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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.BookService
import com.consultantapp.data.models.requests.SaveAddress
import com.consultantapp.data.models.responses.FilterOption
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentRegisterServiceBinding
import com.consultantapp.ui.classes.ClassesViewModel
import com.consultantapp.ui.dashboard.home.bookservice.datetime.DateTimeFragment
import com.consultantapp.ui.dashboard.home.bookservice.location.AddAddressActivity
import com.consultantapp.ui.dashboard.home.bookservice.location.BottomAddressFragment
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
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

    private lateinit var viewModel: ClassesViewModel

    private lateinit var progressDialog: ProgressDialog

    private var rootView: View? = null

    private var bookService = BookService()

    private lateinit var adapterServiceFor: CheckItemAdapter

    private var itemsServiceFor = ArrayList<FilterOption>()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register_service, container, false)
            rootView = binding.root

            initialise()
            listeners()
            setAdapter()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        binding.tvNotSelf.gone()
        binding.ilNameOther.gone()

        binding.cbTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.cbTerms.setText(setAcceptTerms(requireActivity()), TextView.BufferType.SPANNABLE)

        binding.etName.setText(userRepository.getUser()?.name ?: "")

    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["duties"] = requireActivity().intent.getStringExtra(DUTIES) ?: ""
            viewModel.getFilters(hashMap)
        }
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
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.etAddress.setOnClickListener {
            val fragment = BottomAddressFragment(this)
            fragment.show(requireActivity().supportFragmentManager, fragment.tag)

            /*val intent = Intent(requireContext(), AddAddressActivity::class.java)
                if (bookService.address != null)
                    intent.putExtra(AddAddressActivity.EXTRA_ADDRESS, bookService.address)
                startActivityForResult(intent, AppRequestCode.ASK_FOR_LOCATION)*/
        }

        binding.tvContinue.setOnClickListener {

            var servicePos = -1
            itemsServiceFor.forEachIndexed { index, filterOption ->
                if (filterOption.isSelected) {
                    servicePos = index
                    return@forEachIndexed
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
                servicePos != 0 && (binding.etMobileNumber.text.toString().isEmpty() || binding.etMobileNumber.text.toString().length < 6) -> {
                    binding.etMobileNumber.showSnackBar(getString(R.string.enter_phone_number))
                }
                binding.etAddress.text.toString().trim().isEmpty() -> {
                    binding.etAddress.showSnackBar(getString(R.string.select_delivery_address))
                }
                else -> {
                    //bookService.filter_id = requireActivity().intent.getStringExtra(SubCategoryFragment.CATEGORY_PARENT_ID)
                    bookService.service_for = itemsServiceFor[servicePos].option_name
                    bookService.service_type = requireActivity().intent.getStringExtra(DUTIES)

                    if (servicePos == 0) {
                        bookService.personName = binding.etName.text.toString().trim()
                        val userData=userRepository.getUser()
                        bookService.country_code = userData?.country_code
                        bookService.phone_number = userData?.phone
                    }else {
                        bookService.personName = binding.etNameOther.text.toString().trim()
                        bookService.country_code = binding.ccpCountryCode.selectedCountryCodeWithPlus
                        bookService.phone_number = binding.etMobileNumber.text.toString().trim()
                    }

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
        viewModel.getFilters.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    val tempList = it.data?.filters ?: emptyList()

                    var roles = ""
                    if (tempList.isNotEmpty()) {
                        tempList[0].options?.forEach {
                            roles += it.option_name + ", "
                        }
                    }
                    binding.tvResultDutiesV.text = roles.removeSuffix(", ")

                    binding.tvResultDuties.hideShowView(roles.isNotEmpty())
                    binding.tvResultDutiesV.hideShowView(roles.isNotEmpty())
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


    fun onItemClick(serviceFor: Boolean, pos: Int) {
        if (serviceFor) {
            if (pos == 0) {
                binding.ilNameOther.gone()
                binding.tvNotSelf.gone()
                binding.groupPhone.gone()
            } else {
                binding.ilNameOther.visible()
                binding.tvNotSelf.visible()
                binding.groupPhone.visible()
                binding.ilNameOther.hint = "${itemsServiceFor[pos].option_name}\'s ${getString(R.string.full_name)}"
            }
        } else {
            /* var selectedItem = 0
             itemsHomeCare.forEach {
                 if (it.isSelected)
                     selectedItem += 1
             }

             if (selectedItem == 0)
                 binding.tvSelectHomeCare.text = ""
             else
                 binding.tvSelectHomeCare.text = "${getString(R.string.selected)} ($selectedItem)"*/
        }
    }

    fun selectedAddress(item: SaveAddress) {
        bookService.address = SaveAddress()
        bookService.address = item

        binding.etAddress.setText("${bookService.address?.address_name}")

        bookService.address?.address_name = if (bookService.address?.house_no.isNullOrEmpty())
            "${bookService.address?.save_as_preference?.option_name}\n${bookService.address?.address_name}"
        else
            "${bookService.address?.save_as_preference?.option_name} (${bookService.address?.house_no})\n${bookService.address?.address_name}"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ASK_FOR_LOCATION) {
                bookService.address = SaveAddress()
                bookService.address = data?.getSerializableExtra(AddAddressActivity.EXTRA_ADDRESS) as SaveAddress

                binding.etAddress.setText("${bookService.address?.address_name}")

                bookService.address?.address_name = if (bookService.address?.house_no.isNullOrEmpty())
                    "${bookService.address?.save_as_preference?.option_name}\n${bookService.address?.address_name}"
                else
                    "${bookService.address?.save_as_preference?.option_name} (${bookService.address?.house_no})\n${bookService.address?.address_name}"
            }
        }
    }

    companion object {
        const val DUTIES = "DUTIES"
    }
}
