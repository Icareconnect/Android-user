package com.consultantapp.ui.dashboard.doctor.schedule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.DatesAvailability
import com.consultantapp.data.models.responses.Interval
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.FragmentScheduleBinding
import com.consultantapp.ui.dashboard.doctor.confirm.ConfirmBookingFragment
import com.consultantapp.ui.dashboard.home.bookservice.datetime.DatesAdapter
import com.consultantapp.utils.*
import com.consultantapp.utils.DateUtils.dateFormatFromMillis
import dagger.android.support.DaggerFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ScheduleFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentScheduleBinding

    private var rootView: View? = null

    private lateinit var viewModel: GetSlotsViewModel

    private var itemDays = ArrayList<DatesAvailability>()

    private var itemIntervalAll = ArrayList<Interval>()

    private var itemIntervalMorning = ArrayList<Interval>()

    private var itemIntervalAfternoon = ArrayList<Interval>()

    private var itemIntervalEvening = ArrayList<Interval>()

    private lateinit var datesAdapter: DatesAdapter

    private lateinit var intervalAdapterMorning: IntervalAdapter

    private lateinit var intervalAdapterAfternoon: IntervalAdapter

    private lateinit var intervalAdapterEvening: IntervalAdapter

    private var dateSelected = DatesAvailability()

    private var doctorData: UserData? = null

    private var selectedSlotTime = 0


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding =
                    DataBindingUtil.inflate(inflater, R.layout.fragment_schedule, container, false)
            rootView = binding.root

            initialise()
            listeners()
            bindObservers()
            setDatesAdapter()
            setIntervalAdapter()

        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[GetSlotsViewModel::class.java]
        doctorData = requireActivity().intent.getSerializableExtra(USER_DATA) as UserData

        binding.tvName.text = getDoctorName(doctorData)
        binding.tvDesc.text = doctorData?.categoryData?.name
                ?: binding.root.context.getString(R.string.na)
        loadImage(binding.ivPic, doctorData?.profile_image, R.drawable.ic_profile_placeholder)
    }


    private fun setDatesAdapter() {
        itemDays.clear()
        var calendar: Calendar
        var date: DatesAvailability
        for (i in 0..30) {
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

        hitApi(dateFormatFromMillis(DateFormat.DATE_FORMAT, itemDays[1].date ?: 0))
        dateSelected = itemDays[1]
    }

    private fun setIntervalAdapter() {
        intervalAdapterMorning = IntervalAdapter(this, itemIntervalMorning)
        binding.rvListingMorning.adapter = intervalAdapterMorning

        intervalAdapterAfternoon = IntervalAdapter(this, itemIntervalAfternoon)
        binding.rvListingAfternoon.adapter = intervalAdapterAfternoon

        intervalAdapterEvening = IntervalAdapter(this, itemIntervalEvening)
        binding.rvListingEvening.adapter = intervalAdapterEvening
    }


    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.tvMorning.setOnClickListener {
            setDataIntervals(SlotTime.MORNING)
        }

        binding.tvAfternoon.setOnClickListener {
            setDataIntervals(SlotTime.AFTERNOON)
        }

        binding.tvEvening.setOnClickListener {
            setDataIntervals(SlotTime.EVENING)
        }
    }


    private fun hitApi(date: String) {
        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            hashMap["doctor_id"] = doctorData?.id ?: ""
            hashMap["date"] = date
            hashMap["category_id"] = doctorData?.categoryData?.id ?: ""
            hashMap["service_id"] = requireActivity().intent.getStringExtra(SERVICE_ID) ?: ""

            viewModel.getSlots(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.getSlots.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()

                    selectedSlotTime = 0
                    itemIntervalAll.clear()
                    itemIntervalAll.addAll(it.data?.interval ?: emptyList())

                    differIntervals()
                }
                Status.ERROR -> {
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    binding.clLoader.setBackgroundColor(
                            ContextCompat.getColor(
                                    requireContext(),
                                    R.color.colorWhite
                            )
                    )
                    binding.clLoader.visible()
                }
            }
        })
    }

    private fun differIntervals() {
        itemIntervalMorning.clear()
        itemIntervalAfternoon.clear()
        itemIntervalEvening.clear()

        val sdf = SimpleDateFormat(DateFormat.TIME_FORMAT, Locale.ENGLISH)

        val timeMorning = sdf.parse("12:00 pm")
        val timeAfternoon = sdf.parse("4:00 pm")
        var intervalTime: Date

        /*Pick out interval for morning, afternoon, evening*/
        itemIntervalAll.forEach {
            intervalTime = sdf.parse(it.time)

            when {
                intervalTime.before(timeMorning) -> {
                    itemIntervalMorning.add(it)
                }
                intervalTime.before(timeAfternoon) -> {
                    itemIntervalAfternoon.add(it)
                }
                else -> {
                    itemIntervalEvening.add(it)
                }
            }
        }

        setDataIntervals(SlotTime.MORNING)

    }

    private fun setDataIntervals(interval: Int) {
        if (selectedSlotTime != interval) {
            /*Set previous values unselected*/
            when (selectedSlotTime) {
                SlotTime.MORNING -> {
                    itemIntervalMorning.forEachIndexed { index, _ ->
                        itemIntervalMorning[index].isSelected = false
                    }
                    intervalAdapterMorning.notifyDataSetChanged()
                }
                SlotTime.AFTERNOON -> {
                    itemIntervalAfternoon.forEachIndexed { index, _ ->
                        itemIntervalAfternoon[index].isSelected = false
                    }
                    intervalAdapterAfternoon.notifyDataSetChanged()
                }
                SlotTime.EVENING -> {
                    itemIntervalEvening.forEachIndexed { index, _ ->
                        itemIntervalEvening[index].isSelected = false
                    }
                    intervalAdapterEvening.notifyDataSetChanged()
                }
            }

/*Set updated value*/
            selectedSlotTime = interval

            binding.ivMorning.rotation = 0f
            binding.ivAfternoon.rotation = 0f
            binding.ivEvening.rotation = 0f
            binding.rvListingMorning.gone()
            binding.rvListingAfternoon.gone()
            binding.rvListingEvening.gone()
            binding.tvNoDataMorning.gone()
            binding.tvNoDataAfternoon.gone()
            binding.tvNoDataEvening.gone()

            /*Show views acc to selected position*/
            when (interval) {
                SlotTime.MORNING -> {
                    binding.ivMorning.rotation = 180f
                    binding.rvListingMorning.visible()
                    intervalAdapterMorning.notifyDataSetChanged()
                    binding.tvNoDataMorning.hideShowView(itemIntervalMorning.isEmpty())
                }
                SlotTime.AFTERNOON -> {
                    binding.ivAfternoon.rotation = 180f
                    binding.rvListingAfternoon.visible()
                    intervalAdapterAfternoon.notifyDataSetChanged()
                    binding.tvNoDataAfternoon.hideShowView(itemIntervalAfternoon.isEmpty())
                }
                SlotTime.EVENING -> {
                    binding.ivEvening.rotation = 180f
                    binding.rvListingEvening.visible()
                    intervalAdapterEvening.notifyDataSetChanged()
                    binding.tvNoDataEvening.hideShowView(itemIntervalEvening.isEmpty())
                }
            }
        }
    }


    fun onDateSelected(item: DatesAvailability) {
        binding.rvWeek.smoothScrollToPosition(itemDays.indexOf(item))

        dateSelected = item
        hitApi(dateFormatFromMillis(DateFormat.DATE_FORMAT, item.date ?: 0))
    }

    fun onIntervalSelected(item: Interval) {

        val fragment = ConfirmBookingFragment()
        val bundle = Bundle()
        bundle.putLong(ConfirmBookingFragment.DATE_SELECTED, dateSelected.date ?: 0)
        bundle.putString(ConfirmBookingFragment.TIME_SELECTED, item.time)
        fragment.arguments = bundle

        replaceFragment(
                requireActivity().supportFragmentManager,
                fragment, R.id.container
        )
    }

    companion object {
        const val SERVICE_ID = "SERVICE_ID"
    }

    object SlotTime {
        const val MORNING = 1
        const val AFTERNOON = 2
        const val EVENING = 3
    }
}
