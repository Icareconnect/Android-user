package com.consultantapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.consultantapp.R


@SuppressLint("Registered")
/**
 * Created by ANQ on 8/8/2016.
 */

class GPSTracker(private val mContext: Context) : Service(), LocationListener {

    private var alertDialog: AlertDialog? = null

    private var canGetLocation = false

    private var loc: Location? = null
    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()
    private var locationManager: LocationManager? = null

    init {
        getLocation()
    }

    private fun getLocation() {

        try {
            locationManager = mContext
                .getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // getting GPS status
            val checkGPS = locationManager!!
                .isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            val checkNetwork = locationManager!!
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!checkGPS && !checkNetwork) {
                Toast.makeText(mContext, "No Service Provider Available", Toast.LENGTH_SHORT).show()
            } else {
                this.canGetLocation = true
                // First get location from Network Provider
                if (checkNetwork) {
                    //Toast.makeText(mContext, "Network", Toast.LENGTH_SHORT).show();

                    try {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        Log.d("Network", "Network")
                        if (locationManager != null) {
                            loc = locationManager!!
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                        }

                        if (loc != null) {
                            latitude = loc!!.latitude
                            longitude = loc!!.longitude
                        }
                    } catch (ignored: SecurityException) {
                        Log.d("ignored", ignored.toString())
                    }

                }
            }
            // if GPS Enabled get lat/long using GPS Services
            if (checkGPS) {
                //Toast.makeText(mContext, "GPS", Toast.LENGTH_SHORT).show();
                if (loc == null) {
                    try {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(), this)
                        Log.d("GPS Enabled", "GPS Enabled")
                        if (locationManager != null) {
                            loc = locationManager!!
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (loc != null) {
                                latitude = loc!!.latitude
                                longitude = loc!!.longitude
                            }
                        }
                    } catch (ignored: SecurityException) {

                    }

                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getLongitude(): Double {
        if (loc != null) {
            longitude = loc!!.longitude
        }
        return longitude
    }

    fun getLatitude(): Double {
        if (loc != null) {
            latitude = loc!!.latitude
        }
        return latitude
    }

    fun canGetLocation(): Boolean {
        return this.canGetLocation
    }

    fun showSettingsAlert(activity: Activity) {
        AlertDialog.Builder(activity )
            .setCancelable(false)
            .setTitle(activity.getString(R.string.gps_not_enabled))
            .setMessage(activity.getString(R.string.want_to_enable_gps))
            .setPositiveButton(activity.getString(R.string.ok)) { dialog, which ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                activity.startActivityForResult(intent, AppRequestCode.ASK_FOR_LOCATION)
            }.show()
    }


    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GPSTracker)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onLocationChanged(location: Location) {

    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

    }

    override fun onProviderEnabled(s: String) {

    }

    override fun onProviderDisabled(s: String) {

    }

    companion object {


        private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10


        private val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong()
    }
}