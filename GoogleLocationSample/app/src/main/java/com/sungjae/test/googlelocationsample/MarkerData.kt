package com.sungjae.test.googlelocationsample

data class MarkerData(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var title: String? = null,
    var snippet: String? = null
    /*var iconResource: Int? = null*/
)