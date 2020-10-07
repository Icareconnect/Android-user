package com.consultantvendor.data.models.responses.directions

class Leg {
    var distance: Distance? = null
    var duration: Duration? = null
    var end_address: String? = null
    var end_location: End_location? = null
    var start_address: String? = null
    var start_location: End_location? = null
    var steps: List<Step>? = null
    var traffic_speed_entry: List<Any>? = null
    var via_waypoint: List<Any>? = null
}