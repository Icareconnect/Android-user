package com.consultantapp.utils

import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.consultantapp.R


class AlertDialogUtil {

    fun createOkCancelDialog(context: Context, @StringRes titleResourceId: Int, @StringRes messageResourceId: Int,
                             @StringRes positiveResourceId: Int, @StringRes negativeResourceId: Int, cancelable: Boolean,
                             listener: OnOkCancelDialogListener?): AlertDialog {
        val alertDialog = AlertDialog.Builder(context)
        if (titleResourceId != 0) {
            alertDialog.setTitle(titleResourceId)
        }
        if (titleResourceId != 0) {
            alertDialog.setMessage(messageResourceId)
        }
        alertDialog.setCancelable(cancelable)
        alertDialog.setPositiveButton(positiveResourceId
        ) { dialog, which ->
            listener?.onOkButtonClicked()
            dialog.dismiss()
        }
        if (negativeResourceId != 0) {
            alertDialog.setNegativeButton(negativeResourceId) { dialog, which ->
                listener?.onCancelButtonClicked()
                dialog.dismiss()
            }
        }
        val dialog = alertDialog.create()
        dialog.setOnShowListener { dialogInterface ->
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(ContextCompat
                .getColor(context, R.color.colorPrimary))
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(ContextCompat
                .getColor(context, R.color.colorPrimary))
        }
        return dialog
    }


    interface OnOkCancelDialogListener {
        fun onOkButtonClicked()

        fun onCancelButtonClicked()
    }

    companion object {

        private var mInstance: AlertDialogUtil? = null

        val instance: AlertDialogUtil
            get() {
                if (null == mInstance) {
                    mInstance = AlertDialogUtil()
                }
                return mInstance ?: AlertDialogUtil()
            }
    }
}