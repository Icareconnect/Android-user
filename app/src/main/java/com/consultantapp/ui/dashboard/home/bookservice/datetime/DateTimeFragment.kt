package com.consultantapp.ui.dashboard.home.bookservice.datetime

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.requests.BookService
import com.consultantapp.data.models.requests.DatesAvailability
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentDateTimeBinding
import com.consultantapp.databinding.FragmentRegisterServiceBinding
import com.consultantapp.ui.dashboard.home.bookservice.datetime.DatesAdapter
import com.consultantapp.utils.*
import dagger.android.support.DaggerFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DateTimeFragment : DaggerFragment(), OnTimeSelected {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentDateTimeBinding

    private var rootView: View? = null

    private var itemDays = ArrayList<DatesAvailability>()

    private lateinit var datesAdapter: DatesAdapter

    private var dateSelected: Long? = null


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
        editTextScroll(binding.etReason)
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
                dateSelected = date.date
            }
            itemDays.add(date)
        }

        datesAdapter = DatesAdapter(this, itemDays)
        binding.rvWeek.adapter = datesAdapter


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
                    val bookService = BookService()
                    bookService.date = dateSelected
                    bookService.startTime = binding.tvStartTimeV.text.toString()
                    bookService.endTime = binding.tvEndTimeV.text.toString()
                    bookService.reason = binding.etReason.text.toString()

                    val intent = Intent()
                    intent.putExtra(EXTRA_REQUEST_ID, bookService)
                    requireActivity().setResult(Activity.RESULT_OK, intent)
                    requireActivity().finish()

                    /* val intent = Intent()
                     intent.putExtra(EXTRA_REQUEST_ID, bookService)
                     resultFragmentIntent(this, targetFragment ?: this,
                             AppRequestCode.ADD_DATE, intent)*/
                }
            }

        }
    }

    private fun bindObservers() {

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
