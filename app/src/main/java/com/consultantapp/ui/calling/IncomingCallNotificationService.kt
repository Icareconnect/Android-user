package com.consultantapp.ui.calling

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantapp.R
import com.consultantapp.data.models.PushData
import com.consultantapp.data.network.PushType
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.ui.calling.Constants.CALL_NOTIFICATION_ID
import dagger.android.DaggerService
import javax.inject.Inject

class IncomingCallNotificationService : DaggerService() {

    @Inject
    lateinit var userRepository: UserRepository

    private val notificationId = CALL_NOTIFICATION_ID

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val action = intent.action
        if (action != null) {
            val callInvite = intent.getSerializableExtra(Constants.INCOMING_CALL_INVITE) as PushData
            when (action) {
                Constants.ACTION_INCOMING_CALL -> handleIncomingCall(
                        callInvite)
                Constants.ACTION_ACCEPT -> accept(
                        callInvite)
                Constants.ACTION_REJECT -> reject(callInvite)
                Constants.ACTION_CANCEL_CALL -> handleCancelledCall(
                        intent)
                Constants.CLEAR_NOTIFICATION -> endForeground()
                else -> {
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotification(callInvite: PushData, channelImportance: Int): Notification {
        val intent = Intent(this, CallingActivity::class.java)
        intent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite)
        intent.action = Constants.ACTION_INCOMING_CALL_NOTIFICATION

        intent.putExtra(
                Constants.INCOMING_CALL_NOTIFICATION_ID,
                notificationId
        )
        intent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
                this,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        /*
         * Pass the notification id and call sid to use as an identifier to cancel the
         * notification later
         */
        val extras = Bundle()
        extras.putString(Constants.CALL_SID_KEY, callInvite.request_id)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            buildNotification(
                    callInvite.sender_name + " is calling.",
                    pendingIntent,
                    extras,
                    callInvite,
                    createChannel(channelImportance)
            )
        } else {
            NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_call_end_white_24dp)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(callInvite.sender_name + " is calling.")
                    .setAutoCancel(true)
                    .setExtras(extras)
                    .setContentIntent(pendingIntent)
                    .setGroup("test_app_notification")
                    .setColor(Color.rgb(214, 10, 37)).build()
        }
    }

    /**
     * Build a notification.
     *
     * @param text          the text of the notification
     * @param pendingIntent the body, pending intent for the notification
     * @param extras        extras passed with the notification
     * @return the builder
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun buildNotification(text: String, pendingIntent: PendingIntent, extras: Bundle,
                                  callInvite: PushData,
                                  channelId: String): Notification {
        val rejectIntent =
                Intent(applicationContext, IncomingCallNotificationService::class.java)
        rejectIntent.action = Constants.ACTION_REJECT
        rejectIntent.putExtra(
                Constants.INCOMING_CALL_INVITE,
                callInvite
        )
        rejectIntent.putExtra(
                Constants.INCOMING_CALL_NOTIFICATION_ID,
                notificationId
        )
        val piRejectIntent = PendingIntent.getService(
                applicationContext,
                0,
                rejectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val acceptIntent =
                Intent(applicationContext, IncomingCallNotificationService::class.java)
        acceptIntent.action = Constants.ACTION_ACCEPT
        acceptIntent.putExtra(
                Constants.INCOMING_CALL_INVITE,
                callInvite
        )
        acceptIntent.putExtra(
                Constants.INCOMING_CALL_NOTIFICATION_ID,
                notificationId
        )
        val piAcceptIntent = PendingIntent.getService(
                applicationContext,
                0,
                acceptIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        //Define sound URI
        val soundUri =
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder =
                Notification.Builder(applicationContext, channelId)
                        .setSmallIcon(R.drawable.ic_call_end_white_24dp)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(text)
                        .setCategory(Notification.CATEGORY_CALL)
                        .setFullScreenIntent(pendingIntent, true)
                        .setExtras(extras)
                        .setAutoCancel(true)
                        .setSound(soundUri)
                        .addAction(
                                android.R.drawable.ic_menu_delete,
                                getString(R.string.decline),
                                piRejectIntent
                        )
                        .addAction(
                                android.R.drawable.ic_menu_call,
                                getString(R.string.answer),
                                piAcceptIntent
                        )
                        .setFullScreenIntent(pendingIntent, true)
        return builder.build()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createChannel(channelImportance: Int): String {
        var callInviteChannel = NotificationChannel(
                Constants.VOICE_CHANNEL_HIGH_IMPORTANCE,
                "Primary Voice Channel", NotificationManager.IMPORTANCE_HIGH
        )
        var channelId =
                Constants.VOICE_CHANNEL_HIGH_IMPORTANCE
        if (channelImportance == NotificationManager.IMPORTANCE_LOW) {
            callInviteChannel = NotificationChannel(
                    Constants.VOICE_CHANNEL_LOW_IMPORTANCE,
                    "Primary Voice Channel", NotificationManager.IMPORTANCE_LOW
            )
            channelId = Constants.VOICE_CHANNEL_LOW_IMPORTANCE
        }
        callInviteChannel.lightColor = Color.GREEN
        callInviteChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(callInviteChannel)
        return channelId
    }

    private fun accept(callInvite: PushData) {
        endForeground()
        val activeCallIntent = Intent(this, CallingActivity::class.java)
        activeCallIntent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite)
        activeCallIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        activeCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activeCallIntent.putExtra(
                Constants.INCOMING_CALL_INVITE,
                callInvite
        )
        activeCallIntent.putExtra(
                Constants.INCOMING_CALL_NOTIFICATION_ID,
                notificationId
        )
        activeCallIntent.action = Constants.ACTION_ACCEPT
        startActivity(activeCallIntent)
    }

    private fun reject(callInvite: PushData) {
        endForeground()
        SoundPoolManager.getInstance(this).stopRinging()

       /*Handle if cancelled from push*/
        userRepository.callStatus(callInvite.request_id,callInvite.call_id, PushType.CALL_CANCELED)

        val intent = Intent(this, IncomingCallNotificationService::class.java)
        intent.action = Constants.ACTION_CANCEL_CALL

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        //callInvite.reject(getApplicationContext());
    }

    private fun handleCancelledCall(intent: Intent) {
        endForeground()
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun handleIncomingCall(callInvite: PushData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setCallInProgressNotification(callInvite)
        }
        sendCallInviteToActivity(callInvite)
    }

    private fun endForeground() {
        stopForeground(true)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun setCallInProgressNotification(callInvite: PushData) {
        if (isAppVisible) {
            Log.i(TAG,
                    "setCallInProgressNotification - app is visible.")
            startForeground(notificationId,
                    createNotification(callInvite, NotificationManager.IMPORTANCE_LOW))
        } else {
            Log.i(TAG,
                    "setCallInProgressNotification - app is NOT visible.")
            startForeground(notificationId,
                    createNotification(callInvite, NotificationManager.IMPORTANCE_HIGH))
        }
    }

    /*
     * Send the CallInvite to the VoiceActivity. Start the activity if it is not running already.
     */
    private fun sendCallInviteToActivity(callInvite: PushData) {
        if (Build.VERSION.SDK_INT >= 29 && !isAppVisible) {
            return
        }
        val intent = Intent(this, CallingActivity::class.java)
        intent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite)
        intent.action = Constants.ACTION_INCOMING_CALL
        intent.putExtra(Constants.INCOMING_CALL_NOTIFICATION_ID,
                notificationId)
        intent.putExtra(Constants.INCOMING_CALL_INVITE, callInvite)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        this.startActivity(intent)
    }

    private val isAppVisible: Boolean
        private get() = ProcessLifecycleOwner
                .get()
                .lifecycle
                .currentState
                .isAtLeast(Lifecycle.State.STARTED)

    companion object {
        private val TAG = IncomingCallNotificationService::class.java.simpleName
    }
}