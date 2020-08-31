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
import com.consultantapp.data.models.requests.SaveAddress
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentRegisterServiceBinding
import com.consultantapp.ui.dashboard.home.bookservice.datetime.DateTimeFragment
import com.consultantapp.ui.dashboard.home.bookservice.location.AddAddressActivity
import com.consultantapp.ui.dashboard.home.bookservice.waiting.WaitingAllocationFragment
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

    private var address: SaveAddress? = null


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
            if (address != null)
                intent.putExtra(AddAddressActivity.EXTRA_ADDRESS, address)
            startActivityForResult(intent, AppRequestCode.ASK_FOR_LOCATION)
        }

        binding.etServiceDate.setOnClickListener {
            replaceFragment(requireActivity().supportFragmentManager,
                    DateTimeFragment(), R.id.container)
        }

        binding.tvContinue.setOnClickListener {
            replaceFragment(requireActivity().supportFragmentManager,
                    WaitingAllocationFragment(), R.id.container)
        }
    }

    private fun bindObservers() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ASK_FOR_LOCATION) {
                address = data?.getSerializableExtra(AddAddressActivity.EXTRA_ADDRESS) as SaveAddress
                binding.etAddress.setText("${address?.locationName} (${address?.houseNumber})")
            }
        }
    }
}
