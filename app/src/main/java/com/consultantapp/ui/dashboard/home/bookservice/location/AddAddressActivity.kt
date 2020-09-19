package com.consultantapp.ui.dashboard.home.bookservice.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.MotionEvent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.SaveAddress
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityAddAddressBinding
import com.consultantapp.utils.*
import com.consultantapp.utils.PermissionUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.widget.Autocomplete
import dagger.android.support.DaggerAppCompatActivity
import permissions.dispatcher.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@RuntimePermissions
class AddAddressActivity : DaggerAppCompatActivity(), GoogleMap.OnCameraChangeListener, OnMapReadyCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: ActivityAddAddressBinding

    private var saveAddress = SaveAddress()

    private var fromLat = 0.0
    private var fromLng = 0.0
    private var center: LatLng? = null

    private var mapFragment: SupportMapFragment? = null
    private var isPlacePicker = false
    private var mMap: GoogleMap? = null
    private var placeLatLng: LatLng? = null
    private lateinit var geoCoder: Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_address)

        setEditAddress()
        initialise()
        setListeners()
    }

    private fun initialise() {

        geoCoder = Geocoder(this, Locale.getDefault())
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        getLocationWithPermissionCheck()

    }

    private fun setEditAddress() {
        if (intent.hasExtra(EXTRA_ADDRESS)) {
            saveAddress = intent.getSerializableExtra(EXTRA_ADDRESS) as SaveAddress
            saveAddress.addressId = saveAddress._id
            binding.etLocation.setText(saveAddress.locationName)
            binding.etHouseNo.setText(saveAddress.houseNumber)
        }
    }


    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.tvChange.setOnClickListener {
            placePicker(null, this)
        }

        binding.btnSave.setOnClickListener {
            checkValidations()
        }

        binding.transparentImage.setOnTouchListener { v, event ->
            val action = event.action
            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    // Disallow ScrollView to intercept touch events.
                    binding.scrollMap.requestDisallowInterceptTouchEvent(true)
                    // Disable touch on transparent view
                    false
                }

                MotionEvent.ACTION_UP -> {
                    // Allow ScrollView to intercept touch events.
                    binding.scrollMap.requestDisallowInterceptTouchEvent(false)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    binding.scrollMap.requestDisallowInterceptTouchEvent(true)
                    false
                }

                else -> true
            }
        }
    }

    private fun checkValidations() {
        binding.btnSave.hideKeyboard()
        when {
            binding.etLocation.text.toString().isEmpty() -> {
                binding.etLocation.showSnackBar(getString(R.string.location))
            }
            binding.etHouseNo.text.toString().isEmpty() -> {
                binding.etHouseNo.showSnackBar(getString(R.string.house_no))
            }
            else -> {
                saveAddress.houseNumber = binding.etHouseNo.text?.trim().toString()

                val intent = Intent()
                intent.putExtra(EXTRA_ADDRESS, saveAddress)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.AUTOCOMPLETE_REQUEST_CODE) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                binding.etLocation.setText(getAddress(place))

                saveAddress.locationName = binding.etLocation.text.toString()
                saveAddress.location = ArrayList()
                saveAddress.location?.add(place.latLng?.longitude ?: 0.0)
                saveAddress.location?.add(place.latLng?.latitude ?: 0.0)

                isPlacePicker = true
                mMap?.moveCamera(CameraUpdateFactory.newLatLng(place.latLng))
                mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))

                LocaleHelper.setLocale(this, userRepository.getUserLanguage(), prefsManager)
            }
        }
    }


    override fun onCameraChange(cameraPosition: CameraPosition) {
        center = mMap?.cameraPosition?.target
        if (!isPlacePicker) {
            placeLatLng = center

            fromLat = placeLatLng?.latitude ?: 0.0
            fromLng = placeLatLng?.longitude ?: 0.0
            ChangeLocation().execute("")
        }
        isPlacePicker = false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.isTrafficEnabled = false
        mMap?.setOnCameraChangeListener(this)

        // mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        //GeneralFunctions.moveMapButton(mapFragment)

        // Get user current location
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (!saveAddress.location.isNullOrEmpty()) {
            binding.etLocation.setText(saveAddress.locationName)
            val current = LatLng(saveAddress.location?.get(1) ?: 0.0, saveAddress.location?.get(0)
                    ?: 0.0)
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(current))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))
        }


        val gps = GPSTracker(this)
        // check if GPS location can get Location
        if (gps.canGetLocation() && statusOfGPS) {
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                if (saveAddress.location.isNullOrEmpty()) {
                    intent.removeExtra(EXTRA_ADDRESS)
                    val current = LatLng(gps.getLatitude(), gps.getLongitude())
                    placeLatLng = current
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f))
                    CheckLocation().execute("")
                }
            }
        } else {
            gps.showSettingsAlert(this)
        }

    }

    @SuppressLint("StaticFieldLeak")
    private inner class ChangeLocation : AsyncTask<String, Void, String>() {
        var name = ""

        override fun doInBackground(vararg params: String): String? {

            val addresses: List<Address>
            try {
                addresses = geoCoder.getFromLocation(fromLat, fromLng, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


                if (addresses.isNotEmpty()) {
                    name = when {
                        addresses[0].getAddressLine(0) != null -> addresses[0].getAddressLine(0)
                        addresses[0].featureName != null -> addresses[0].featureName
                        addresses[0].locality != null -> addresses[0].locality
                        else -> addresses[0].adminArea
                    }
                }

            } catch (ignored: Exception) {
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            try {
                super.onPostExecute(result)
                runOnUiThread {
                    if (intent.hasExtra(EXTRA_ADDRESS)) {
                        intent.removeExtra(EXTRA_ADDRESS)
                    } else {
                        binding.etLocation.setText(name)

                        saveAddress.locationName = binding.etLocation.text.toString()
                        saveAddress.location = ArrayList()
                        saveAddress.location?.add(fromLng)
                        saveAddress.location?.add(fromLat)
                    }
                }

            } catch (ignored: Exception) {
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private inner class CheckLocation : AsyncTask<String, Void, String>() {
        var name = ""

        override fun doInBackground(vararg params: String): String? {

            //Get user current location
            val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val statusOfGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            val gps = GPSTracker(this@AddAddressActivity)
            // check if GPS location can get Location
            if (gps.canGetLocation() && statusOfGPS) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    val addresses: List<Address>
                    try {
                        addresses = geoCoder.getFromLocation(gps.getLatitude(), gps.getLongitude(), 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5


                        if (addresses.isNotEmpty()) {
                            name = when {
                                addresses[0].getAddressLine(1) != null -> addresses[0].getAddressLine(1)
                                addresses[0].featureName == null -> addresses[0].adminArea
                                else -> String.format("%s, %s", addresses[0].featureName, addresses[0].locality)
                            }
                        }

                        runOnUiThread {
                            binding.etLocation.setText(name)
                            fromLat = gps.getLatitude()
                            fromLng = gps.getLongitude()

                            saveAddress.locationName = binding.etLocation.text.toString()
                            saveAddress.location = ArrayList()
                            saveAddress.location?.add(fromLng)
                            saveAddress.location?.add(fromLat)
                        }
                    } catch (ignored: Exception) {
                    }

                }
            } else {
                if (!statusOfGPS)
                    gps.showSettingsAlert(this@AddAddressActivity)
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            try {
                super.onPostExecute(result)
                runOnUiThread {
                    binding.etLocation.setText(name)

                    saveAddress.locationName = binding.etLocation.text.toString()
                    saveAddress.location = ArrayList()
                    saveAddress.location?.add(fromLng)
                    saveAddress.location?.add(fromLat)
                }
            } catch (ignored: Exception) {
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getLocation() {
        mapFragment?.getMapAsync(this)
    }

    @OnShowRationale(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(this, R.string.we_will_need_your_location, request)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
                this,
                R.string.we_will_need_your_location)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(
                this, R.string.we_will_need_your_location)
    }

    companion object {
        const val EXTRA_ADDRESS = "EXTRA_ADDRESS"
    }

}
