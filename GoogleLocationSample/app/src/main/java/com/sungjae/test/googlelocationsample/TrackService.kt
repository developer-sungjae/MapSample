package com.sungjae.test.googlelocationsample

import android.app.Service
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.sungjae.test.googlelocationsample.FindLocationActivity.Companion.TAG
import java.io.IOException
import java.util.*

class TrackService : Service(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var locationCallback: LocationCallback? = null
    private var arrMarkerInService: ArrayList<MarkerData>? = null
    private var locationPrvClient: FusedLocationProviderClient? = null
    private var location: Location? = null
    private var locationRequest: LocationRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        arrMarkerInService = ArrayList<MarkerData>()
        locationPrvClient = LocationServices.getFusedLocationProviderClient(this)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "service onStartCommand")

        setLocationCallback()
        setLocationRequest()

        locationPrvClient?.requestLocationUpdates(locationRequest, locationCallback, null)
        return super.onStartCommand(intent, flags, startId)
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
                    Log.d(TAG, "service markerSnippet : $markerSnippet")
                    arrMarkerInService?.add(
                        MarkerData(currentLat, currentLng, currentPosition.addressTitle(), markerSnippet)
                    )

                }
            }
        }
    }

    private fun setLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest?.apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = FindLocationActivity.UPDATE_INTERVAL_MS

            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(this)
        }
    }

    //지오코더 : GPS를 주소로 변환
    private fun LatLng.addressTitle(): String {
        this.let {
            return try {
                val geoCoder = Geocoder(this@TrackService, Locale.getDefault())
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

    private fun sendMessage() = arrMarkerInService

    override fun onDestroy() {
        super.onDestroy()

        val i = Intent("SendMarker")
        i.putExtra("MarkerList", arrMarkerInService)
        LocalBroadcastManager.getInstance(this).sendBroadcast(i)
        locationPrvClient?.removeLocationUpdates(locationCallback)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }
}