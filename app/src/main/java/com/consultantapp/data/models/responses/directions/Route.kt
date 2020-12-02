package com.consultantapp.data.models.responses.directions

import com.consultantapp.data.models.responses.directions.Bounds
import com.consultantapp.data.models.responses.directions.Leg
import com.consultantapp.data.models.responses.directions.Overview_polyline

class Route {
    var bounds: Bounds? = null
    var copyrights: String? = null
    var legs: List<Leg>? = null
    var overview_polyline: Overview_polyline? = null
    var summary: String? = null
    var warnings: List<Any>? = null
    var waypoint_order: List<Any>? = null
}