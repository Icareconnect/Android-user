package com.consultantapp.ui.dashboard.home.bookservice.datetime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.requests.DatesAvailability
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentDateTimeBinding
import com.consultantapp.databinding.FragmentRegisterServiceBinding
import com.consultantapp.ui.dashboard.home.bookservice.datetime.DatesAdapter
import com.consultantapp.utils.DateFormat
import com.consultantapp.utils.DateUtils
import com.consultantapp.utils.PrefsManager
import com.consultantapp.utils.showSnackBar
import dagger.android.support.DaggerFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DateTimeFragment : DaggerFragment(), OnTimeSelected  {

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
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setDatesAdapter() {
        itemDays.clear()
        var calendar: Calendar
        var date: DatesAvailability
        for (i in 0..60) {
            calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_MONTH, i)

            date = DatesAvailability()
            if (i == 1) {
                date.isSelected = true
            }
            date.displayName =
                    calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
            date.date = calendar.timeInMillis
            itemDays.add(date)
        }

        datesAdapter = DatesAdapter(this, itemDays)
        binding.rvWeek.adapter = datesAdapter


        binding.rvWeek.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvWeek.layoutManager as LinearLayoutManager
                val midItemPosition = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition()

                binding.tvMonth.text = DateUtils.dateFormatFromMillis(DateFormat.MONTH_YEAR, itemDays[midItemPosition].date)
            }
        })
    }

    private fun listeners() {
        binding.tvStartTimeV.setOnClickListener {
            DateUtils.getTime(requireContext(), binding.tvStartTimeV.text.toString(),
                    binding.tvEndTimeV.text.toString(), isStart = true, listener = this)
        }
        binding.tvEndTimeV.setOnClickListener {
            DateUtils.getTime(requireContext(), binding.tvStartTimeV.text.toString(),
                    binding.tvEndTimeV.text.toString(), isStart = false, listener = this)
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
        //dateSelected = item
    }

}

interface OnTimeSelected {
    fun onTimeSelected(time: Triple<String, Boolean, Boolean>)
}
