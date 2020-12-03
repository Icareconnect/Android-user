package com.consultantapp.data.models.responses.directions

class Route {
    var bounds: Bounds? = null
    var copyrights: String? = null
    var legs: List<Leg>? = null
    var overview_polyline: Overview_polyline? = null
    var summary: String? = null
    var warnings: List<Any>? = null
    var waypoint_order: List<Any>? = null
}