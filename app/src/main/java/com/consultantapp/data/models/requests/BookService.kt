package com.consultantapp.data.models.requests

import java.io.Serializable

class BookService : Serializable {
    var filter_id: String? = null

    var address: SaveAddress? = null
    var date: Long? = null
    var startTime: String? = null
    var endTime: String? = null
    var reason: String? = null
    var service_for: String? = null
    var home_care_req: String? = null

    var personName = ""
}