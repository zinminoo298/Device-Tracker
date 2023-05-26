package com.xpand.devicetracker.Model

class LocationModel {
    var latitude:String? = null
    var longitude:String? = null
    var location:String? = null
    var time:String? = null

    constructor(latitude:String, longitude:String, location:String, time:String){
        this.latitude = latitude
        this.longitude = longitude
        this.location = location
        this.time = time
    }
}