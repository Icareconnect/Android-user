package com.consultantapp.data.models.requests

import java.io.Serializable

class SaveAddress : Serializable {
    var address_name: String? = null
    var house_no: String? = null
    var save_as: String? = null
    var location: ArrayList<Double>? = null
    var _id: String? = null
    var addressId: String? = null
    var lat: String? = null
    var long: String? = null
}