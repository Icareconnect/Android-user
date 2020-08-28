package com.consultantapp.ui.dashboard.home.registerservice

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentRegisterServiceBinding
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

    }

    private fun bindObservers() {

    }
}
