package com.consultantapp.ui.dashboard.appointment.appointmentStatus

import android.animation.ValueAnimator
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.R
import com.consultantapp.data.models.responses.Request
import com.consultantapp.data.network.ApisRespHandler
import com.consultantapp.data.network.responseUtil.Status
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityAppointmentStatusBinding
import com.consultantapp.ui.dashboard.appointment.AppointmentViewModel
import com.consultantapp.utils.*
import com.consultantvendor.data.models.responses.directions.Overview_polyline
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.android.support.DaggerAppCompatActivity
import io.socket.emitter.Emitter
import org.json.JSONObject
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.sign

class AppointmentStatusActivity : DaggerAppCompatActivity(), OnMapReadyCallback {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var appSocket: AppSocket

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: ActivityAppointmentStatusBinding

    private lateinit var viewModelDirection: DirectionViewModel

    private lateinit var viewModel: AppointmentViewModel

    private var mapFragment: SupportMapFragment? = null

    private var mMap: GoogleMap? = null

    private lateinit var placeLatLng: LatLng

    private lateinit var finalLatLng: LatLng

    private var request: Request? = null

    private var polyline: Polyline? = null

    private var markerToMove: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_appointment_status)

        initialise()
        setListeners()
        setRequestData()
        bindObservers()
    }

    private fun initialise() {
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment?.getMapAsync(this)
        viewModelDirection = ViewModelProvider(this, viewModelFactory)[DirectionViewModel::class.java]
        viewModel = ViewModelProvider(this, viewModelFactory)[AppointmentViewModel::class.java]

        // These flags ensure that the activity can be launched when the screen is locked.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun setRequestData() {
        request = intent.getSerializableExtra(EXTRA_REQUEST_ID) as Request
        finalLatLng = LatLng(request?.extra_detail?.lat?.toDouble()
                ?: 0.0, request?.extra_detail?.long?.toDouble() ?: 0.0)

        binding.tvName.text = request?.to_user?.name
        binding.tvTime.text = request?.time
        loadImage(binding.ivPic, request?.to_user?.profile_image,
                R.drawable.ic_profile_placeholder)

        if (request?.status == CallAction.INPROGRESS) {
            if (isConnectedToInternet(this, true)) {
                val hashMap = HashMap<String, String>()
                hashMap["request_id"] = request?.id ?: ""
                viewModel.requestDetail(hashMap)
            }
        } else {
            binding.tvTime.gone()
            binding.ivCall.visible()
            binding.tvStatusV.text = getString(R.string.reached_destination)

            mMap?.moveCamera(CameraUpdateFactory.newLatLng(finalLatLng))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))
        }

    }


    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun drawPolyLineApi() {
        runOnUiThread {
            if (request?.status == CallAction.INPROGRESS && markerToMove == null) {
                markerToMove = mMap?.addMarker(MarkerOptions()
                        .position(placeLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_arrow))
                        .anchor(0.5f, 0.5f)
                        .flat(true))
            } else
                animateMarker()

            if (isConnectedToInternet(this, true)) {
                val hashMap = HashMap<String, String>()
                hashMap["origin"] = "${placeLatLng.latitude},${placeLatLng.longitude}"
                hashMap["destination"] = "${finalLatLng.latitude},${finalLatLng.longitude}"
                hashMap["key"] = getString(R.string.google_places_api_key)
                viewModelDirection.directions(hashMap)
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.isTrafficEnabled = false

        // mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.isMyLocationButtonEnabled = true

        mMap?.addMarker(MarkerOptions()
                .position(finalLatLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_drop_location_mrkr))
                .anchor(0.5f, 0.5f)
                .flat(true))

        if (request?.status == CallAction.INPROGRESS && markerToMove == null && ::placeLatLng.isInitialized) {
            markerToMove = mMap?.addMarker(MarkerOptions()
                    .position(placeLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_arrow))
                    .anchor(0.5f, 0.5f)
                    .flat(true))
        } else if (::finalLatLng.isInitialized) {
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(finalLatLng))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))
        }
    }

    private fun animateMarker() {
        if (markerToMove != null) {
            val locationStart = Location(LocationManager.GPS_PROVIDER)
            locationStart.latitude = placeLatLng.latitude
            locationStart.longitude = placeLatLng.longitude

            val startPosition = markerToMove?.position ?: LatLng(0.0, 0.0)
            val endPosition = placeLatLng
            val startRotation = markerToMove?.rotation ?: 0f
            val latLngInterpolator: LatLngInterpolator = LatLngInterpolator.LinearFixed()
            val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
            valueAnimator.duration = 1000 // duration 1 second
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.addUpdateListener { animation ->
                try {
                    val v = animation.animatedFraction
                    val newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition)
                            ?: LatLng(0.0, 0.0)
                    markerToMove?.position = newPosition
                    markerToMove?.rotation = computeRotation(v, startRotation, locationStart.bearing)
                } catch (ex: Exception) {
                    // I don't care atm..
                }
            }
            valueAnimator.start()
        }
    }

    private interface LatLngInterpolator {
        fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng?
        class LinearFixed : LatLngInterpolator {
            override fun interpolate(fraction: Float, a: LatLng, b: LatLng): LatLng {
                val lat = (b.latitude - a.latitude) * fraction + a.latitude
                var lngDelta = b.longitude - a.longitude
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= sign(lngDelta) * 360
                }
                val lng = lngDelta * fraction + a.longitude
                return LatLng(lat, lng)
            }
        }
    }

    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizeEnd = end - start // rotate start to 0
        val normalizedEndAbs = (normalizeEnd + 360) % 360
        val direction = if (normalizedEndAbs > 180) (-1).toFloat() else 1.toFloat() // -1 = anticlockwise, 1 = clockwise
        val rotation: Float
        rotation = if (direction > 0) {
            normalizedEndAbs
        } else {
            normalizedEndAbs - 360
        }
        val result = fraction * rotation + start
        return (result + 360) % 360
    }


    private fun bindObservers() {
        viewModel.requestDetail.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    runOnUiThread {
                        request = it.data?.request_detail ?: Request()

                        finalLatLng = LatLng(request?.extra_detail?.lat?.toDouble()
                                ?: 0.0, request?.extra_detail?.long?.toDouble() ?: 0.0)


                        if (request?.status == CallAction.INPROGRESS && request?.last_location != null) {
                            placeLatLng = LatLng(request?.last_location?.lat
                                    ?: 0.0, request?.last_location?.long ?: 0.0)

                            mMap?.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng))
                            mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))

                            drawPolyLineApi()
                        } else {
                            mMap?.moveCamera(CameraUpdateFactory.newLatLng(finalLatLng))
                            mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))
                        }
                    }

                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, this, prefsManager)
                }
                Status.LOADING -> {
                }
            }
        })

        viewModelDirection.directions.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    if (request?.status == CallAction.INPROGRESS) {
                        binding.tvTime.text = getString(R.string.estimate_time_of_arrival_s,
                                it.data?.routes?.get(0)?.legs?.get(0)?.duration?.text)
                        drawDirectionToStop(it.data?.routes?.get(0)?.overview_polyline)
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

    override fun onBackPressed() {
        if (request?.status == CallAction.INPROGRESS) {
            AlertDialogUtil.instance.createOkCancelDialog(this, R.string.quit,
                    R.string.quit_message, R.string.yes, R.string.no, false,
                    object : AlertDialogUtil.OnOkCancelDialogListener {
                        override fun onOkButtonClicked() {
                            finish()
                        }

                        override fun onCancelButtonClicked() {
                        }
                    }).show()
        } else
            super.onBackPressed()
    }

    private fun drawDirectionToStop(overviewPolyline: Overview_polyline?) {
        runOnUiThread {
            if (overviewPolyline != null) {
                val polyz = decodeOverviewPolyLinePonts(overviewPolyline.points)
                if (polyz != null) {
                    val lineOptions = PolylineOptions()
                    lineOptions.addAll(polyz)
                    lineOptions.width(20f)
                    lineOptions.color(ContextCompat.getColor(this, R.color.colorBlack))

                    polyline?.remove()

                    polyline = mMap?.addPolyline(lineOptions)
                    mMap?.moveCamera(CameraUpdateFactory.newLatLng(placeLatLng))
                    mMap?.animateCamera(CameraUpdateFactory.zoomTo(14f))

                }
            }
        }
    }


    //This function is to parse the value of "points"
    private fun decodeOverviewPolyLinePonts(encoded: String?): List<LatLng>? {
        val poly = ArrayList<LatLng>()
        if (encoded != null && encoded.isNotEmpty() && encoded.trim { it <= ' ' }.isNotEmpty()) {
            var index = 0
            val len = encoded.length
            var lat = 0
            var lng = 0
            while (index < len) {
                var b: Int
                var shift = 0
                var result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lat += dlat
                shift = 0
                result = 0
                do {
                    b = encoded[index++].toInt() - 63
                    result = result or (b and 0x1f shl shift)
                    shift += 5
                } while (b >= 0x20)
                val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
                lng += dlng
                val p = LatLng(lat.toDouble() / 1E5,
                        lng.toDouble() / 1E5)
                poly.add(p)
            }
        }
        return poly
    }

    override fun onStart() {
        super.onStart()
        appSocket.on(AppSocket.Events.SEND_LIVE_LOCATION, listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        appSocket.off(AppSocket.Events.SEND_LIVE_LOCATION, listener)
    }

    private val listener = Emitter.Listener {
        Timber.e("SEND_LIVE_LOCATION $it")

        val data = it[0] as JSONObject
        val senderId = data.getString("senderId")
        val receiverId = data.getString("receiverId")
        val request_id = data.getString("request_id")
        val lat = data.getString("lat")
        val long = data.getString("long")

        if (request_id == request?.id) {
            placeLatLng = LatLng(lat.toDouble(), long.toDouble())

            drawPolyLineApi()
        }
    }
}
