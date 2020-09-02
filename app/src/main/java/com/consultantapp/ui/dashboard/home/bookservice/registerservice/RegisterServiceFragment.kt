package com.consultantapp.ui.dashboard.home.bookservice.registerservice

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.DateTimeKeyListener
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
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentRegisterServiceBinding
import com.consultantapp.ui.dashboard.home.bookservice.datetime.DateTimeFragment
import com.consultantapp.ui.dashboard.home.bookservice.location.AddAddressActivity
import com.consultantapp.ui.dashboard.home.bookservice.waiting.WaitingAllocationFragment
import com.consultantapp.ui.drawermenu.DrawerActivity
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
        }
        return rootView
    }

    private fun initialise() {
        binding.cvHomeCare.gone()
        binding.ilNotSelf.gone()
        binding.etNameOther.gone()

        binding.cbTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.cbTerms.setText(setAcceptTerms(requireActivity(), getString(R.string.you_agree_to_our_terms)), TextView.BufferType.SPANNABLE)

        binding.etName.setText(userRepository.getUser()?.name ?: "")

    }


    private fun listeners() {

        binding.tvSelectHomeCare.setOnClickListener {
            binding.cvHomeCare.hideShowView(binding.cvHomeCare.visibility == View.GONE)
        }

        binding.rgRequestService.setOnCheckedChangeListener { radioGroup, id ->
            val isNotSelf = id != R.id.rbSelf
            binding.ilNotSelf.hideShowView(isNotSelf)
            binding.etNameOther.hideShowView(isNotSelf)
        }

        binding.etAddress.setOnClickListener {
            val intent = Intent(requireContext(), AddAddressActivity::class.java)
            if (bookService.address != null)
                intent.putExtra(AddAddressActivity.EXTRA_ADDRESS, bookService.address)
            startActivityForResult(intent, AppRequestCode.ASK_FOR_LOCATION)
        }

        binding.etDate.setOnClickListener {
            startActivityForResult(Intent(requireContext(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, DrawerActivity.DATE_TIME), AppRequestCode.ADD_DATE)
            //replaceResultFragment(this, DateTimeFragment(), R.id.container, AppRequestCode.ADD_DATE)
        }

        binding.tvContinue.setOnClickListener {
            when {
                binding.etName.text.toString().trim().isEmpty() -> {
                    binding.etName.showSnackBar(getString(R.string.enter_name))
                }
                binding.rgRequestService.checkedRadioButtonId == -1 -> {
                    binding.rgRequestService.showSnackBar(getString(R.string.requesting_service_for))
                }
                binding.rgRequestService.checkedRadioButtonId != R.id.rbSelf
                        && binding.etNameOther.text.toString().trim().isEmpty() -> {
                    binding.etName.showSnackBar(getString(R.string.enter_name_other))
                }
                !binding.cbDualDiagnostic.isChecked && !binding.cbMedicallyCompromised.isChecked
                        && !binding.cbOther.isChecked && !binding.cbPalliative.isChecked
                        && !binding.cbWoundcare.isChecked -> {
                    binding.tvHomeCare.showSnackBar(getString(R.string.select_home_care_requirement))
                }
                binding.etAddress.text.toString().trim().isEmpty() -> {
                    binding.etAddress.showSnackBar(getString(R.string.select_delivery_address))
                }
                binding.etDate.text.toString().trim().isEmpty() -> {
                    binding.etDate.showSnackBar(getString(R.string.select_date))
                }
                else -> {
                    replaceFragment(requireActivity().supportFragmentManager,
                            WaitingAllocationFragment(), R.id.container)
                }
            }
        }
    }

    private fun bindObservers() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ASK_FOR_LOCATION) {
                bookService.address = SaveAddress()
                bookService.address = data?.getSerializableExtra(AddAddressActivity.EXTRA_ADDRESS) as SaveAddress
                binding.etAddress.setText("${bookService?.address?.locationName} (${bookService?.address?.houseNumber})")
            } else if (requestCode == AppRequestCode.ADD_DATE) {
                val book = data?.getSerializableExtra(EXTRA_REQUEST_ID) as BookService
                bookService.date = book.date
                bookService.startTime = book.startTime
                bookService.endTime = book.endTime
                bookService.reason = book.reason

                binding.etDate.setText(DateUtils.dateFormatFromMillis(DateFormat.DATE_FORMAT_SLASH_YEAR, bookService.date))
            }
        }
    }
}
