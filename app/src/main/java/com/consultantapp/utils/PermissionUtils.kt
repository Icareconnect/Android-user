package com.consultantapp.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.consultantapp.R
import permissions.dispatcher.PermissionRequest

object PermissionUtils {
    fun showRationalDialog(context: Context, @StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(context)
            .setPositiveButton(context.getString(R.string.allow), { _, _ -> request.proceed() })
            .setNegativeButton(context.getString(R.string.deny), { _, _ -> request.cancel() })
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }

    fun showAppSettingsDialog(context: Context, @StringRes messageResId: Int) {
        AlertDialog.Builder(context)
            .setPositiveButton(context.getString(R.string.settings)) { _, _ ->
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                intent.data = Uri.fromParts("package", context.packageName, null)
                context.startActivity(intent)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, which -> dialog?.dismiss() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }
}