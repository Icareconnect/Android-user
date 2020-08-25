package com.consultantapp.ui.dashboard.location

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.databinding.FragmentLocationBinding
import com.consultantapp.utils.PrefsManager
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class LocationFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentLocationBinding

    private var rootView: View? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_location, container, false)
            rootView = binding.root

            initialise()
            listeners()
        }
        return rootView
    }

    private fun initialise() {


    }


    private fun listeners() {
        binding.tvSkip.setOnClickListener {
            requireActivity().finish()
        }

        binding.tvUseLocation.setOnClickListener {
            requireActivity().setResult(Activity.RESULT_OK)
            requireActivity().finish()
        }

    }
}
