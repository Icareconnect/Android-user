package com.consultantapp.ui.dashboard.appointment.rating

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.databinding.FragmentAddRatingBinding
import com.consultantapp.ui.dashboard.appointment.AppointmentViewModel
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class AddRatingFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var binding: FragmentAddRatingBinding

    private lateinit var viewModel: AppointmentViewModel

    private lateinit var progressDialog: ProgressDialog

    private var rootView: View? = null

    private lateinit var userData: UserData


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_rating, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
        }
        return rootView
    }


    private fun initialise() {
        progressDialog = ProgressDialog(requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]

        userData = requireActivity().intent.getSerializableExtra(USER_DATA) as UserData

        binding.tvName.text = getDoctorName(userData)
        loadImage(binding.ivPic, userData.profile_image,
            R.drawable.ic_profile_placeholder)

        editTextScroll(binding.etDescription)
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.tvSubmit.setOnClickListener {
            binding.tvSubmit.hideKeyboard()

            when {
                binding.ratingBar.rating <= 0 -> {
                    binding.ratingBar.showSnackBar(getString(R.string.error_select_rating))
                }
                else -> {
                    if (isConnectedToInternet(requireContext(), true)) {
                        val hashMap = HashMap<String, String>()
                        hashMap["consultant_id"] = userData.id ?: ""
                        if (requireActivity().intent.hasExtra(EXTRA_REQUEST_ID))
                            hashMap["request_id"] = requireActivity().intent.getStringExtra(EXTRA_REQUEST_ID) ?:""
                        hashMap["rating"] = binding.ratingBar.rating.toString()
                        hashMap["review"] = binding.etDescription.text.toString().trim()

                        viewModel.addReview(hashMap)
                    }
                }
            }
        }
    }

    private fun bindObservers() {
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
    }
}
