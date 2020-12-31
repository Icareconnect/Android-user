package com.consultantapp.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.TaskStackBuilder
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.appClientDetails
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.ui.dashboard.MainActivity
import com.consultantapp.ui.dashboard.doctor.detail.DoctorDetailActivity
import com.consultantapp.ui.loginSignUp.SignUpActivity
import com.consultantapp.utils.*
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.android.support.DaggerAppCompatActivity
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap


class SplashActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var viewModel: AppVersionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initialise()
        bindObservers()
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[AppVersionViewModel::class.java]

        if (isConnectedToInternet(this, true)) {
            val hashMap = HashMap<String, String>()
            hashMap["app_type"] = "1"/*APP_TYPE 1: User App, 2: Doctor App*/
            hashMap["device_type"] = "2"/*ANDROID*/
            viewModel.clientDetails(hashMap)
        }
    }


    private fun bindObservers() {
        viewModel.clientDetails.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {

                    val appDetails = it.data

                    /*Handle feature keys*/
                    appDetails?.client_features?.forEach {
                        when (it.name?.toLowerCase(Locale.getDefault())) {
                            ClientFeatures.ADDRESS.toLowerCase(Locale.getDefault()) ->
                                appDetails.clientFeaturesKeys.isAddress = true
                        }
                    }

                    prefsManager.save(APP_DETAILS, appDetails)
                    appClientDetails = userRepository.getAppSetting()

                    /*Check App Version*/
                    val hashMap = HashMap<String, String>()
                    hashMap["app_type"] = "1"/*APP_TYPE 1: User App, 2: Doctor App*/
                    hashMap["device_type"] = "2"/*ANDROID*/
                    hashMap["current_version"] = getVersion(this).versionCode.toString()

                    viewModel.checkAppVersion(hashMap)

                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                }
            }
        })

        viewModel.checkAppVersion.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    when (it.data?.update_type) {
                        AppUpdateType.HARD_UPDATE -> hardUpdate()
                        AppUpdateType.SOFT_UPDATE -> softUpdate()
                        else -> checkDeepLink()
                    }
                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                }
            }
        })
    }

    private fun hardUpdate() {
        AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.update))
                .setMessage(getString(R.string.update_desc))
                .setPositiveButton(getString(R.string.update)) { dialog, which ->
                    updatePlayStore()
                    hardUpdate()
                }.show()


    }

    private fun updatePlayStore() {
        val appPackageName = packageName // getPackageName() from Context or Activity object
        try {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PLAY_STORE + appPackageName)))
        }
    }

    private fun softUpdate() {
        AlertDialogUtil.instance.createOkCancelDialog(this, R.string.update,
                R.string.update_desc, R.string.update, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        updatePlayStore()
                        softUpdate()
                    }

                    override fun onCancelButtonClicked() {
                        checkDeepLink()
                    }
                }).show()
    }

    private fun goNormalSteps() {
        if (userRepository.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        finish()
    }

    object AppUpdateType {
        const val HARD_UPDATE = 1
        const val SOFT_UPDATE = 2
    }

    private fun checkDeepLink() {
        /*Check if Deep Link*/
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink((intent ?: Intent()))
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    val deepLink: Uri?
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link
                        openHomeActivity(deepLink)
                    } else {
                        openHomeActivity(null)
                    }
                }
                .addOnFailureListener(this) { _ ->
                    openHomeActivity(null)
                }
    }

    private fun openHomeActivity(deepLink: Uri?) {
        if (userRepository.isUserLoggedIn()) {
            val stackBuilder = TaskStackBuilder.create(this)

            stackBuilder.addParentStack(MainActivity::class.java)
            val homeIntent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            stackBuilder.addNextIntent(homeIntent)

            if (deepLink?.getQueryParameter("id") != null) {
                var intent = Intent(this, DoctorDetailActivity::class.java)
                if (deepLink.toString().contains(DeepLink.USER_PROFILE)) {
                    intent = Intent(this, DoctorDetailActivity::class.java)
                    intent.putExtra(DoctorDetailActivity.DOCTOR_ID, deepLink.getQueryParameter("id"))
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                stackBuilder.addNextIntent(intent)
            }

            stackBuilder.startActivities()
        } else {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        finish()
    }
}
