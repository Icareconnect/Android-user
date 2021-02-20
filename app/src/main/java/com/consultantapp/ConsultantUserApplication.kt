package com.consultantapp

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantapp.data.models.responses.appdetails.AppVersion
import com.consultantapp.data.network.PushType
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.di.DaggerAppComponent
import com.consultantapp.ui.dashboard.chat.chatdetail.ChatDetailActivity
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.utils.*
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.libraries.places.api.Places
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import javax.inject.Inject


var appClientDetails = AppVersion()

class ConsultantUserApplication : DaggerApplication(), LifecycleObserver {

    @Inject
    lateinit var userRepository: UserRepository

    private var isReceiverRegistered = false


    override fun onCreate() {
        super.onCreate()

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Fresco.initialize(this)
        // Initialize Places.
        Places.initialize(applicationContext, getString(R.string.google_places_api_key))


        appClientDetails = userRepository.getAppSetting()
        setsApplication(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> =
            DaggerAppComponent.builder().create(this)

    companion object {

        private var isApplication: Application? = null

        fun setsApplication(sApplication: Application) {
            isApplication = sApplication
        }

        fun getUser() {

        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun appInResumeState() {
        registerReceiver()
        //Toast.makeText(this, "In Foreground", Toast.LENGTH_LONG).show();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun appInPauseState() {
        //Toast.makeText(this, "In Background", Toast.LENGTH_LONG).show();
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.CHAT_STARTED)
            intentFilter.addAction(PushType.COMPLETED)
            LocalBroadcastManager.getInstance(this).registerReceiver(refreshRequests, intentFilter)
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshRequests)
            isReceiverRegistered = false
        }
    }

    private val refreshRequests = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ChatDetailActivity.otherUserID == "-1" && intent.action == PushType.CHAT_STARTED) {
                val intentActivity = Intent(this@ConsultantUserApplication, ChatDetailActivity::class.java)
                        .putExtra(USER_ID, intent.getStringExtra(USER_ID))
                        .putExtra(USER_NAME, intent.getStringExtra(USER_NAME))
                        .putExtra(EXTRA_REQUEST_ID, intent.getStringExtra(EXTRA_REQUEST_ID))
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intentActivity)
            } else if (intent.action == PushType.COMPLETED) {
                val intentActivity = Intent(this@ConsultantUserApplication, DrawerActivity::class.java)
                        .putExtra(PAGE_TO_OPEN, DrawerActivity.REQUEST_COMPLETE)
                        .putExtra(EXTRA_REQUEST_ID, intent.getStringExtra(EXTRA_REQUEST_ID))
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                startActivity(intentActivity)
            }
        }
    }
}