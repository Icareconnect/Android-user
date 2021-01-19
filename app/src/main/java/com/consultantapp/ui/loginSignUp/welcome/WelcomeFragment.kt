package com.consultantapp.ui.loginSignUp.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.databinding.FragmentWelcomeBinding
import com.consultantapp.ui.loginSignUp.login.LoginFragment
import com.consultantapp.ui.loginSignUp.signup.SignUpFragment
import com.consultantapp.utils.PrefsManager
import com.consultantapp.utils.replaceFragment
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class WelcomeFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentWelcomeBinding

    private var rootView: View? = null


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome, container, false)
            rootView = binding.root

            initialise()
            listeners()
        }
        return rootView
    }

    private fun initialise() {

    }

    private fun listeners() {
        binding.tvSignUpMobile.setOnClickListener {
            val fragment = LoginFragment()
            val bundle = Bundle()
            bundle.putBoolean(EXTRA_SIGNUP, true)
            fragment.arguments = bundle

            replaceFragment(requireActivity().supportFragmentManager,
                    fragment, R.id.container)
        }

        binding.tvLogin.setOnClickListener {
            replaceFragment(requireActivity().supportFragmentManager,
                    LoginFragment(), R.id.container)
        }

        binding.tvSignUpEmail.setOnClickListener {
            replaceFragment(requireActivity().supportFragmentManager,
                    SignUpFragment(), R.id.container)
        }
    }

    companion object {
        const val EXTRA_SIGNUP = "EXTRA_SIGNUP"
    }
}
