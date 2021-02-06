package com.consultantapp.ui.dashboard.appointment.appointmentStatus

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.Request
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.databinding.FragmentRequestCompletedBinding
import com.consultantapp.ui.dashboard.appointment.AppointmentViewModel
import com.consultantapp.ui.drawermenu.DrawerActivity
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

    private lateinit var userData: UserData


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
        editTextScroll(binding.etReasonHour)
        progressDialog = ProgressDialog(requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]


        when (requireActivity().intent.getStringExtra(PAGE_TO_OPEN)) {
            DrawerActivity.APPROVE_HOUR -> {
                binding.clApproveHour.visible()
                binding.clSuccess.gone()
                binding.clFeedBack.gone()
            }
            DrawerActivity.RATE -> {
                binding.clFeedBack.visible()
                binding.clApproveHour.gone()
                binding.clSuccess.gone()
            }
            else -> {
                binding.clSuccess.visible()
                binding.clApproveHour.gone()
                binding.clFeedBack.gone()
            }
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
            binding.clSuccess.gone()
            binding.clApproveHour.visible()
        }

        binding.tvSubmit.setOnClickListener {
            binding.tvSubmit.hideKeyboard()

            when {
                binding.ratingBar.rating <= 0 -> {
                    binding.ratingBar.showSnackBar(getString(R.string.error_select_rating))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    val hashMap = HashMap<String, String>()
                    hashMap["consultant_id"] = userData.id ?: ""
                    if (requireActivity().intent.hasExtra(EXTRA_REQUEST_ID))
                        hashMap["request_id"] = requireActivity().intent.getStringExtra(EXTRA_REQUEST_ID)
                                ?: ""
                    hashMap["rating"] = binding.ratingBar.rating.toString()
                    hashMap["review"] = binding.etReason.text.toString().trim()

                    viewModel.addReview(hashMap)
                }
            }
        }

        binding.tvAccept.setOnClickListener {
            binding.tvAccept.hideKeyboard()
            approveHour(APPROVED)
        }

        binding.tvDecline.setOnClickListener {
            binding.tvDecline.hideKeyboard()
            approveHour(DECLINED)
        }
    }

    private fun approveHour(status: String) {
        when {
            (status == DECLINED && binding.etHour.text.toString().isEmpty()) -> {
                binding.etHour.showSnackBar(getString(R.string.error_working_hour))
            }
            binding.etReasonHour.text.toString().isEmpty() -> {
                binding.etReasonHour.showSnackBar(getString(R.string.error_working_hour_message))
            }
            isConnectedToInternet(requireContext(), true) -> {
                val hashMap = HashMap<String, String>()
                hashMap["request_id"] = requireActivity().intent.getStringExtra(EXTRA_REQUEST_ID)
                        ?: ""
                hashMap["status"] = status
                if (binding.etHour.text.toString().isNotEmpty())
                    hashMap["valid_hours"] = binding.etHour.text.toString().trim()
                hashMap["comment"] = binding.etReasonHour.text.toString().trim()

                viewModel.approveWorkingHour(hashMap)
            }
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
        userData = request.to_user ?: UserData()

        binding.etHour.setText((request.total_hours?.toInt() ?:"").toString())
    }

    private fun bindObservers() {
        viewModel.requestDetail.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
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

        viewModel.addReview.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
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

        viewModel.approveWorkingHour.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().setResult(Activity.RESULT_OK)

                    if (requireActivity().intent.getStringExtra(PAGE_TO_OPEN) == DrawerActivity.REQUEST_COMPLETE) {
                        binding.clApproveHour.gone()
                        binding.clFeedBack.visible()
                    } else
                        requireActivity().finish()
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

    companion object {
        const val APPROVED = "approved"
        const val DECLINED = "declined"
    }

}
