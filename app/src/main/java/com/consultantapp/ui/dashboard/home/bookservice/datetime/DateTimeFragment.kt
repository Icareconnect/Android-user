package com.consultantapp.ui.dashboard.home.bookservice.datetime

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.appClientDetails
import com.consultantapp.data.models.requests.BookService
import com.consultantapp.data.models.requests.DatesAvailability
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentDateTimeBinding
import com.consultantapp.ui.dashboard.DoctorViewModel
import com.consultantapp.ui.dashboard.doctor.listing.DoctorListActivity
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.ui.drawermenu.addmoney.AddMoneyActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

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

    private lateinit var viewModel: DoctorViewModel

    private var itemDays = ArrayList<DatesAvailability>()

    private lateinit var datesAdapter: DatesAdapter

    private var bookService = BookService()

    private var hashMap = HashMap<String, Any>()


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
        viewModel = ViewModelProvider(this, viewModelFactory)[DoctorViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        editTextScroll(binding.etReason)

        bookService = arguments?.getSerializable(EXTRA_REQUEST_ID) as BookService

    }

    private fun setDatesAdapter() {
        itemDays.clear()
        var calendar: Calendar
        var date: DatesAvailability
        for (i in 0..60) {
            calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, i)

            date = DatesAvailability()
            date.displayName =
                    calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            date.date = calendar.timeInMillis

            if (i == 1) {
                date.isSelected = true

                binding.tvMonth.text = DateUtils.dateFormatFromMillis(DateFormat.MONTH_YEAR, date.date)
            }
            itemDays.add(date)

            datesAdapter = DatesAdapter(this, itemDays)
            binding.rvWeek.adapter = datesAdapter
        }

        binding.rvWeek.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvWeek.layoutManager as LinearLayoutManager
                val midItemPosition = layoutManager.findLastVisibleItemPosition() - 4

                binding.tvMonth.text = DateUtils.dateFormatFromMillis(DateFormat.MONTH_YEAR, itemDays[midItemPosition].date)
            }
        })

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
            var dateSelected = ""

            if (itemDays[0].isSelected && binding.tvStartTimeV.text.isNotEmpty()) {
                val sdf = SimpleDateFormat(DateFormat.TIME_FORMAT, Locale.ENGLISH)
                val calendar = Calendar.getInstance(Locale.getDefault())
                calendar.add(Calendar.HOUR, appClientDetails.booking_delay ?: 1)
                val newTime = sdf.parse(sdf.format(calendar.time))

                val timeCompare = sdf.parse(binding.tvStartTimeV.text.toString())
                if (timeCompare.before(newTime)) {
                    binding.tvStartTimeV.showSnackBar(getString(R.string.error_for_today, (appClientDetails.booking_delay
                            ?: 1).toString()))
                    return@setOnClickListener
                }
            }

            var noOfDaysSelected = 0
            itemDays.forEach {
                if (it.isSelected) {
                    dateSelected += "${DateUtils.dateFormatFromMillis(DateFormat.DATE_FORMAT, it.date ?: 0L)}, "
                    noOfDaysSelected++
                }
            }


            when {
                dateSelected.isNullOrEmpty() -> {
                    binding.tvAppointments.showSnackBar(getString(R.string.select_date))
                }
                binding.tvStartTimeV.text.toString().trim().isEmpty() -> {
                    binding.tvStartTimeV.showSnackBar(getString(R.string.start_time))
                }
                binding.tvEndTimeV.text.toString().trim().isEmpty() -> {
                    binding.tvEndTimeV.showSnackBar(getString(R.string.end_time))
                }
                /* binding.etReason.text.toString().trim().isEmpty() -> {
                     binding.etReason.showSnackBar(getString(R.string.reason_of_service))
                 }*/
                else -> {
                    val sdf = SimpleDateFormat(DateFormat.TIME_FORMAT, Locale.ENGLISH)
                    val startTime = sdf.parse(binding.tvStartTimeV.text.toString().trim())
                    val endTime = sdf.parse(binding.tvEndTimeV.text.toString().trim())

                    val difference = ((endTime.time - startTime.time) / (60 * 60 * 1000) * noOfDaysSelected)

                    if (difference < 4) {
                        binding.tvStartTimeV.showSnackBar(getString(R.string.error_for_booking_4_hours))
                        return@setOnClickListener
                    }

                    bookService.date = dateSelected.removeSuffix(", ")
                    bookService.startTime = binding.tvStartTimeV.text.toString()
                    bookService.endTime = binding.tvEndTimeV.text.toString()
                    bookService.reason = binding.etReason.text.toString()

                    if (isConnectedToInternet(requireContext(), true)) {
                        if (requireActivity().intent.getStringExtra(PAGE_TO_OPEN) == DrawerActivity.BOOK_AGAIN) {
                            /*hashMap = HashMap()
                            viewModel.createRequest(hashMap)*/
                        } else
                            startActivityForResult(Intent(requireContext(), DoctorListActivity::class.java)
                                    .putExtra(EXTRA_REQUEST_ID, bookService), AppRequestCode.APPOINTMENT_BOOKING)
                    }

                }
            }

        }
    }

    private fun bindObservers() {
        viewModel.createRequest.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    startActivityForResult(Intent(requireContext(), AddMoneyActivity::class.java)
                            .putExtra(EXTRA_PRICE, it.data?.grand_total)
                            .putExtra(EXTRA_REQUEST_ID, hashMap), AppRequestCode.ADD_MONEY)
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
            /*If today date is selected*/
            if (itemDays[0].isSelected && time.first.isNotEmpty()) {
                val sdf = SimpleDateFormat(DateFormat.TIME_FORMAT, Locale.ENGLISH)
                val calendar = Calendar.getInstance(Locale.getDefault())
                calendar.add(Calendar.HOUR, appClientDetails.booking_delay ?: 1)
                val newTime = sdf.parse(sdf.format(calendar.time))

                val timeCompare = sdf.parse(time.first)
                if (timeCompare.before(newTime)) {
                    binding.tvStartTimeV.showSnackBar(getString(R.string.error_for_today, (appClientDetails.booking_delay
                            ?: 1).toString()))
                    return
                }
            }

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
        //dateSelected = item.date
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.APPOINTMENT_BOOKING) {
                requireActivity().finish()
            }
        }
    }

}

interface OnTimeSelected {
    fun onTimeSelected(time: Triple<String, Boolean, Boolean>)
}
