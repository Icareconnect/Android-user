package com.consultantapp.data.models.responses.directions

import com.consultantapp.data.models.responses.directions.Distance
import com.consultantapp.data.models.responses.directions.Duration
import com.consultantapp.data.models.responses.directions.End_location
import com.consultantapp.data.models.responses.directions.Polyline

class Step {
    var distance: Distance? = null
    var duration: Duration? = null
    var end_location: End_location? = null
    var html_instructions: String? = null
    var polyline: Polyline? = null
    var start_location: End_location? = null
    var travel_mode: String? = null
    var maneuver: String? = null
}