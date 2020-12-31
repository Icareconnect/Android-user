package com.consultantapp.ui.jitsimeet


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantapp.R
import com.consultantapp.appClientDetails
import com.consultantapp.data.models.responses.JitsiClass
import com.consultantapp.data.network.Config
import com.consultantapp.data.network.PushType
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.pushNotifications.MessagingService
import com.consultantapp.ui.calling.Constants
import com.consultantapp.ui.calling.SoundPoolManager
import com.consultantapp.utils.*
import com.facebook.react.modules.core.PermissionListener
import dagger.android.support.DaggerAppCompatActivity
import org.jitsi.meet.sdk.*
import org.jitsi.meet.sdk.log.JitsiMeetLogger
import java.net.MalformedURLException
import java.net.URL
import javax.inject.Inject


class JitsiActivity : DaggerAppCompatActivity(), JitsiMeetActivityInterface, JitsiMeetViewListener {

    @Inject
    lateinit var userRepository: UserRepository

    private var isReceiverRegistered = false

    private var jitsiMeetView: JitsiMeetView? = null

    private var jitsiClass: JitsiClass? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_jitsi)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)

        checkPermission()

        MessagingService.mHandler.removeCallbacksAndMessages(null)
        jitsiMeetView = JitsiMeetView(this)


        jitsiClass = intent.getSerializableExtra(EXTRA_CALL_NAME) as JitsiClass
        val roomName: String
        val subjectName: String

        if (jitsiClass?.isClass == false) {
            roomName = "Call_${appClientDetails.jitsi_id}_${jitsiClass?.id}"
            subjectName = "Call"
        } else {
            roomName = "Class_${appClientDetails.jitsi_id}_${jitsiClass?.id}"
            subjectName = jitsiClass?.name ?:""
        }
        Log.d("Call Name", roomName)


        // Initialize default options for Jitsi Meet conferences.
        val serverURL: URL
        serverURL = try {
            URL("https://meet.royoapps.com/")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            throw RuntimeException("Invalid server URL!")
        }
        val defaultOptions = JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverURL)
                .setWelcomePageEnabled(false)
                .setFeatureFlag("invite.enabled", false)
                .setFeatureFlag("chat.enabled", false)
                .setFeatureFlag("calendar.enabled", false)
                .setFeatureFlag("live-streaming.enabled", false)
                .setFeatureFlag("recording.enabled", false)
                .setFeatureFlag("tile-view.enabled", false)
                .setFeatureFlag("meeting-password.enabled", false)
                .setFeatureFlag("pip.enabled", true)
                .setFeatureFlag("close-captions.enabled", false)
                .build()
        JitsiMeet.setDefaultConferenceOptions(defaultOptions)


        if (roomName.isNotEmpty()) {
            // Build options object for joining the conference. The SDK will merge the default
            // one we set earlier and this one when joining.
            val userInfo = JitsiMeetUserInfo()
            val userData = userRepository.getUser()
            userInfo.displayName = userData?.name
            userInfo.avatar = URL("${Config.imageURL}uploads/${userData?.profile}")

            val setAudioOnly = jitsiClass?.callType?.toLowerCase() == CallFrom.CALL

            val options = JitsiMeetConferenceOptions.Builder()
                    .setUserInfo(userInfo)
                    .setRoom(roomName)
                    .setSubject(subjectName)
                    .setAudioOnly(setAudioOnly)
                    .build()
            // Launch the new activity with the given options. The launch() method takes care
            // of creating the required Intent and passing the options.

            /*   JitsiMeetActivity.launch(this, options)
               finish()*/

            jitsiMeetView?.join(options)

            setContentView(jitsiMeetView)
            jitsiMeetView?.listener = this

            SoundPoolManager.getInstance(this).stopRinging()

        }

        //finish()
    }

    override fun onBackPressed() {
    }


    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {
        JitsiMeetActivityDelegate.requestPermissions(this, p0, p1, p2)
    }

    override fun onConferenceJoined(data: Map<String?, Any?>) {
        JitsiMeetLogger.i("Conference joined: $data")
        // Launch the service for the ongoing notification.
        // JitsiMeetOngoingConferenceService.launch(this);
    }

    override fun onConferenceTerminated(data: Map<String?, Any?>) {
        JitsiMeetLogger.i("Conference terminated: $data")

        if (isConnectedToInternet(this, true) && jitsiClass?.isClass == false) {
            userRepository.callStatus(jitsiClass?.id ?: "",jitsiClass?.call_id ?: "",
                    PushType.CALL_CANCELED)

            longToast(getString(R.string.disconnecting))
        }

        jitsiMeetView?.listener = null
        jitsiMeetView?.leave()
        finish()
    }

    override fun onConferenceWillJoin(data: Map<String?, Any?>) {
        JitsiMeetLogger.i("Conference will join: $data")
    }

    private fun checkPermission() {
        val PERMISSIONS = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        )
        if (!hasPermissions(*PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 100)
        }
    }

    fun hasPermissions(vararg permissions: String?): Boolean {
        if (permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                                this,
                                permission!!
                        ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
        JitsiMeetActivityDelegate.onHostResume(this)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    override fun onStop() {
        super.onStop()
        JitsiMeetActivityDelegate.onHostPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        JitsiMeetActivityDelegate.onHostDestroy(this)
        jitsiMeetView?.leave()
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
            if (intent.getStringExtra(EXTRA_REQUEST_ID) == jitsiClass?.call_id) {
                if (intent.action == Constants.ACTION_CANCEL_CALL || intent.action == PushType.REQUEST_COMPLETED) {
                    jitsiMeetView?.listener = null
                    jitsiMeetView?.leave()
                    finish()
                }
            }
        }
    }

}