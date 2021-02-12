package com.consultantapp.ui.dashboard.appointment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantapp.R
import com.consultantapp.data.models.responses.Request
import com.consultantapp.data.network.ApiKeys.AFTER
import com.consultantapp.data.network.ApiKeys.PER_PAGE
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PER_PAGE_LOAD
import com.consultantapp.data.network.PushType
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityListingToolbarBinding
import com.consultantapp.ui.dashboard.appointment.appointmentStatus.AppointmentStatusActivity
import com.consultantapp.ui.dashboard.doctor.DoctorActionActivity
import com.consultantapp.ui.dashboard.doctor.schedule.ScheduleFragment.Companion.SERVICE_ID
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.item_no_data.view.*
import javax.inject.Inject

class AppointmentFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityListingToolbarBinding

    private var rootView: View? = null

    private lateinit var viewModel: AppointmentViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<Request>()

    private lateinit var adapter: AppointmentAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var isReceiverRegistered = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(
                inflater,
                R.layout.activity_listing_toolbar,
                container,
                false
            )
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()
            hitApi(true)
        }
        return rootView
    }


    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        binding.toolbar.title = getString(R.string.my_bookings)
        if (!requireActivity().intent.hasExtra(PAGE_TO_OPEN))
            binding.toolbar.navigationIcon = null
        binding.tvTitle.gone()

        binding.clNoData.ivNoData.setImageResource(R.drawable.ic_requests_empty_state)
        binding.clNoData.tvNoData.text = getString(R.string.no_requests)
        binding.clNoData.tvNoDataDesc.text = getString(R.string.no_requests_desc)

    }

    private fun setAdapter() {
        adapter = AppointmentAdapter(this, items)
        binding.rvListing.adapter = adapter
        binding.rvListing.itemAnimator = null
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
            hitApi(true)
        }

        binding.rvListing.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvListing.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMoreItems && !isLastPage && lastVisibleItemPosition >= totalItemCount) {
                    isLoadingMoreItems = true
                    hitApi(false)
                }
            }
        })
    }


    fun hitApiRefresh() {
        hitApi(true)
    }

    private fun hitApi(firstHit: Boolean) {
        if (isConnectedToInternet(requireContext(), true)) {
            if (firstHit) {
                isFirstPage = true
                isLastPage = false
            }

            val hashMap = HashMap<String, String>()
            if (!isFirstPage && items.isNotEmpty())
                hashMap[AFTER] = items[items.size - 1].id ?: ""

            hashMap[PER_PAGE] = PER_PAGE_LOAD.toString()

            hashMap["service_type"] = CallType.ALL
            viewModel.request(hashMap)
        } else
            binding.swipeRefresh.isRefreshing = false
    }

    private fun bindObservers() {
        viewModel.request.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    isLoadingMoreItems = false

                    val tempList = it.data?.requests ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                        items.addAll(tempList)

                        adapter.notifyDataSetChanged()
                    } else {
                        val oldSize = items.size
                        items.addAll(tempList)

                        adapter.notifyItemRangeInserted(oldSize, items.size)
                    }
                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.clNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)

                    binding.swipeRefresh.isRefreshing = false
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!isLoadingMoreItems && !binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                }
            }
        })

        viewModel.cancelRequest.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    hitApi(true)

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


    fun checkStatus(item: Request) {
//        item.status = CallAction.REACHED
        when (item.status) {
            CallAction.START, CallAction.REACHED ->
                startActivityForResult(
                    Intent(requireActivity(), AppointmentStatusActivity::class.java)
                        .putExtra(EXTRA_REQUEST_ID, item.id), AppRequestCode.APPOINTMENT_DETAILS
                )
            CallAction.START_SERVICE ->
                startActivityForResult(
                    Intent(requireContext(), DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.UPDATE_SERVICE)
                        .putExtra(EXTRA_REQUEST_ID, item.id), AppRequestCode.APPOINTMENT_DETAILS
                )
        }
    }

    fun rescheduleAppointment(item: Request) {
        val intent = Intent(requireContext(), DoctorActionActivity::class.java)
            .putExtra(PAGE_TO_OPEN, item.schedule_type)
            .putExtra(SERVICE_ID, item.service_id)
            .putExtra(USER_DATA, item.to_user)

        if (item.status != CallAction.COMPLETED) {
            intent.putExtra(EXTRA_REQUEST_ID, item.id)
        }
        startActivityForResult(intent, AppRequestCode.NEW_APPOINTMENT)
    }

    fun cancelAppointment(item: Request) {
        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(),
            R.string.cancel_appointment,
            R.string.cancel_appointment_msg,
            R.string.cancel_appointment,
            R.string.cancel,
            false,
            object : AlertDialogUtil.OnOkCancelDialogListener {
                override fun onOkButtonClicked() {
                    if (isConnectedToInternet(requireContext(), true)) {
                        val hashMap = HashMap<String, String>()
                        hashMap["request_id"] = item.id ?: ""
                        viewModel.cancelRequest(hashMap)
                    }
                }

                override fun onCancelButtonClicked() {
                }
            }).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                AppRequestCode.NEW_APPOINTMENT, AppRequestCode.APPOINTMENT_DETAILS -> {
                    hitApi(true)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.NEW_REQUEST)
            intentFilter.addAction(PushType.REQUEST_COMPLETED)
            intentFilter.addAction(PushType.COMPLETED)
            intentFilter.addAction(PushType.REQUEST_ACCEPTED)
            intentFilter.addAction(PushType.CANCELED_REQUEST)
            intentFilter.addAction(PushType.REQUEST_FAILED)
            intentFilter.addAction(PushType.CHAT_STARTED)
            intentFilter.addAction(PushType.START)
            intentFilter.addAction(PushType.START_SERVICE)
            intentFilter.addAction(PushType.CANCEL_SERVICE)
            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(refreshRequests, intentFilter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(refreshRequests)
            isReceiverRegistered = false
        }
    }

    private val refreshRequests = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                PushType.REQUEST_COMPLETED, PushType.COMPLETED, PushType.REQUEST_ACCEPTED,
                PushType.CANCELED_REQUEST, PushType.REQUEST_FAILED, PushType.CHAT_STARTED, PushType.START,
                PushType.REACHED, PushType.START_SERVICE, PushType.CANCEL_SERVICE, PushType.NEW_REQUEST -> {
                    hitApi(true)
                }
            }
        }
    }
}