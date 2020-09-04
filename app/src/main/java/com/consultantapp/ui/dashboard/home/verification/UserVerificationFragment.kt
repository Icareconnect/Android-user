package com.consultantapp.ui.dashboard.home.verification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantapp.R
import com.consultantapp.data.network.PushType
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentRegisterServiceBinding
import com.consultantapp.databinding.FragmentUserVerificationBinding
import com.consultantapp.utils.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class UserVerificationFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentUserVerificationBinding

    private var rootView: View? = null

    private var isReceiverRegistered = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_verification, container, false)
            rootView = binding.root

        }
        return rootView
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }


    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.PROFILE_APPROVED)
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(refreshRequests, intentFilter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshRequests)
            isReceiverRegistered = false
        }
    }

    private val refreshRequests = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == PushType.PROFILE_APPROVED) {
                requireActivity().finish()
            }
        }
    }
}
