package com.consultantapp.ui.calling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantapp.R
import com.consultantapp.data.models.PushData
import com.consultantapp.data.models.responses.JitsiClass
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.PushType
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityCallingBinding
import com.consultantapp.pushNotifications.MessagingService
import com.consultantapp.ui.jitsimeet.JitsiActivity
import com.consultantapp.utils.*
import com.consultantapp.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class CallingActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    lateinit var binding: ActivityCallingBinding

    private lateinit var callInvite: PushData

    private lateinit var progressDialog: ProgressDialog

    private lateinit var viewModel: CallViewModel

    private var callStatus = ""

    private var audioManager: AudioManager? = null

    private var isReceiverRegistered = false

    private var callId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialise()
        listeners()
        bindObservers()

        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
    }

    private fun initialise() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calling)

        // These flags ensure that the activity can be launched when the screen is locked.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        viewModel = ViewModelProvider(this, viewModelFactory)[CallViewModel::class.java]
        progressDialog = ProgressDialog(this)


        /*
       * Needed for setting/abandoning audio focus during a call
       */
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager?.isSpeakerphoneOn = true

        callInvite = intent.getSerializableExtra(Constants.INCOMING_CALL_INVITE) as PushData
        callId = callInvite.call_id

        binding.tvName.text = callInvite.sender_name
        binding.tvDesc.text = callInvite.vendor_category_name
        loadImage(binding.ivPic, callInvite.sender_image, R.drawable.ic_profile_placeholder)

        binding.tvTime.text = "${DateUtils.dateTimeFormatFromUTC(
                DateFormat.DATE_TIME_FORMAT,
                callInvite?.request_time
        )}"

        binding.tvCallType.text = callInvite.service_type
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.action == Constants.ACTION_CANCEL_CALL) {
            if (intent.hasExtra(EXTRA_REQUEST_ID) && intent.getStringExtra(EXTRA_REQUEST_ID) == callId) {
                clearNotification()
                finish()
            }
        }
    }

    private fun listeners() {
        binding.ivPickCall.setOnClickListener {
            hitApiCall(PushType.CALL_ACCEPTED)
        }

        binding.ivRejectCall.setOnClickListener {
            hitApiCall(PushType.CALL_CANCELED)
        }
    }

    private fun hitApiCall(status: String) {
        MessagingService.mHandler.removeCallbacksAndMessages(null)

        callStatus = status
        if (isConnectedToInternet(this, true)) {
            val hashMap = HashMap<String, String>()
            hashMap["request_id"] = callInvite.request_id
            hashMap["call_id"] = callInvite.call_id
            hashMap["status"] = callStatus
            viewModel.callStatus(hashMap)

            if (status == PushType.CALL_ACCEPTED)
                longToast(getString(R.string.connecting))
            else if (status == PushType.CALL_CANCELED)
                longToast(getString(R.string.disconnecting))

            disableButton(binding.ivPickCall)
            disableButton(binding.ivRejectCall)
        }
    }

    override fun onBackPressed() {
    }

    private fun bindObservers() {
        viewModel.callStatus.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (callStatus == PushType.CALL_ACCEPTED) {

                        /*Data for jitsi class*/
                        val jitsiClass = JitsiClass()
                        jitsiClass.id = callInvite.request_id
                        jitsiClass.call_id = callInvite.call_id
                        jitsiClass.callType = callInvite.service_type
                        jitsiClass.name = ""

                        val intent = Intent(this, JitsiActivity::class.java)
                        intent.putExtra(EXTRA_CALL_NAME, jitsiClass)
                        startActivity(intent)
                    }
                    clearNotification()
                    finish()

                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    clearNotification()
                    finish()
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })
    }

    private fun clearNotification() {
        SoundPoolManager.getInstance(this).stopRinging()
        val intent = Intent(this, IncomingCallNotificationService::class.java)
        intent.action = Constants.CLEAR_NOTIFICATION
        intent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite)

        startService(intent)
    }

    override fun onDestroy() {
        SoundPoolManager.getInstance(this).release()
        super.onDestroy()
    }


    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Constants.ACTION_INCOMING_CALL)
            intentFilter.addAction(Constants.ACTION_CANCEL_CALL)
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    callCancelledReceiver, intentFilter
            )
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(callCancelledReceiver)
            isReceiverRegistered = false
        }
    }

    private val callCancelledReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Constants.ACTION_CANCEL_CALL) {
                if (intent.hasExtra(EXTRA_REQUEST_ID) && intent.getStringExtra(EXTRA_REQUEST_ID) == callId) {
                    clearNotification()
                    finish()
                }
            }
        }
    }

}
