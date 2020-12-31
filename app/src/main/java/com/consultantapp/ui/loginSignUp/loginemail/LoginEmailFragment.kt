package com.consultantapp.ui.loginSignUp.loginemail

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.network.ApiKeys
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.ProviderType
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentLoginEmailBinding
import com.consultantapp.ui.LoginViewModel
import com.consultantapp.ui.dashboard.MainActivity
import com.consultantapp.ui.loginSignUp.forgotpassword.ForgotPasswordFragment
import com.consultantapp.ui.loginSignUp.login.LoginFragment
import com.consultantapp.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class LoginEmailFragment : DaggerFragment() {


    @Inject
    lateinit var appSocket: AppSocket

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentLoginEmailBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                    DataBindingUtil.inflate(inflater, R.layout.fragment_login_email, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.tvLoginScreen.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
            replaceFragment(requireActivity().supportFragmentManager,
                    LoginFragment(), R.id.container)
        }

        binding.ivNext.setOnClickListener {
            when {
                binding.etEmail.text.toString().isEmpty() -> {
                    binding.etEmail.showSnackBar(getString(R.string.enter_email))
                }
                !Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches() -> {
                    binding.etEmail.showSnackBar(getString(R.string.enter_correct_email))
                }
                binding.etPassword.text.toString().length < 8 -> {
                    binding.etPassword.showSnackBar(getString(R.string.enter_password))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    val hashMap = HashMap<String, Any>()
                    hashMap[ApiKeys.PROVIDER_TYPE] = ProviderType.email
                    hashMap["provider_id"] = binding.etEmail.text.toString()
                    hashMap[ApiKeys.PROVIDER_VERIFICATION] = binding.etPassword.text.toString()
                    hashMap[ApiKeys.USER_TYPE] = APP_TYPE

                    viewModel.login(hashMap)
                }
            }
        }

        binding.tvForgetPass.setOnClickListener {
            replaceFragment(requireActivity().supportFragmentManager,
                    ForgotPasswordFragment(), R.id.container)
        }
    }


    private fun bindObservers() {
        viewModel.login.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)
                    if (userRepository.isUserLoggedIn()) {

                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        val fragment = MasterPrefrenceFragment()
                        val bundle = Bundle()
                        bundle.putString(MasterPrefrenceFragment.MASTER_PREFRENCE_TYPE, PreferencesType.WORK_ENVIRONMENT)
                        fragment.arguments = bundle

                        replaceFragment(requireActivity().supportFragmentManager,
                                fragment, R.id.container)
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
}
