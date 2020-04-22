package com.sungjae.test.googlelocationsample

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_find_location.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*


class FindLocationActivity : AppCompatActivity(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {
    companion object {
        const val TAG = "bsjbsj"
        const val GPS_ENABLE_REQUEST_CODE = 9999
        const val UPDATE_INTERVAL_MS: Long = 5000
        const val PERMISSIONS_REQUEST_CODE = 100
        val LOCATION_PERMISSIONS =
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    private lateinit var locationManager: LocationManager
    private var arrMarkerInActivity: ArrayList<MarkerData>? = null
    private var map: GoogleMap? = null
    private var cameraCnt = 0
    private var needRequest = false
    private var locationPrvClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    private var locationCallback: LocationCallback? = null
    private var location: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_find_location)

        (supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment).getMapAsync(this)

        arrMarkerInActivity = ArrayList<MarkerData>()
        locationPrvClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setLocationCallback()
        setLocationRequest()

        val setOnClickHandler = View.OnClickListener {
            if (!isAllPermissionChecked()) {
                toast("모든 권한을 허용해주셔야 서비스 이용이 가능합니다.")
                return@OnClickListener
            }
            when (it) {
                btnStartService ->
                    if (isAllPermissionChecked()) {
                        locationPrvClient?.removeLocationUpdates(locationCallback)
                        startService(Intent(this, TrackService::class.java))
                    }

                btnStopService -> stopService(Intent(this, TrackService::class.java))

                btnStartForwardService -> locationPrvClient?.requestLocationUpdates(locationRequest, locationCallback, null)

                btnStopForwardService -> locationPrvClient?.removeLocationUpdates(locationCallback)

                btnMarker ->
                    arrMarkerInActivity?.let { arrMarker ->
                        for (idx in 0 until arrMarker.count()) {
                            map?.addMarker(
                                MarkerOptions()
                                    .position(LatLng(arrMarker[idx].latitude, arrMarker[idx].longitude))
                                    .anchor(0.5f, 0.5f)
                                    .title(arrMarker[idx].title)
                                    .snippet(arrMarker[idx].snippet)
                            )
                        }
                        toast("쌓아둔 데이터 마커에 연동")
                    } ?: run { toast("데이터가 없습니다.") }
            }
        }
        btnStartService.setOnClickListener(setOnClickHandler)
        btnStopService.setOnClickListener(setOnClickHandler)
        btnStartForwardService.setOnClickListener(setOnClickHandler)
        btnStopForwardService.setOnClickListener(setOnClickHandler)
        btnMarker.setOnClickListener(setOnClickHandler)

        getDataFromService()
    }

    private fun setLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                val locationList = locationResult?.locations
                location = locationList?.get(locationList.size - 1)
                location?.let {
                    val currentLat = it.latitude
                    val currentLng = it.longitude
                    val currentPosition = LatLng(currentLat, currentLng)
                    val markerSnippet = "위도:${it.latitude}" + " 경도:${it.longitude}"

                    arrMarkerInActivity?.add(
                        MarkerData(currentLat, currentLng, currentPosition.addressTitle(), markerSnippet)
                    )

                    toast(markerSnippet)
                    Log.d(TAG, "activity markerSnippet:$markerSnippet")
                    if (cameraCnt == 0) {
                        val cameraUpdate = CameraUpdateFactory.newLatLng(currentPosition)
                        map?.moveCamera(cameraUpdate)
                    }
                }
                cameraCnt++
            }
        }
    }

    private fun getDataFromService() {
        val markerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val arrMarkerInService = intent.getSerializableExtra("MarkerList") as ArrayList<MarkerData>?
                arrMarkerInService?.let { arrMarkerInActivity?.addAll(it) }
                Log.d(TAG, "arrMarkerInService:$arrMarkerInService")
            }
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(markerReceiver, IntentFilter("SendMarker"))
        Log.d(TAG, "markerReceiver arrMarker:$arrMarkerInActivity")
    }

    override fun onStart() {
        super.onStart()
        map?.isMyLocationEnabled = true
    }

    override fun onResume() {
        super.onResume()
        map?.clear()
        if (arrMarkerInActivity?.count() ?: 0 != 0) {
            val lastCnt = arrMarkerInActivity?.count()?.minus(1) ?: 0
            val lat = arrMarkerInActivity?.get(lastCnt)?.latitude ?: 0.0
            val lng = arrMarkerInActivity?.get(lastCnt)?.longitude ?: 0.0
            val cameraUpdate = CameraUpdateFactory.newLatLng(LatLng(lat, lng))
            map?.moveCamera(cameraUpdate)
        }
    }

    private fun setLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL_MS

            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(this)
        }
    }


    override fun onMapReady(googleMap: GoogleMap?) {
        Log.d(TAG, "onMapReady")
        this.map = googleMap
        when (isFineLocationPermissionChecked() && isCoarseLocationPermissionChecked()) {
            true ->
                if (!checkProviderStatus()) {
                    AlertDialog.Builder(this).apply {
                        setTitle("위치 서비스 비활성화")
                        setMessage("""앱을 사용하기 위해서는 위치 서비스가 필요합니다. 위치 설정을 수정하실래요?""".trimIndent())
                        setCancelable(true)
                        setPositiveButton("설정") { _, _ -> startActivityForResult(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), GPS_ENABLE_REQUEST_CODE) }
                        setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
                        create()
                        show()
                    }
                }

            false ->
                when (ActivityCompat.shouldShowRequestPermissionRationale(this, LOCATION_PERMISSIONS[0])) {
                    true -> {
                        Snackbar.make(layout_marker, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", LENGTH_INDEFINITE)
                            .setAction("확인") { ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, PERMISSIONS_REQUEST_CODE) }.show()
                    }

                    false -> ActivityCompat.requestPermissions(this, LOCATION_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
                }
        }

        map?.apply {
            uiSettings?.isMyLocationButtonEnabled = true
            animateCamera(CameraUpdateFactory.zoomTo(15F))
            setOnMapClickListener {
                Log.d(TAG, "onMapClick :")


            }
        }
    }

    //지오코더 : GPS를 주소로 변환
    private fun LatLng.addressTitle(): String {
        this.let {
            return try {
                val geoCoder = Geocoder(this@FindLocationActivity, Locale.getDefault())
                val addresses: List<Address>? = geoCoder.getFromLocation(it.latitude, it.longitude, 1)

                when (addresses.isNullOrEmpty()) {
                    true -> "주소 미발견"
                    false -> addresses[0].getAddressLine(0).toString()
                }
            } catch (e: Exception) {
                when (e) {
                    is IOException -> "지오코더 서비스 사용불가"
                    is IllegalArgumentException -> "잘못된 GPS 좌표"
                    else -> "에러 발생"
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //사용자가 GPS 활성 시켰는지 검사
        when (requestCode) {
            GPS_ENABLE_REQUEST_CODE ->
                if (checkProviderStatus()) {
                    Log.d(TAG, "onActivityResult : GPS 활성화 되있음")
                    needRequest = true
                    return
                }
        }
    }

    private fun checkProviderStatus(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    private fun isAllPermissionChecked(): Boolean = isFineLocationPermissionChecked() && isCoarseLocationPermissionChecked()
    private fun isFineLocationPermissionChecked(): Boolean = ContextCompat.checkSelfPermission(this, LOCATION_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
    private fun isCoarseLocationPermissionChecked(): Boolean = ContextCompat.checkSelfPermission(this, LOCATION_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED
}
