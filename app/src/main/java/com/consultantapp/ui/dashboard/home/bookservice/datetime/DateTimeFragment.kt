package com.consultantapp.ui.dashboard.home.bookservice.datetime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.BookService
import com.consultantapp.data.models.requests.DatesAvailability
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentDateTimeBinding
import com.consultantapp.ui.dashboard.home.bookservice.AllocateDoctorViewModel
import com.consultantapp.ui.dashboard.home.bookservice.waiting.WaitingAllocationFragment
import com.consultantapp.utils.*
import androidx.lifecycle.Observer
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class DateTimeFragment : DaggerFragment(), OnTimeSelected {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentDateTimeBinding

    private var rootView: View? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: AllocateDoctorViewModel

    private var itemDays = ArrayList<DatesAvailability>()

    private lateinit var datesAdapter: DatesAdapter

    private var dateSelected: Long? = null

    private var bookService = BookService()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_date_time, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            setDatesAdapter()
        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[AllocateDoctorViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        editTextScroll(binding.etReason)

        bookService = arguments?.getSerializable(EXTRA_REQUEST_ID) as BookService

    }

    private fun setDatesAdapter() {
        itemDays.clear()
        var calendar: Calendar
        var date: DatesAvailability
        for (i in 0..30) {
            calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, i)

            date = DatesAvailability()
            date.displayName =
                    calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            date.date = calendar.timeInMillis

            if (i == 1) {
                date.isSelected = true
                dateSelected = date.date

                binding.tvMonth.text = DateUtils.dateFormatFromMillis(DateFormat.MONTH_YEAR, date.date)
            }
            itemDays.add(date)

            datesAdapter = DatesAdapter(this, itemDays)
            binding.rvWeek.adapter = datesAdapter
        }

        /* binding.rvWeek.addOnScrollListener(object : RecyclerView.OnScrollListener() {
             override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                 super.onScrolled(recyclerView, dx, dy)

                 val layoutManager = binding.rvWeek.layoutManager as LinearLayoutManager
                 val midItemPosition = layoutManager.findLastVisibleItemPosition() - 4

                 binding.tvMonth.text = DateUtils.dateFormatFromMillis(DateFormat.MONTH_YEAR, itemDays[midItemPosition].date)
             }
         })*/
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.tvStartTimeV.setOnClickListener {
            DateUtils.getTime(requireContext(), binding.tvStartTimeV.text.toString(),
                    binding.tvEndTimeV.text.toString(), isStart = true, listener = this)
        }
        binding.tvEndTimeV.setOnClickListener {
            DateUtils.getTime(requireContext(), binding.tvStartTimeV.text.toString(),
                    binding.tvEndTimeV.text.toString(), isStart = false, listener = this)
        }

        binding.tvBookAppointment.setOnClickListener {
            when {
                dateSelected == null -> {
                    binding.tvAppointments.showSnackBar(getString(R.string.select_date))
                }
                binding.tvStartTimeV.text.toString().trim().isEmpty() -> {
                    binding.tvStartTimeV.showSnackBar(getString(R.string.start_time))
                }
                binding.tvEndTimeV.text.toString().trim().isEmpty() -> {
                    binding.tvEndTimeV.showSnackBar(getString(R.string.end_time))
                }
                binding.etReason.text.toString().trim().isEmpty() -> {
                    binding.etReason.showSnackBar(getString(R.string.reason_of_service))
                }
                else -> {
                    bookService.date = dateSelected
                    bookService.startTime = binding.tvStartTimeV.text.toString()
                    bookService.endTime = binding.tvEndTimeV.text.toString()
                    bookService.reason = binding.etReason.text.toString()


                    if (isConnectedToInternet(requireContext(), true)) {

                        val fragment = WaitingAllocationFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(EXTRA_REQUEST_ID, bookService)
                        fragment.arguments = bundle
                        replaceFragment(requireActivity().supportFragmentManager, fragment, R.id.container)

                        /*val hashMap = HashMap<String, String>()
                        hashMap["category_id"] = "1"
                        hashMap["filter_id"] = bookService.filter_id ?: ""
                        hashMap["date"] = DateUtils.dateFormatFromMillis(DateFormat.DATE_FORMAT, bookService.date)
                        hashMap["end_date"] = DateUtils.dateFormatFromMillis(DateFormat.DATE_FORMAT, bookService.date)
                        hashMap["time"] = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                                DateFormat.TIME_FORMAT_24, bookService.startTime ?: "")
                        hashMap["end_time"] = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                                DateFormat.TIME_FORMAT_24, bookService.endTime ?: "")
                        hashMap["reason_for_service"] = bookService.reason ?: ""

                        hashMap["schedule_type"] = RequestType.SCHEDULE

                        hashMap["lat"] = bookService.address?.location?.get(1).toString()
                        hashMap["long"] = bookService.address?.location?.get(0).toString()
                        hashMap["service_address"] = bookService.address?.locationName ?: ""

                        hashMap["first_name"] = bookService.personName
                        hashMap["last_name"] = bookService.personName
                        hashMap["service_for"] = bookService.service_for ?: ""
                        hashMap["home_care_req"] = bookService.home_care_req ?: ""

                        viewModel.confirmAutoAllocate(hashMap)*/
                    }

                }
            }

        }
    }

    private fun bindObservers() {
        viewModel.confirmAutoAllocate.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    requireActivity().supportFragmentManager.popBackStack()
                    requireActivity().supportFragmentManager.popBackStack()

                    val fragment = WaitingAllocationFragment()
                    val bundle = Bundle()
                    bundle.putSerializable(EXTRA_REQUEST_ID, bookService)
                    fragment.arguments = bundle
                    replaceFragment(requireActivity().supportFragmentManager, fragment, R.id.container)

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

    override fun onTimeSelected(time: Triple<String, Boolean, Boolean>) {
        if (!time.third) {
            val sdf = SimpleDateFormat(DateFormat.TIME_FORMAT, Locale.ENGLISH)

            if (time.second)
                binding.tvStartTimeV.text = time.first
            else
                binding.tvEndTimeV.text = time.first

        } else {
            binding.tvStartTimeV.showSnackBar(getString(R.string.greater_time))
        }
    }

    fun onDateSelected(item: DatesAvailability) {
        binding.rvWeek.smoothScrollToPosition(itemDays.indexOf(item))
        binding.tvMonth.text = DateUtils.dateFormatFromMillis(DateFormat.MONTH_YEAR, item.date)
        dateSelected = item.date
    }

}

interface OnTimeSelected {
    fun onTimeSelected(time: Triple<String, Boolean, Boolean>)
}
