package com.consultantapp.data.models.requests

import java.io.Serializable

class SaveAddress : Serializable {
    var locationName: String? = null
    var houseNumber: String? = null
    var location: ArrayList<Double>? = null
    var isDefault = false
    var _id: String? = null
    var addressId: String? = null
}