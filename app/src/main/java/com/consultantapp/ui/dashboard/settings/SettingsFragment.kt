package com.consultantapp.ui.dashboard.settings

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
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.ProviderType
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentSettingsBinding
import com.consultantapp.ui.LoginViewModel
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.ui.drawermenu.DrawerActivity.Companion.CHANGE_PASSWORD
import com.consultantapp.ui.drawermenu.DrawerActivity.Companion.REQUEST
import com.consultantapp.ui.drawermenu.addmoney.AddMoneyActivity
import com.consultantapp.ui.webview.WebViewActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class SettingsFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentSettingsBinding

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
                    DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            setUserProfile()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())
    }

    private fun setUserProfile() {
        val userData = userRepository.getUser()

        binding.tvName.text = userData?.name
        loadImage(binding.ivPic, userData?.profile_image,
                R.drawable.ic_profile_placeholder)

        binding.tvVersion.text =
                getString(R.string.version, getVersion(requireActivity()).versionName)

        binding.tvChangePassword.hideShowView(userRepository.getUser()?.provider_type == ProviderType.email)
    }

    private fun listeners() {
        binding.ivPic.setOnClickListener {
            val itemImages = java.util.ArrayList<String>()
            itemImages.add(getImageBaseUrl(ImageFolder.UPLOADS,userRepository.getUser()?.profile_image))
            viewImageFull(requireActivity(), itemImages, 0)
        }

        binding.tvName.setOnClickListener {
            goToProfile()
        }


        binding.tvLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.tvSavedCards.setOnClickListener {
            startActivity(Intent(requireContext(), AddMoneyActivity::class.java))
        }

        binding.tvAccount.setOnClickListener {
            goToProfile()
        }

        binding.tvChangePassword.setOnClickListener {
            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, CHANGE_PASSWORD))
        }

        binding.tvMyBookings.setOnClickListener {
            startActivity(Intent(requireContext(), DrawerActivity::class.java)
                .putExtra(PAGE_TO_OPEN, REQUEST))
        }

        binding.tvContactUs.setOnClickListener {
            startActivity(Intent(requireContext(), WebViewActivity::class.java)
                    .putExtra(WebViewActivity.LINK_TITLE, getString(R.string.contact_us))
                    .putExtra(WebViewActivity.LINK_URL, PageLink.CONTACT_US))
        }

        binding.tvInvite.setOnClickListener {
            shareDeepLink(DeepLink.INVITE, requireActivity(), null)
        }
    }

    private fun goToProfile() {
        startActivityForResult(Intent(requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.PROFILE), AppRequestCode.PROFILE_UPDATE)
    }

    private fun showLogoutDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(
                requireContext(), R.string.sign_out,
                R.string.logout_dialog_message, R.string.yes, R.string.no, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        viewModel.logout()
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun bindObservers() {
        viewModel.logout.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    logoutUser(requireActivity(), prefsManager)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.PROFILE_UPDATE) {
                setUserProfile()
            }
        }
    }

}