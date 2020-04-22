package com.sungjae.test.googlelocationsample

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_main.*
import noman.googleplaces.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


class PlaceActivity : AppCompatActivity(), OnMapReadyCallback,
    ActivityCompat.OnRequestPermissionsResultCallback, PlacesListener {

    private lateinit var map: GoogleMap
    var previousMarker: ArrayList<Marker>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previousMarker = ArrayList()

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        button.setOnClickListener {
//            showPlaceInformation(currentPosition)

        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let { nGoogleMap ->
            this.map = nGoogleMap
//            nGoogleMap.setOnInfoWindowClickListener { marker ->
//                val i = Intent(baseContext, GetMarkerActivity::class.java)
//                val title: String = marker.title
//                val address: String = marker.snippet
//                i.putExtra("title", title)
//                i.putExtra("address", address)
//                startActivity(i)
//            }


        }
    }

    override fun onPlacesFailure(e: PlacesException?) {
    }

    override fun onPlacesSuccess(places: MutableList<Place>?) {
        places?.let { nPlaces ->
            runOnUiThread {
                for (place in nPlaces) {
                    val latLng = LatLng(
                        place.latitude
                        , place.longitude
                    )
                    val markerSnippet: String = getCurrentAddress(latLng)
                    val markerOptions = MarkerOptions()
                    markerOptions.position(latLng)
                    markerOptions.title(place.name)
                    markerOptions.snippet(markerSnippet)
                    val item: Marker = map.addMarker(markerOptions)
                    previousMarker?.add(item)
                }

                //중복 마커 제거
                val hashSet = HashSet<Marker>()
                previousMarker?.let { nPreviousMarker ->
                    hashSet.addAll(nPreviousMarker)
                    nPreviousMarker.clear()
                    nPreviousMarker.addAll(hashSet)
                }
            }
        }
    }

    override fun onPlacesFinished() {
    }

    override fun onPlacesStart() {
    }

    private fun showPlaceInformation(location: LatLng) {
        map.clear() //지도 클리어
        previousMarker?.clear() //지역정보 마커 클리어
        NRPlaces.Builder()
            .listener(this)
            .key("AIzaSyA1boEwm8htDTMip5CjxuNavhX1yG0Fndw")
            .latlng(location.latitude, location.longitude) //현재 위치
            .radius(500) //500 미터 내에서 검색
            .type(PlaceType.RESTAURANT) //음식점
            .build()
            .execute()
    }

    private fun getCurrentAddress(latlng: LatLng): String {
        //지오코더 -> GPS를 주소로 변환
        val geoCoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address>?

        addresses =
            try {
                geoCoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
            } catch (ioException: IOException) {
                //네트워크 문제
                toast("지오코더 서비스 사용불가")
                return "지오코더 서비스 사용불가"
            } catch (illegalArgumentException: IllegalArgumentException) {
                toast("잘못된 GPS 좌표")
                return "잘못된 GPS 좌표"
            }


        return if (addresses == null || addresses.isEmpty()) {
            toast("주소 미발견")
            "주소 미발견"
        } else {
            val address: Address = addresses[0]
            address.getAddressLine(0).toString()
        }
    }
}
