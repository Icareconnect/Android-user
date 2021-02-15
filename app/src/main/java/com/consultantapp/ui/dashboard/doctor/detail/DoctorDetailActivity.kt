package com.consultantapp.ui.dashboard.doctor.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantapp.R
import com.consultantapp.data.models.requests.BookService
import com.consultantapp.data.models.responses.Filter
import com.consultantapp.data.models.responses.Review
import com.consultantapp.data.models.responses.Service
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PER_PAGE_LOAD
import com.consultantapp.data.network.PushType
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityDoctorDetailBinding
import com.consultantapp.ui.dashboard.DoctorViewModel
import com.consultantapp.ui.dashboard.doctor.DoctorActionActivity
import com.consultantapp.ui.dashboard.doctor.schedule.ScheduleFragment.Companion.SERVICE_ID
import com.consultantapp.ui.drawermenu.addmoney.AddMoneyActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class DoctorDetailActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    lateinit var binding: ActivityDoctorDetailBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: DoctorViewModel

    private lateinit var adapter: RatingAdapter

    private var items = ArrayList<Review>()

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var doctorId = ""

    private var doctorData: UserData? = null

    private var serviceSelected: Service? = null

    private var hashMap = HashMap<String, Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_doctor_detail)

        initialise()
        listeners()
        setAdapter()
        bindObservers()
        hiApiDoctorDetail()
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[DoctorViewModel::class.java]
        progressDialog = ProgressDialog(this)

        doctorId = intent.getStringExtra(DOCTOR_ID) ?: ""

        binding.clLoader.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhite))
    }

    private fun hiApiDoctorDetail() {
        if (isConnectedToInternet(this, true)) {
            val hashMap = HashMap<String, String>()
            hashMap["doctor_id"] = doctorId
            viewModel.doctorDetails(hashMap)

            viewModel.reviewList(hashMap)

        }
    }


    fun hiApiDoctorRequest(schedule: Boolean, service: Service) {
        if (schedule) {
            startActivity(Intent(this, DoctorActionActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, RequestType.SCHEDULE)
                    .putExtra(SERVICE_ID, service.service_id)
                    .putExtra(USER_DATA, doctorData))
        } else {

            if (isConnectedToInternet(this, true)) {
                val hashMap = HashMap<String, Any>()

                hashMap["consultant_id"] = doctorId
                hashMap["service_id"] = service.service_id ?: ""
                hashMap["schedule_type"] = RequestType.INSTANT

                viewModel.confirmRequest(hashMap)
            }

        }
    }

    private fun bindObservers() {
        viewModel.doctorDetails.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.ivMark.visible()

                    doctorData = it.data?.dcotor_detail
                    setDoctorData()

                }
                Status.ERROR -> {
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    binding.ivMark.gone()
                    binding.clLoader.visible()
                }
            }
        })

        viewModel.reviewList.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {

                    isLoadingMoreItems = false

                    val tempList = it.data?.review_list ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                    }

                    items.addAll(tempList)
                    adapter.notifyDataSetChanged()

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {

                }
            }
        })

        viewModel.createRequest.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    startActivityForResult(Intent(this, AddMoneyActivity::class.java)
                            .putExtra(EXTRA_PRICE, it.data?.grand_total)
                            .putExtra(EXTRA_REQUEST_ID, hashMap), AppRequestCode.ADD_MONEY)
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })

    }

    private fun setDoctorData() {
        binding.tvName.text = getDoctorName(doctorData)
        loadImage(binding.ivPic, doctorData?.profile_image,
                R.drawable.image_placeholder)

        binding.tvApproved.text = "${getString(R.string.approved)} :" +
                " ${DateUtils.dateTimeFormatFromUTC(DateFormat.MON_YEAR_FORMAT, doctorData?.account_verified_at)}"
        binding.tvAboutV.text = doctorData?.profile?.bio ?: getString(R.string.na)
        binding.tvDesc.text = doctorData?.categoryData?.name ?: getString(R.string.na)

        binding.tvRating.text = getString(
                R.string.s_s_reviews,
                getUserRating(doctorData?.totalRating),
                doctorData?.reviewCount
        )
        binding.tvLocation.text = doctorData?.profile?.location_name
                ?: getString(R.string.na)
        binding.tvRate.text = getString(R.string.price_ss, getCurrency(doctorData?.price))

        binding.tvPatient.gone()
        binding.tvPatientV.gone()
        if (doctorData?.patientCount.isNullOrEmpty() || doctorData?.patientCount == "0") {
            binding.tvPatient.gone()
            binding.tvPatientV.gone()
        } else
            binding.tvPatientV.text = doctorData?.patientCount ?: getString(R.string.na)

        binding.tvReviewsV.text = doctorData?.reviewCount ?: getString(R.string.na)
        binding.tvReviewCount.text = getUserRating(doctorData?.totalRating)

        if (doctorData?.services?.isNotEmpty() == true) {
            val service = doctorData?.services?.get(0)
            binding.tvRate.text = getString(R.string.price_s, getCurrency(service?.price),
                    getUnitPrice(service?.unit_price, this))
        }


        doctorData?.custom_fields?.forEach {
            when (it.field_name) {
                CustomFields.WORK_EXPERIENCE -> {
                    binding.tvExperienceV.text = it.field_value
                }
            }
        }

        var specialisation = ""
        doctorData?.filters?.forEach {
            it.options?.forEach {
                if (it.isSelected) {
                    specialisation += "${it.option_name}, "
                }
            }
        }
        binding.tvExpertiseV.text = specialisation.removeSuffix(", ")
        binding.tvExpertiseV.hideShowView(specialisation.isNotEmpty())
        binding.tvExpertise.hideShowView(specialisation.isNotEmpty())

        doctorData?.custom_fields?.forEach {
            when (it.field_name) {
                CustomFields.WORK_EXPERIENCE -> {
                    binding.tvExperienceV.text = it.field_value
                }
            }
        }


        val covid = ArrayList<Filter>()
        val personalInterest = ArrayList<Filter>()
        val providableServices = ArrayList<Filter>()
        val workExperience = ArrayList<Filter>()
        doctorData?.master_preferences?.forEach {
            when (it.preference_type) {
                PreferencesType.COVID ->
                    covid.add(it)
                PreferencesType.PERSONAL_INTEREST ->
                    personalInterest.add(it)
                PreferencesType.WORK_ENVIRONMENT ->
                    workExperience.add(it)
                PreferencesType.PROVIDABLE_SERVICES ->
                    providableServices.add(it)
            }
        }

        if (covid.isNotEmpty()) {
            var covidText = ""
            covid.forEach {
                covidText += it.preference_name + "\n"

                it.options?.forEach {
                    if (it.isSelected) {
                        covidText += it.option_name + "\n\n"
                    }
                }
            }
            binding.tvCovidV.text = covidText

            binding.tvCovid.hideShowView(covidText.isNotEmpty())
            binding.tvCovidV.hideShowView(covidText.isNotEmpty())
        } else {
            binding.tvCovid.gone()
            binding.tvCovidV.gone()
        }

        if (providableServices.isNotEmpty()) {
            var servicesText = ""
            providableServices.forEach {
                it.options?.forEach {
                    if (it.isSelected) {
                        servicesText += it.option_name + ", "
                    }
                }
            }
            binding.tvServicesV.text = servicesText.removeSuffix(", ")

            binding.tvServices.hideShowView(servicesText.isNotEmpty())
            binding.tvServicesV.hideShowView(servicesText.isNotEmpty())
        } else {
            binding.tvServices.gone()
            binding.tvServicesV.gone()
        }

        if (personalInterest.isNotEmpty()) {
            var personalText = ""
            personalInterest.forEach {
                it.options?.forEach {
                    if (it.isSelected) {
                        personalText += it.option_name + ", "
                    }
                }
            }
            binding.tvPersonalV.text = personalText.removeSuffix(", ")

            binding.tvPersonal.hideShowView(personalText.isNotEmpty())
            binding.tvPersonalV.hideShowView(personalText.isNotEmpty())
        } else {
            binding.tvPersonal.gone()
            binding.tvPersonalV.gone()
        }

        if (workExperience.isNotEmpty()) {
            var workText = ""
            workExperience.forEach {
                it.options?.forEach {
                    if (it.isSelected) {
                        workText += it.option_name + ", "
                    }
                }
            }
            binding.tvWorkV.text = workText.removeSuffix(", ")

            binding.tvWork.hideShowView(workText.isNotEmpty())
            binding.tvWorkV.hideShowView(workText.isNotEmpty())
        } else {
            binding.tvWork.gone()
            binding.tvWorkV.gone()
        }

        binding.tvWork.gone()
        binding.tvWorkV.gone()
        binding.tvCovid.gone()
        binding.tvCovidV.gone()
        binding.tvWork.gone()
        binding.tvWork.gone()
        binding.tvApproved.gone()
        binding.tvRating.gone()
        binding.tvPatient.gone()
        binding.tvPatientV.gone()
        binding.tvExperience.gone()
        binding.tvExperienceV.gone()
        binding.tvReviews.gone()
        binding.tvReviewsV.gone()
        binding.tvServices.gone()
        binding.tvServicesV.gone()
    }


    private fun setAdapter() {
        adapter = RatingAdapter(items)
        binding.rvReview.adapter = adapter
    }

    private fun listeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.ivMark.setOnClickListener {
            if (isConnectedToInternet(this, true)) {
                shareDeepLink(DeepLink.USER_PROFILE, this, doctorData)
            }
        }

        binding.tvBookAppointment.setOnClickListener {
            if (isConnectedToInternet(this, true)) {
                hashMap = HashMap()

                hashMap["consultant_id"] = doctorId
                hashMap["service_id"] = CATEGORY_SERVICE_ID
                hashMap["schedule_type"] = RequestType.SCHEDULE
                /*Mutiple date*/
                hashMap["request_type"] = "multiple"

                hashMap["request_step"] = "confirm"

                if (intent.hasExtra(EXTRA_REQUEST_ID)) {
                    val bookService = intent.getSerializableExtra(EXTRA_REQUEST_ID) as BookService
                    hashMap["filter_id"] = bookService.filter_id ?: ""
                    hashMap["duties"] = bookService.service_type ?: ""

                    hashMap["dates"] = bookService.date ?: ""
                    hashMap["start_time"] = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                            DateFormat.TIME_FORMAT_24, bookService.startTime ?: "")
                    hashMap["end_time"] = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                            DateFormat.TIME_FORMAT_24, bookService.endTime ?: "")

                    hashMap["lat"] = bookService.address?.location?.get(1).toString()
                    hashMap["long"] = bookService.address?.location?.get(0).toString()
                    hashMap["service_address"] = bookService.address?.address_name ?: ""
                    hashMap["address_id"] = bookService.address?.id ?:""

                    hashMap["first_name"] = bookService.personName
                    hashMap["last_name"] = bookService.personName
                    hashMap["service_for"] = bookService.service_for ?: ""
                    hashMap["home_care_req"] = bookService.service_type ?: ""
                    hashMap["reason_for_service"] = bookService.reason ?: ""
                    hashMap["country_code"] = bookService.country_code ?: ""
                    hashMap["phone_number"] = bookService.phone_number ?: ""
                }

                viewModel.createRequest(hashMap)
            }
        }
    }

    fun serviceClick(item: Service) {
        serviceSelected = item
        if (userRepository.isUserLoggedIn()) {
            bottomOption(item)
        }
    }

    private fun bottomOption(service: Service) {
        if (service.need_availability == "1") {
            val fragment = BottomRequestFragment(this, service)
            fragment.show(supportFragmentManager, fragment.tag)
        } else {
            hiApiDoctorRequest(false, service)
        }
    }

    private fun showCreateRequestDialog(service: Service) {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.create_request))
                .setMessage(getString(R.string.create_request_message))
                .setPositiveButton(getString(R.string.create_request)) { dialog, which ->
                    hiApiDoctorRequest(false, service)
                }.setNegativeButton(getString(R.string.cancel)) { dialog, which ->

                }.show()
    }


    companion object {
        const val DOCTOR_ID = "DOCTOR_ID"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ADD_MONEY) {
                val intentBroadcast = Intent()
                intentBroadcast.action = PushType.NEW_REQUEST
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)

                longToast(getString(R.string.request_sent))
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}
