package com.consultantapp.utils.dialogs

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND
import com.consultantapp.R

class ProgressDialog() {

    private lateinit var dialog: Dialog

    constructor(context: Activity) : this() {
        val dialogView = View.inflate(context, R.layout.dialog_progress, null)
        dialog = Dialog(context, R.style.CustomDialog)
        dialog.window?.clearFlags(FLAG_DIM_BEHIND)
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)
    }

    private fun show() {
        if (!dialog.isShowing)
            dialog.show()
    }

    private fun dismiss() {
        if (dialog.isShowing)
            dialog.dismiss()
    }

    fun setLoading(isLoading: Boolean) {
        if (isLoading)
            show()
        else
            dismiss()
    }
}