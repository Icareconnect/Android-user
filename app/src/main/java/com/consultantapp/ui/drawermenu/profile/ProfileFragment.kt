package com.consultantapp.ui.drawermenu.profile

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
import com.consultantapp.data.models.responses.Filter
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentProfileBinding
import com.consultantapp.ui.LoginViewModel
import com.consultantapp.ui.loginSignUp.SignUpActivity
import com.consultantapp.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import permissions.dispatcher.*
import java.util.*
import javax.inject.Inject

class ProfileFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentProfileBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: LoginViewModel

    private var userData: UserData? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
            rootView = binding.root

            initialise()
            setUserProfile()
            hiApiUserProfile()
            listeners()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun hiApiUserProfile() {
        if (isConnectedToInternet(requireContext(), true)) {
            viewModel.profile()
        }
    }

    private fun setUserProfile() {
        userData = userRepository.getUser()

        binding.tvName.text = userData?.name ?: ""
        binding.tvEmailV.text = userData?.email ?: getString(R.string.na)
        binding.tvPhoneV.text = "${userData?.country_code ?: getString(R.string.na)} ${userData?.phone ?: ""}"
        loadImage(binding.ivPic, userData?.profile_image, R.drawable.ic_profile_placeholder)


        val workExperience = ArrayList<Filter>()
        val covid = ArrayList<Filter>()
        userData?.master_preferences?.forEach {
            when (it.preference_type) {
                PreferencesType.COVID ->
                    covid.add(it)
                PreferencesType.WORK_ENVIRONMENT ->
                    workExperience.add(it)
            }
        }

        if (workExperience.isNotEmpty()) {
            var workText = ""
            workExperience.forEach {
                it.options?.forEach {
                    if (it.isSelected) {
                        workText += it.option_name + ", "
                    }
                }
            }
            binding.tvWorkV.text = workText.removeSuffix(", ")
            binding.tvWorkV.hideShowView(workText.isNotEmpty())
        } else {
            binding.tvWorkV.gone()
        }

        if (covid.isNotEmpty()) {
            var covidText = ""
            covid.forEach {
                covidText += it.preference_name + "\n"

                it.options?.forEach {
                    if (it.isSelected) {
                        covidText += it.option_name + "\n\n"
                    }
                }
            }
            binding.tvCovidV.text = covidText
            binding.tvCovidV.hideShowView(covidText.isNotEmpty())
        } else {
            binding.tvCovidV.gone()
        }
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.tvEdit.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(UPDATE_PROFILE, true), AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvPhoneUpdate.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), SignUpActivity::class.java)
                    .putExtra(UPDATE_NUMBER, true), AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvWorkUpdate.setOnClickListener {

            val fragment = MasterPrefrenceFragment()
            val bundle = Bundle()
            bundle.putString(MasterPrefrenceFragment.MASTER_PREFRENCE_TYPE, PreferencesType.WORK_ENVIRONMENT)
            bundle.putBoolean(UPDATE_PROFILE, true)
            fragment.arguments = bundle

            replaceResultFragment(this, fragment, R.id.container, AppRequestCode.PROFILE_UPDATE)
        }

        binding.tvCovidUpdate.setOnClickListener {

            val fragment = MasterPrefrenceFragment()
            val bundle = Bundle()
            bundle.putString(MasterPrefrenceFragment.MASTER_PREFRENCE_TYPE, PreferencesType.COVID)
            bundle.putBoolean(UPDATE_PROFILE, true)
            fragment.arguments = bundle

            replaceResultFragment(this, fragment, R.id.container, AppRequestCode.PROFILE_UPDATE)
        }

        binding.ivPic.setOnClickListener {
            val itemImages = ArrayList<String>()
            itemImages.add(getImageBaseUrl(ImageFolder.UPLOADS, userRepository.getUser()?.profile_image))
            viewImageFull(requireActivity(), itemImages, 0)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.PROFILE_UPDATE) {
                setUserProfile()
                requireActivity().setResult(Activity.RESULT_OK)
            }
        }
    }


    private fun bindObservers() {
        viewModel.profile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    setUserProfile()
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(false)
                }
            }
        })
    }
}
