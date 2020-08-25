package com.consultantapp.data.models.responses

import java.io.Serializable

class JitsiClass : Serializable {

    var id: String? = null
    var call_id: String? = null
    var name: String? = null
    var callType: String? = null
    var isClass = false
}