package com.consultantapp.data.models.responses

import java.io.Serializable

class Subscription :Serializable{

    var service_id: Int? = null
    var charges: String? = null
    var duration: Int? = null
    var type: String? = null

}