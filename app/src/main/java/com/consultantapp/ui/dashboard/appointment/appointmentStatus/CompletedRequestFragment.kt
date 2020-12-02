package com.consultantapp.ui.dashboard.appointment.appointmentStatus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.Request
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.databinding.FragmentRequestCompletedBinding
import com.consultantapp.ui.dashboard.appointment.AppointmentViewModel
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class CompletedRequestFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentRequestCompletedBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AppointmentViewModel

    private lateinit var request: Request


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_request_completed, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            hitApi()
        }
        return rootView
    }

    private fun initialise() {
        editTextScroll(binding.etReason)
        progressDialog = ProgressDialog(requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]


        if (requireActivity().intent.hasExtra(EXTRA_TAB)) {
            binding.toolbar.visible()
            binding.ivBackground.visible()
            binding.groupFeedback.visible()
        }
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            when {
                requireActivity().supportFragmentManager.backStackEntryCount > 0 -> {
                    requireActivity().supportFragmentManager.popBackStack()
                }
                else -> requireActivity().finish()
            }
        }

        binding.tvGiveFeedback.setOnClickListener {
            binding.toolbar.visible()
            binding.ivBackground.visible()
            binding.groupFeedback.visible()
        }
    }

    private fun hitApi() {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["request_id"] = requireActivity().intent.getStringExtra(EXTRA_REQUEST_ID) ?: ""
            viewModel.requestDetail(hashMap)
        }
    }

    private fun setData() {

    }

    private fun bindObservers() {
        viewModel.requestDetail.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {

                    request = it.data?.request_detail ?: Request()
                    setData()

                }
                Status.ERROR -> {

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {

                }
            }
        })
    }

}