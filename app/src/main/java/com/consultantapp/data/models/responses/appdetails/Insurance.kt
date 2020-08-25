package com.consultantapp.data.models.responses.appdetails

import java.io.Serializable

class Insurance :Serializable{
    var id: String? = null
    var category_id: String? = null
    var name: String? = null
    var company: String? = null
    var enable: String? = null
    var created_at: String? = null
    var updated_at: String? = null

    var isSelected=false


    /*Fields*/
    var field_name: String? = null
    var field_value:String?=null
    var field_type: String? = null
    var required_sign_up: String? = null
}