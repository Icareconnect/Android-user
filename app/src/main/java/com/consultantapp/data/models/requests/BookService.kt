package com.consultantapp.data.models.requests

import java.io.Serializable

class BookService : Serializable {
    var address: SaveAddress? = null
    var date: Long? = null
    var startTime: String? = null
    var endTime: String? = null
    var reason: String? = null
}