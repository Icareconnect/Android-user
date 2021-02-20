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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.requests.SaveAddress
import com.consultantapp.data.models.responses.FilterOption
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityAddAddressBinding
import com.consultantapp.ui.dashboard.home.bookservice.registerservice.CheckItemAdapter
import com.consultantapp.utils.*
import com.consultantapp.utils.PermissionUtils
import com.consultantapp.utils.dialogs.ProgressDialog
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
import java.io.File
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

    private lateinit var adapterRelation: CheckItemAdapter

    private lateinit var viewModel: AddressViewModel

    private lateinit var progressDialog: ProgressDialog

    private var itemsSaveAs = ArrayList<FilterOption>()

    private var saveAs: FilterOption? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_address)

        setEditAddress()
        initialise()
        setListeners()
        setAdapter()
        bindObservers()
    }

    private fun initialise() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
        getLocationWithPermissionCheck()

        geoCoder = Geocoder(this, Locale.getDefault())
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        progressDialog = ProgressDialog(this)
        viewModel = ViewModelProvider(this, viewModelFactory)[AddressViewModel::class.java]
    }

    private fun setEditAddress() {
        if (intent.hasExtra(EXTRA_ADDRESS)) {
            saveAddress = intent.getSerializableExtra(EXTRA_ADDRESS) as SaveAddress
            saveAddress.addressId = saveAddress.id
            binding.etLocation.setText(saveAddress.address_name)
            binding.etHouseNo.setText(saveAddress.house_no)
        }
    }


    private fun setAdapter() {
        itemsSaveAs.clear()

        val optionsWorkLocation = ArrayList<FilterOption>()
        userRepository.getUser()?.master_preferences?.forEach {
            if (it.preference_type == PreferencesType.WORK_ENVIRONMENT && optionsWorkLocation.isEmpty()) {
                optionsWorkLocation.addAll(it.options ?: emptyList())
            }
        }
        //itemsSaveAs.addAll(optionsWorkLocation ?: emptyList())

        optionsWorkLocation.forEach {
            it.isSelected = false
            itemsSaveAs.add(it)
        }

        adapterRelation = CheckItemAdapter(null, false, false, itemsSaveAs)
        binding.rvAddressType.adapter = adapterRelation

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

        saveAs = null
        itemsSaveAs.forEach {
            if (it.isSelected) {
                saveAs = it
                return@forEach
            }
        }

        when {
            binding.etLocation.text.toString().isEmpty() -> {
                binding.etLocation.showSnackBar(getString(R.string.location))
            }
            saveAs == null -> {
                binding.etLocation.showSnackBar(getString(R.string.select_save_as_option))
            }
            isConnectedToInternet(this, true) -> {
                /*      val hashMap = HashMap<String, Any>()
                      hashMap["address_name"] = saveAddress.address_name ?: ""
                      hashMap["save_as"] = saveAs?.option_name ?: ""
                      hashMap["save_as_preference"] = Gson().toJson(saveAs ?: FilterOption())
                      hashMap["lat"] = saveAddress.location?.get(1) ?: ""
                      hashMap["long"] = saveAddress.location?.get(0) ?: ""
                      hashMap["house_no"] = binding.etHouseNo.text?.trim().toString()*/

                saveAddress.house_no = binding.etHouseNo.text?.trim().toString()
                saveAddress.lat = (saveAddress.location?.get(1) ?: "").toString()
                saveAddress.long = (saveAddress.location?.get(0) ?: "").toString()
                saveAddress.save_as = saveAs?.option_name
                saveAddress.save_as_preference = saveAs
                viewModel.saveAddress(saveAddress)
            }
        }
    }

    private fun bindObservers() {
        viewModel.saveAddress.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    saveAddress.house_no = binding.etHouseNo.text?.trim().toString()
                    saveAddress.save_as_preference = saveAs

                    val intent = Intent()
                    intent.putExtra(EXTRA_ADDRESS, saveAddress)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.AUTOCOMPLETE_REQUEST_CODE) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                binding.etLocation.setText(getAddress(place))

                saveAddress.address_name = binding.etLocation.text.toString()
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
            saveAddress.address_name = getAddress()

            binding.etLocation.setText(saveAddress.address_name)
        }
        isPlacePicker = false
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.isTrafficEnabled = false
        mMap?.setOnCameraChangeListener(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getLocationWithPermissionCheck()
            return
        }
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true


        if (!saveAddress.location.isNullOrEmpty()) {
            binding.etLocation.setText(saveAddress.address_name)
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
            saveAddress.address_name = getAddress()

            binding.etLocation.setText(saveAddress.address_name)
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
                saveAddress.location?.get(0)
                        ?: 0.0, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

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
