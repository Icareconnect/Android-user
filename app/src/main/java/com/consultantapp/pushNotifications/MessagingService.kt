package com.consultantapp.pushNotifications

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantapp.R
import com.consultantapp.data.models.PushData
import com.consultantapp.data.network.PushType
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.ui.calling.Constants
import com.consultantapp.ui.calling.IncomingCallNotificationService
import com.consultantapp.ui.calling.SoundPoolManager
import com.consultantapp.ui.dashboard.MainActivity
import com.consultantapp.ui.dashboard.appointment.appointmentStatus.AppointmentStatusActivity
import com.consultantapp.ui.dashboard.chat.chatdetail.ChatDetailActivity
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.utils.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import org.json.JSONObject
import java.util.*
import javax.inject.Inject


class MessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var appSocket: AppSocket

    private val channelId = "Consultant user"


    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("fcmToken", token)

        if (userRepository.isUserLoggedIn()) {
            userRepository.pushTokenUpdate()
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e("remoteMessage", remoteMessage.data.toString())

        val notificationData = JSONObject(remoteMessage.data as MutableMap<Any?, Any?>)

        if (userRepository.isUserLoggedIn()) {
            sendNotification(notificationData)
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    private fun sendNotification(notificationData: JSONObject) {

        userRepository.isNewNotification.postValue(true)

        val pushData = PushData(
                msg = notificationData.optString("msg"),
                title = notificationData.optString("title"),
                sound = notificationData.optString("sound"),
                pushType = notificationData.optString("pushType"),
                imageUrl = notificationData.optString("imageUrl"),
                message = notificationData.optString("message"),
                senderId = notificationData.optString("senderId"),
                senderName = notificationData.optString("senderName"),
                receiverId = notificationData.optString("receiverId"),
                messageType = notificationData.optString("messageType"),
                request_id = notificationData.optString("request_id"),
                call_id = notificationData.optString("call_id"),
                service_type = notificationData.optString("service_type"),
                sentAt = notificationData.optLong("sentAt"),
                request_time = notificationData.optString("request_time"),
                sender_name = notificationData.optString("sender_name"),
                sender_image = notificationData.optString("sender_image"),
                vendor_category_name = notificationData.optString("vendor_category_name"),
                transaction_id = notificationData.optString("transaction_id")
        )


        val requestID = Calendar.getInstance().timeInMillis.toInt()

        /*Stack builder home activity*/
        val stackBuilder = TaskStackBuilder.create(this)

        stackBuilder.addParentStack(MainActivity::class.java)
        val homeIntent = Intent(this, MainActivity::class.java)
        //stackBuilder.addNextIntent(homeIntent)

        Log.e("Notification", "Parent added")
        /*Final activity to open*/
        var intent: Intent? = null

        val titleString = pushData.pushType.replace("_", " ").toLowerCase()
        var title = ""

        val lineScan = Scanner(titleString)
        while (lineScan.hasNext()) {
            val word: String = lineScan.next()
            title += Character.toUpperCase(word[0]).toString() + word.substring(1) + " "
        }

        val msg = pushData.message
        when (pushData.pushType) {
            PushType.CHAT -> {
                homeIntent.putExtra(EXTRA_TAB, "2")
                title = pushData.senderName
                intent = Intent(this, ChatDetailActivity::class.java)
                        .putExtra(USER_ID, pushData.senderId)
                        .putExtra(USER_NAME, pushData.senderName)
                        .putExtra(EXTRA_REQUEST_ID, pushData.request_id)
                //stackBuilder.addNextIntent(intent)
            }
            PushType.CHAT_STARTED -> {
                intent = Intent(this, ChatDetailActivity::class.java)
                        .putExtra(USER_ID, pushData.senderId)
                        .putExtra(USER_NAME, pushData.senderName)
                        .putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                val intentBroadcast = Intent()
                intentBroadcast.action = pushData.pushType
                intentBroadcast.putExtra(USER_ID, pushData.senderId)
                        .putExtra(USER_NAME, pushData.senderName)
                        .putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)
            }
            PushType.PROFILE_APPROVED -> {

                val intentBroadcast = Intent()
                intentBroadcast.action = pushData.pushType
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)
            }
            PushType.START, PushType.REACHED -> {

                intent = Intent(this, AppointmentStatusActivity::class.java)
                        .putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                val intentBroadcast = Intent()
                intentBroadcast.action = pushData.pushType
                intentBroadcast.putExtra(EXTRA_REQUEST_ID, pushData.request_id)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)
            }
            PushType.START_SERVICE -> {

                intent = Intent(this, DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.UPDATE_SERVICE)
                        .putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                val intentBroadcast = Intent()
                intentBroadcast.action = pushData.pushType
                intentBroadcast.putExtra(EXTRA_REQUEST_ID, pushData.request_id)
                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)
            }

            PushType.BOOKING_RESERVED, PushType.REQUEST_ACCEPTED, PushType.REQUEST_COMPLETED,
            PushType.CANCELED_REQUEST, PushType.CANCEL_SERVICE, PushType.RESCHEDULED_REQUEST -> {
                homeIntent.putExtra(EXTRA_TAB, "1")

                intent = Intent(this, DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.APPOINTMENT_DETAILS)
                        .putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                val intentBroadcast = Intent()
                intentBroadcast.action = pushData.pushType
                intentBroadcast.putExtra(EXTRA_REQUEST_ID, pushData.request_id)
                intentBroadcast.putExtra(EXTRA_TRANSACTION_ID, pushData.transaction_id)

                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)
            }
            PushType.COMPLETED -> {
                intent = Intent(this, DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.REQUEST_COMPLETE)
                        .putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                val intentBroadcast = Intent()
                intentBroadcast.action = pushData.pushType
                intentBroadcast.putExtra(EXTRA_REQUEST_ID, pushData.request_id)

                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)
            }
            PushType.BALANCE_ADDED, PushType.BALANCE_FAILED -> {
               /* intent = Intent(this, DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, WALLET)*/

                val intentBroadcast = Intent()
                intentBroadcast.action = pushData.pushType
                intentBroadcast.putExtra(EXTRA_REQUEST_ID, pushData.transaction_id)

                LocalBroadcastManager.getInstance(this).sendBroadcast(intentBroadcast)

            }
            PushType.CALL -> {

                handleInvite(pushData)
                SoundPoolManager.getInstance(this).release()
                SoundPoolManager.getInstance(this).playRinging()

                wakeDevice()
                userRepository.callStatus(pushData.request_id, pushData.call_id, PushType.CALL_RINGING)

                mHandler.removeCallbacksAndMessages(null)
                mHandler.postDelayed({
                    handleCanceledCallInvite(pushData)

                    userRepository.callStatus(pushData.request_id, pushData.call_id, PushType.CALL_CANCELED)

                    mHandler.removeCallbacksAndMessages(null)
                    Log.e("remoteMessageCall==", "4")
                }, 40000)

                return

            }
            PushType.CALL_CANCELED -> {
                handleCanceledCallInvite(pushData)
                return
            }
        }

        stackBuilder.addNextIntent(homeIntent)
        if (intent != null)
            stackBuilder.addNextIntent(intent)


        /*val pendingIntent = PendingIntent.getActivity(this, requestID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)*/

        /*Flags*/
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        homeIntent.action = System.currentTimeMillis().toString()

        val pendingIntent =
                stackBuilder.getPendingIntent(requestID, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setContentTitle(title) //Header
                .setContentText(msg) //Content
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
            notificationBuilder.color = ContextCompat.getColor(this, R.color.colorAccent)
        } else {
            notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
        }

        val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                    channelId, getText(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH
            )

            notificationManager.createNotificationChannel(mChannel)
        }

        if (pushData.pushType == PushType.CHAT && pushData.senderId == ChatDetailActivity.otherUserID &&
                pushData.request_id == ChatDetailActivity.requestId
        ) {
            /*Don't generate push*/
            Log.e("", "")
        } else {
            notificationManager.notify(requestID, notificationBuilder.build())
        }
    }

    companion object {
        val mHandler = Handler()
    }

    private fun wakeDevice() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "Consultant:"
        )
        wl.acquire(25000)
    }

    private fun handleInvite(pushData: PushData) {
        val intent = Intent(this, IncomingCallNotificationService::class.java)
        intent.action = Constants.ACTION_INCOMING_CALL
        intent.putExtra(Constants.INCOMING_CALL_INVITE, pushData)
        intent.putExtra(EXTRA_REQUEST_ID, pushData.call_id)

        startService(intent)
    }

    private fun handleCanceledCallInvite(pushData: PushData) {
        SoundPoolManager.getInstance(this).stopRinging()
        val intent = Intent(this, IncomingCallNotificationService::class.java)
        intent.action = Constants.ACTION_CANCEL_CALL
        intent.putExtra(Constants.INCOMING_CALL_INVITE, pushData)
        intent.putExtra(EXTRA_REQUEST_ID, pushData.call_id)

        startService(intent)
    }
}