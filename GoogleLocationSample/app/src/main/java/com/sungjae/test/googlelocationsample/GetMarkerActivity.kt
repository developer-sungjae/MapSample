package com.sungjae.test.googlelocationsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_get_marker.*

class GetMarkerActivity : AppCompatActivity() {

    private var extras: Bundle? = null
    private var title: String? = null
    private var address: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_marker)
        extras = intent.extras

        when (extras) {
            null -> title = "error"
            else -> {
                title = extras?.getString("title")
                address = extras?.getString("address")
            }
        }
        val str = title + '\n' + address + '\n'
        tv_content.text = str
    }
}
