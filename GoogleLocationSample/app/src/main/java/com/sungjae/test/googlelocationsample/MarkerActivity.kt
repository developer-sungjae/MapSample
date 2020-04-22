package com.sungjae.test.googlelocationsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MarkerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let { nGoogleMap ->
            this.map = nGoogleMap

            val SEOUL1 = LatLng(37.56, 126.97)
            val SEOUL2 = LatLng(37.55, 126.95)
            val SEOUL3 = LatLng(37.54, 126.94)
            val SEOUL4 = LatLng(37.57, 126.93)
            val SEOUL5 = LatLng(37.58, 126.92)


            val seoulMarker1 = MarkerOptions().apply {
                position(SEOUL1)
                title("서울1")
                snippet("인용문구1")
            }
            val seoulMarker2 = MarkerOptions().apply {
                position(SEOUL2)
                title("서울2")
                snippet("인용문구2")
            }
            val seoulMarker3 = MarkerOptions().apply {
                position(SEOUL3)
                title("서울3")
                snippet("인용문구3")
            }
            val seoulMarker4 = MarkerOptions().apply {
                position(SEOUL4)
                title("서울4")
                snippet("인용문구4")
            }
            val seoulMarker5 = MarkerOptions().apply {
                position(SEOUL5)
                title("서울5")
                snippet("인용문구5")
            }

            map.addMarker(seoulMarker1)
            map.addMarker(seoulMarker2)
            map.addMarker(seoulMarker3)
            map.addMarker(seoulMarker4)
            map.addMarker(seoulMarker5)
            map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL3))
//            map.moveCamera(CameraUpdateFactory.newLatLng(SEOUL2))
            map.animateCamera(CameraUpdateFactory.zoomTo(12F))
        }
    }
}
