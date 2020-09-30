package com.consultantapp.ui.dashboard.doctor.detail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.BookService
import com.consultantapp.data.models.responses.Review
import com.consultantapp.data.models.responses.Service
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PER_PAGE_LOAD
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityDoctorDetailBinding
import com.consultantapp.ui.dashboard.DoctorViewModel
import com.consultantapp.ui.dashboard.doctor.DoctorActionActivity
import com.consultantapp.ui.dashboard.doctor.schedule.ScheduleFragment.Companion.SERVICE_ID
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.ui.drawermenu.DrawerActivity.Companion.WALLET
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
            startActivity(
                    Intent(this, DoctorActionActivity::class.java)
                            .putExtra(PAGE_TO_OPEN, RequestType.SCHEDULE)
                            .putExtra(SERVICE_ID, service.service_id)
                            .putExtra(USER_DATA, doctorData)
            )
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
                    //binding.ivMark.visible()

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

                    /*If amount not sufficient then add money*/
                    if (it.data?.amountNotSufficient == true) {
                        AlertDialog.Builder(this)
                                .setCancelable(false)
                                .setTitle(getString(R.string.added_to_wallet))
                                .setMessage(getString(R.string.money_insufficient))
                                .setPositiveButton(getString(R.string.ok)) { dialog, which ->

                                }
                                .setNegativeButton(getString(R.string.add_money)) { dialog, which ->
                                    startActivity(Intent(this, DrawerActivity::class.java)
                                                    .putExtra(PAGE_TO_OPEN, WALLET))
                                }.show()

                    } else {
                        longToast(getString(R.string.request_sent))
                        setResult(Activity.RESULT_OK)
                        finish()
                    }

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
        loadImage(
                binding.ivPic, doctorData?.profile_image,
                R.drawable.image_placeholder
        )

        binding.tvDesc.text = doctorData?.categoryData?.name ?: getString(R.string.na)

        binding.tvRating.text = getString(
                R.string.s_s_reviews,
                getUserRating(doctorData?.totalRating),
                doctorData?.reviewCount
        )
        binding.tvPatientV.text = doctorData?.patientCount ?: getString(R.string.na)

        if (doctorData?.profile?.working_since == null) {
            binding.tvExperience.gone()
            binding.tvExperienceV.gone()
        } else
            binding.tvExperienceV.text =
                    "${getAge(doctorData?.profile?.working_since)} ${getString(R.string.years)}"

        binding.tvReviewsV.text = doctorData?.reviewCount ?: getString(R.string.na)
        binding.tvReviewCount.text = getUserRating(doctorData?.totalRating)

        /*val serviceList = ArrayList<Service>()
        serviceList.addAll(doctorData?.services ?: emptyList())
        val adapter = ServicesAdapter(this, serviceList)
        binding.rvServices.adapter = adapter*/
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
                shareDeepLink(DeepLink.USER_PROFILE, this, doctorData, userRepository)
            }
        }

        binding.tvBookAppointment.setOnClickListener {
            if (isConnectedToInternet(this, true)) {
                val hashMap = HashMap<String, Any>()

                hashMap["consultant_id"] = doctorId
                hashMap["service_id"] = CATEGORY_SERVICE_ID
                hashMap["schedule_type"] = RequestType.SCHEDULE
                hashMap["request_type"] = "multiple"

                if (intent.hasExtra(EXTRA_REQUEST_ID)) {
                    val bookService = intent.getSerializableExtra(EXTRA_REQUEST_ID) as BookService
                    hashMap["filter_id"] = bookService.filter_id ?: ""

                    hashMap["dates"] = bookService.date ?: ""
                    hashMap["start_time"] = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                            DateFormat.TIME_FORMAT_24, bookService.startTime ?: "")
                    hashMap["end_time"] = DateUtils.dateFormatChange(DateFormat.TIME_FORMAT,
                            DateFormat.TIME_FORMAT_24, bookService.endTime ?: "")

                    hashMap["lat"] = bookService.address?.location?.get(1).toString()
                    hashMap["long"] = bookService.address?.location?.get(0).toString()
                    hashMap["service_address"] = bookService.address?.locationName ?: ""

                    hashMap["first_name"] = bookService.personName
                    hashMap["last_name"] = bookService.personName
                    hashMap["service_for"] = bookService.service_for ?: ""
                    hashMap["home_care_req"] = bookService.home_care_req ?: ""
                    hashMap["reason_for_service"] = bookService.reason ?: ""
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
}
