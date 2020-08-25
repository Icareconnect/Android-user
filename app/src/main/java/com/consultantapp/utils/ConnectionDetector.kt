package com.consultantapp.utils

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AlertDialog
import com.consultantapp.R

private fun showNoInternetDialog(context: Context) {
    AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle(context.getString(R.string.internet))
            .setMessage(context.getString(R.string.check_internet))
            .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                /*val intent = Intent(Settings.ACTION_SETTINGS)
                context.startActivity(intent)*/
            }.show()
}

/*  fun showRetrofitErrorToast() {
      Toast.makeText(context, context.getString(R.string.might_problem), Toast.LENGTH_LONG).show()
  }*/

fun isConnectedToInternet(context: Context?, showAlert: Boolean): Boolean {
    val connectivity = context
            ?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivity.activeNetworkInfo

    return if (activeNetwork != null && activeNetwork.isConnected)
        true
    else {
        if (showAlert)
            showNoInternetDialog(context)
        false
    }
}