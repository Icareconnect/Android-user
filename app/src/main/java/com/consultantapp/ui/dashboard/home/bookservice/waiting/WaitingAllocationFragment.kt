package com.consultantapp.ui.dashboard.home.bookservice.waiting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.BookService
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentWaitingAllocationBinding
import com.consultantapp.ui.dashboard.home.bookservice.AllocateDoctorViewModel
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
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

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AllocateDoctorViewModel

    private var rootView: View? = null

    private var bookService = BookService()

    private val hashMap = HashMap<String, String>()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_waiting_allocation, container, false)
            rootView = binding.root

            initialise()
            bindObservers()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[AllocateDoctorViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        bookService = arguments?.getSerializable(EXTRA_REQUEST_ID) as BookService

        if (isConnectedToInternet(requireContext(), true)) {

            hashMap["category_id"] = "1"
            hashMap["filter_id"] = bookService.filter_id ?: ""
           /* hashMap["date"] = DateUtils.dateFormatFromMillis(DateFormat.DATE_FORMAT, bookService.date)
            hashMap["end_date"] = DateUtils.dateFormatFromMillis(DateFormat.DATE_FORMAT, bookService.date)*/
            hashMap["time"] = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                    DateFormat.TIME_FORMAT_24, bookService.startTime ?: "")
            hashMap["end_time"] = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                    DateFormat.TIME_FORMAT_24, bookService.endTime ?: "")
            hashMap["reason_for_service"] = bookService.reason ?: ""

            hashMap["schedule_type"] = RequestType.SCHEDULE

            hashMap["lat"] = bookService.address?.location?.get(1).toString()
            hashMap["long"] = bookService.address?.location?.get(0).toString()
            hashMap["service_address"] = bookService.address?.address_name ?: ""

            hashMap["first_name"] = bookService.personName
            hashMap["last_name"] = bookService.personName
            hashMap["service_for"] = bookService.service_for ?: ""
            hashMap["home_care_req"] = bookService.service_type ?: ""

            viewModel.autoAllocate(hashMap)
        }

    }

    private fun bindObservers() {
        viewModel.confirmAutoAllocate.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    viewModel.autoAllocate(hashMap)
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

        viewModel.autoAllocate.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    val fragment = DialogAllocatedNurseFragment(this, it.data?.doctor_data)
                    fragment.show(requireActivity().supportFragmentManager, fragment.tag)

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
