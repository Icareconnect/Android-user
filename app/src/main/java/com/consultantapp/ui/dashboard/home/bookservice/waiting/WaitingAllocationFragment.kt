package com.consultantapp.ui.dashboard.home.bookservice.waiting

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
import com.consultantapp.databinding.FragmentUserVerificationBinding
import com.consultantapp.databinding.FragmentWaitingAllocationBinding
import com.consultantapp.utils.*
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class WaitingAllocationFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentWaitingAllocationBinding

    private var rootView: View? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_waiting_allocation, container, false)
            rootView = binding.root

        }
        return rootView
    }


}
