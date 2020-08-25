package com.consultantapp.ui

import android.content.Context
import android.content.Intent
import com.consultantapp.utils.APP_UNIQUE_ID
import com.consultantapp.utils.PrefsManager
import dagger.android.DaggerBroadcastReceiver
import javax.inject.Inject

class InstallReferrerReceiver : DaggerBroadcastReceiver() {
    @Inject
    lateinit var prefsManager: PrefsManager

    override fun onReceive(context: Context?, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.hasExtra("app_id")) {
            val app_id: String = intent.getStringExtra("app_id") ?:""

            prefsManager.save(APP_UNIQUE_ID, app_id)
        }
    }
}