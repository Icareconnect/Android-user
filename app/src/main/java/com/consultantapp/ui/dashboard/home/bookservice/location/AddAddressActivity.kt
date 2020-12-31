package com.consultantapp.ui.dashboard.home.bookservice.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.SaveAddress
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityAddAddressBinding
import com.consultantapp.utils.*
import com.consultantapp.utils.PermissionUtils
import com.google.android.gms.location.*
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

    private var mapFragment: SupportMapFragment? = null

    private var isPlacePicker = false

    private var mMap: GoogleMap? = null

    private lateinit var geoCoder: Geocoder

    lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_address)

        setEditAddress()
        initialise()
        setListeners()
    }

    private fun initialise() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
        getLocationWithPermissionCheck()

        geoCoder = Geocoder(this, Locale.getDefault())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
        if (!isPlacePicker) {
            val latLng = mMap?.cameraPosition?.target

            saveAddress.location = ArrayList()
            saveAddress.location?.add(latLng?.longitude ?: 0.0)
            saveAddress.location?.add(latLng?.latitude ?: 0.0)
            saveAddress.locationName = getAddress()

            binding.etLocation.setText(saveAddress.locationName)
        }
        isPlacePicker = false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.isTrafficEnabled = false
        mMap?.setOnCameraChangeListener(this)

        // mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true


        if (!saveAddress.location.isNullOrEmpty()) {
            binding.etLocation.setText(saveAddress.locationName)
            val current = LatLng(saveAddress.location?.get(1) ?: 0.0, saveAddress.location?.get(0)
                    ?: 0.0)
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(current))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(15f))
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                requestNewLocationData()
            } else {
                Toast.makeText(this, R.string.we_will_need_your_location, Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            getLocationWithPermissionCheck()
        }
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        runOnUiThread {
            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 0
            mLocationRequest.fastestInterval = 0
            mLocationRequest.numUpdates = 1

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                Looper.myLooper())

        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)

            mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))

            saveAddress.location = ArrayList()
            saveAddress.location?.add(latLng.longitude)
            saveAddress.location?.add(latLng.latitude)
            saveAddress.locationName = getAddress()

            binding.etLocation.setText(saveAddress.locationName)
            //placeLatLng = LatLng(30.7457, 76.7332)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)
    }


    private fun getAddress(): String {
        var locationName = ""
        val addresses: List<Address> = geoCoder.getFromLocation(saveAddress.location?.get(1) ?: 0.0,
            saveAddress.location?.get(0) ?: 0.0, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

        if (addresses.isNotEmpty()) {
            locationName = when {
                addresses[0].getAddressLine(0) != null -> addresses[0].getAddressLine(0)
                addresses[0].featureName != null -> addresses[0].featureName
                addresses[0].locality != null -> addresses[0].locality
                else -> addresses[0].adminArea
            }
        }

        return locationName
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun getLocation() {
        if (saveAddress.location.isNullOrEmpty())
            getLastLocation()
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
