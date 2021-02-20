package com.consultantapp.data.models.responses

class Notification {

    var id: String? = null
    var pushType: String? = null
    var message: String? = null
    var module: String? = null
    var module_id: String? = null
    var created_at: String? = null
    var read_status: String? = null
    var form_user:UserData?=null
    var to_user:UserData?=null
}