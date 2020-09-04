package com.consultantapp.ui.loginSignUp.verifyotp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import com.consultantapp.databinding.FragmentVerifyOtpBinding
import com.consultantapp.ui.LoginViewModel
import com.consultantapp.ui.dashboard.MainActivity
import com.consultantapp.ui.loginSignUp.insurance.InsuranceFragment
import com.consultantapp.ui.loginSignUp.signup.SignUpFragment
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class VerifyOTPFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var appSocket: AppSocket


    private lateinit var binding: FragmentVerifyOtpBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel

    private var phoneNumber = ""


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                    DataBindingUtil.inflate(inflater, R.layout.fragment_verify_otp, container, false)
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

        phoneNumber = arguments?.getString(COUNTRY_CODE).toString() + arguments?.getString(PHONE_NUMBER).toString()
        binding.tvMsg.text = getString(R.string.we_sent_you_a_code, phoneNumber)
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.ivNext.setOnClickListener {
            when {
                binding.pvOtp.text.toString().length < 4 -> {
                    binding.pvOtp.showSnackBar(getString(R.string.enter_otp))
                }
                binding.pvOtp.text.toString().length == 4 -> {
                    if (isConnectedToInternet(requireContext(), true)) {
                        val hashMap = HashMap<String, Any>()
                        hashMap["country_code"] = arguments?.getString(COUNTRY_CODE).toString()

                        if (arguments?.containsKey(UPDATE_NUMBER) == true) {
                            hashMap["phone"] = arguments?.getString(PHONE_NUMBER).toString()
                            hashMap["otp"] = binding.pvOtp.text.toString()
                            viewModel.updateNumber(hashMap)
                        } else {
                            hashMap["provider_id"] = arguments?.getString(PHONE_NUMBER).toString()
                            hashMap[ApiKeys.PROVIDER_TYPE] = ProviderType.phone
                            hashMap[ApiKeys.PROVIDER_VERIFICATION] = binding.pvOtp.text.toString()
                            hashMap[ApiKeys.USER_TYPE] = APP_TYPE

                            viewModel.login(hashMap)
                        }

                    }
                }
            }
        }

        binding.tvResentOTP.setOnClickListener {
            val hashMap = HashMap<String, Any>()
            hashMap["country_code"] = arguments?.getString(COUNTRY_CODE).toString()
            hashMap["phone"] = arguments?.getString(PHONE_NUMBER).toString()

            viewModel.sendSms(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.login.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)
                    if (userRepository.isUserLoggedIn()) {
                        /*Connect socket and update token*//*
                        appSocket.init()
                        userRepository.pushTokenUpdate()

                        requireActivity().setResult(Activity.RESULT_OK)
                        requireActivity().finish()*/

                        startActivity(Intent(requireContext(), MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        val fragment = SignUpFragment()
                        val bundle = Bundle()
                        bundle.putBoolean(UPDATE_NUMBER, true)
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

        viewModel.sendSMS.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireContext().longToast(getString(R.string.code_sent_to, phoneNumber))

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


        viewModel.updateNumber.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    if (userRepository.isUserLoggedIn()) {
                        /*Connect socket and update token*/
                        appSocket.init()
                        userRepository.pushTokenUpdate()

                        requireActivity().setResult(Activity.RESULT_OK)
                        requireActivity().finish()
                    } else {

                        val appSetting = userRepository.getAppSetting()
                        /*Handle feature keys*/
                        if (appSetting?.insurance == true || appSetting?.clientFeaturesKeys?.isAddress == true) {
                            replaceFragment(requireActivity().supportFragmentManager,
                                    InsuranceFragment(), R.id.container)
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
}
