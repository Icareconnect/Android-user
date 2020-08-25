package com.consultantapp.ui.loginSignUp.welcome

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.network.ApiKeys
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.ProviderType
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentWelcomeBinding
import com.consultantapp.di.DaggerBottomSheetDialogFragment
import com.consultantapp.ui.LoginViewModel
import com.consultantapp.ui.loginSignUp.SignUpActivity
import com.consultantapp.ui.loginSignUp.SignUpActivity.Companion.EXTRA_LOGIN
import com.consultantapp.ui.loginSignUp.SignUpActivity.Companion.EXTRA_LOGIN_EMAIL
import com.consultantapp.ui.loginSignUp.SignUpActivity.Companion.EXTRA_SIGNUP_EMAIL
import com.consultantapp.ui.loginSignUp.loginemail.LoginEmailFragment
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import javax.inject.Inject


class WelcomeFragment : DaggerBottomSheetDialogFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentWelcomeBinding

    private var rootView: View? = null

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = STATE_EXPANDED
        return dialog
    }


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
        binding.tvTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.tvTerms.setText(setAcceptTerms(requireActivity(),getString(R.string.you_agree_to_our_terms)), TextView.BufferType.SPANNABLE)
    }

    private fun listeners() {
        binding.ivCross.setOnClickListener {
            dialog?.dismiss()
        }

        binding.tvSignUpMobile.setOnClickListener {
            gotoLogin(false)
        }

        binding.tvSignUpEmail.setOnClickListener {
            startActivity(Intent(activity, SignUpActivity::class.java)
                .putExtra(EXTRA_SIGNUP_EMAIL, false))

            dialog?.dismiss()
        }

        binding.tvLogin.setOnClickListener {
            gotoLogin(false)
        }
    }

    private fun gotoLogin(updateNumber: Boolean) {
        startActivity(Intent(activity, SignUpActivity::class.java)
            .putExtra(EXTRA_LOGIN, true)
            .putExtra(UPDATE_NUMBER, updateNumber))

        dialog?.dismiss()
    }
}
