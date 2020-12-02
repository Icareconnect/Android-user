package com.consultantapp.data.network

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.consultantapp.R
import com.consultantapp.data.network.responseUtil.AppError
import com.consultantapp.utils.PrefsManager
import com.consultantapp.utils.logoutUser

object ApisRespHandler {

    private var alertDialog: AlertDialog.Builder? = null

    fun handleError(error: AppError?, activity: Activity, prefsManager: PrefsManager) {
        error ?: return


        when (error) {
            is AppError.ApiError -> {
                if (alertDialog == null)
                    errorMessage(activity, error.message)
            }

            is AppError.ApiUnauthorized -> {
                if (alertDialog == null)
                    sessionExpired(activity, error.message, prefsManager)
            }

            is AppError.ApiAccountBlock -> {
                if (alertDialog == null)
                    accountDeleted(activity, error.message, prefsManager)
            }

            is AppError.ApiAccountRuleChanged -> {
                if (alertDialog == null)
                    accountDeleted(activity, error.message, prefsManager)
            }

            is AppError.ApiFailure -> {
                if (alertDialog == null) {
                    if (error.message.contains("Failed to connect to") ||
                        error.message.contains("Unable to resolve host") ||
                        error.message.contains("No address associated with hostname"))
                        errorMessage(activity, activity.getString(R.string.check_internet))
                    else
                        errorMessage(activity, error.message)
                }
            }
        }
    }


    private fun sessionExpired(activity: Activity, message: String?, prefsManager: PrefsManager) {
        try {
            alertDialog = AlertDialog.Builder(activity)
            alertDialog?.setCancelable(false)
            alertDialog?.setTitle(activity.getString(R.string.alert))
            alertDialog?.setMessage(message)
            alertDialog?.setPositiveButton(activity.getString(R.string.login)) { _, _ ->
                logoutUser(activity, prefsManager)
                alertDialog = null
            }
            alertDialog?.show()
        } catch (ignored: Exception) {
        }
    }

    private fun accountDeleted(activity: Activity, message: String?, prefsManager: PrefsManager) {
        try {
            alertDialog = AlertDialog.Builder(activity)
            alertDialog?.setCancelable(false)
            alertDialog?.setTitle(activity.getString(R.string.alert))
            alertDialog?.setMessage(message)
            alertDialog?.setPositiveButton(activity.getString(R.string.ok)) { _, _ ->
                logoutUser(activity, prefsManager)
                alertDialog = null
            }
            alertDialog?.show()

        } catch (ignored: Exception) {
        }
    }

    private fun errorMessage(activity: Activity, message: String?) {
        try {
            alertDialog = AlertDialog.Builder(activity)
            alertDialog?.setCancelable(false)
            alertDialog?.setTitle(activity.getString(R.string.alert))
            alertDialog?.setMessage(message)
            alertDialog?.setPositiveButton(activity.getString(R.string.ok)) { _, _ ->
                alertDialog = null
            }
            alertDialog?.show()

        } catch (ignored: Exception) {
        }

    }
}