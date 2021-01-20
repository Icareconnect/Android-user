package com.consultantapp.data.models.requests

import java.io.Serializable

class BookService : Serializable {
    var filter_id: String? = null

    var address: SaveAddress? = null
    var date: String? = null
    var startTime: String? = null
    var endTime: String? = null
    var reason: String? = null
    var service_for: String? = null
    var service_type: String? = null

    var personName = ""
    var phone_number : String? = null
    var country_code: String? = null
}