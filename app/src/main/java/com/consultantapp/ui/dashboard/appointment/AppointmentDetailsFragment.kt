package com.consultantapp.ui.dashboard.appointment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.Request
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.databinding.FragmentAppointmentDetailsBinding
import com.consultantapp.ui.dashboard.appointment.appointmentStatus.AppointmentStatusActivity
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject


class AppointmentDetailsFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentAppointmentDetailsBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AppointmentViewModel

    private lateinit var request: Request


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_appointment_details, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            hitApi()
        }
        return rootView
    }

    private fun initialise() {
        progressDialog = ProgressDialog(requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]
        binding.clLoader.setBackgroundResource(R.color.colorWhite)
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.tvCancel.setOnClickListener {
            cancelAppointment()
        }

        binding.tvViewMap.setOnClickListener {
            val address = request.extra_detail
            mapIntent(requireActivity(), address?.service_address ?: "", address?.lat?.toDouble()
                    ?: 0.0,
                    address?.long?.toDouble() ?: 0.0)
        }

        binding.tvRate.setOnClickListener {
            if (request.rating == null) {
                startActivityForResult(Intent(requireActivity(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.RATE)
                        .putExtra(EXTRA_REQUEST_ID, request.id), AppRequestCode.APPOINTMENT_DETAILS)
            }
        }

        binding.tvApprove.setOnClickListener {
            if (request.userIsApproved == false) {
                startActivityForResult(Intent(requireActivity(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.APPROVE_HOUR)
                        .putExtra(EXTRA_REQUEST_ID, request.id), AppRequestCode.APPOINTMENT_DETAILS)
            }
        }

        binding.tvTrack.setOnClickListener {
            checkStatus()
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
        binding.tvCancel.hideShowView(request.canCancel)
        binding.tvTrack.gone()
        binding.tvApprove.gone()
        binding.tvRate.gone()

        binding.tvName.text = request.to_user?.name
        binding.tvServiceTypeV.text = request.extra_detail?.filter_name ?: ""
        binding.tvDistanceV.text = request.extra_detail?.distance ?: ""
        binding.tvLocation.text = request.extra_detail?.service_address
        loadImage(binding.ivPic, request.to_user?.profile_image,
                R.drawable.ic_profile_placeholder)

        binding.tvBookingDateV.text = getDatesComma(request.extra_detail?.working_dates)
        binding.tvBookingTimeV.text = "${request.extra_detail?.start_time ?: ""} - ${request.extra_detail?.end_time ?: ""}"

        binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
        binding.tvSpecialInstructionsV.text = request.extra_detail?.reason_for_service


        var services = ""
        request.duties?.forEach {
            services += it.option_name + ", "
        }
        binding.tvServicesV.text = services.removeSuffix(", ")
        binding.tvServices.hideShowView(services.isNotEmpty())
        binding.tvServicesV.hideShowView(services.isNotEmpty())

        when (request.status) {
            CallAction.ACCEPT -> {
                binding.tvStatus.text = getString(R.string.accepted)
                binding.tvCancel.gone()
            }
            CallAction.PENDING -> {
                binding.tvStatus.text = getString(R.string.new_request)
            }
            CallAction.COMPLETED -> {
                binding.tvStatus.text = getString(R.string.done)
                binding.tvCancel.gone()
                binding.tvRate.visible()
                binding.tvApprove.visible()

                if (request.rating == null) {
                    binding.tvRate.text = getString(R.string.rate)
                    binding.tvRate.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                } else {
                    binding.tvRate.text = request.rating
                    binding.tvRate.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorRate))
                }

                if (request.userIsApproved == false) {
                    binding.tvApprove.visible()
                    binding.tvApprove.text = getString(R.string.approve)
                } else {
                    binding.tvApprove.gone()
                }
            }
            CallAction.START -> {
                binding.tvStatus.text = getString(R.string.inprogess)
                binding.tvTrack.visible()
                binding.tvCancel.gone()
            }
            CallAction.REACHED -> {
                binding.tvStatus.text = getString(R.string.reached_destination)
                binding.tvTrack.visible()
                binding.tvCancel.gone()
            }
            CallAction.START_SERVICE -> {
                binding.tvStatus.text = getString(R.string.started)
                binding.tvTrack.visible()
                binding.tvCancel.gone()
            }
            CallAction.FAILED -> {
                binding.tvStatus.text = getString(R.string.no_show)
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNoShow))
            }
            CallAction.CANCELED -> {
                binding.tvStatus.text = getString(R.string.canceled)
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNoShow))
                binding.tvCancel.gone()
            }
            CallAction.CANCEL_SERVICE -> {
                binding.tvStatus.text = getString(R.string.canceled_service)
                binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorNoShow))
                binding.tvCancel.gone()
            }
            else -> {
                binding.tvStatus.text = getString(R.string.new_request)
            }
        }
    }

    private fun checkStatus() {
//        item.status = CallAction.REACHED
        when (request.status) {
            CallAction.START, CallAction.REACHED ->
                startActivityForResult(
                        Intent(requireActivity(), AppointmentStatusActivity::class.java)
                                .putExtra(EXTRA_REQUEST_ID, request.id), AppRequestCode.APPOINTMENT_DETAILS
                )
            CallAction.START_SERVICE ->
                startActivityForResult(
                        Intent(requireContext(), DrawerActivity::class.java)
                                .putExtra(PAGE_TO_OPEN, DrawerActivity.UPDATE_SERVICE)
                                .putExtra(EXTRA_REQUEST_ID, request.id), AppRequestCode.APPOINTMENT_DETAILS
                )
        }
    }


    private fun cancelAppointment() {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(),
                R.string.cancel_appointment,
                R.string.cancel_appointment_msg,
                R.string.cancel_appointment,
                R.string.cancel,
                false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        if (isConnectedToInternet(requireContext(), true)) {
                            val hashMap = HashMap<String, String>()
                            hashMap["request_id"] = request.id ?: ""
                            viewModel.cancelRequest(hashMap)
                        }
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun bindObservers() {
        viewModel.requestDetail.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.setBackgroundResource(0)
                    binding.clLoader.gone()
                    request = it.data?.request_detail ?: Request()
                    setData()

                }
                Status.ERROR -> {
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.visible()
                }
            }
        })

        viewModel.cancelRequest.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    hitApi()
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
            if (requestCode == AppRequestCode.APPOINTMENT_DETAILS) {
                requireActivity().setResult(Activity.RESULT_OK)
                hitApi()
            }
        }
    }

}
