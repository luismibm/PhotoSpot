package com.luismibm.photospot

class PhotoSpot (
    var latitude: String,
    var longitude: String,
    var location: String,
    var description: String,
    var url: String
) {
    constructor(): this("", "", "", "", "")
}