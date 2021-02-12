package com.consultantapp.data.models.responses

import java.io.Serializable

class Request :Serializable{

    var id: String? = null
    var booking_date: String? = null
    var from_user: UserData? = null
    var canceled_by: UserData? = null
    var to_user: UserData? = null
    var time: String? = null
    var service_type: String? = null
    var schedule_type:String?=null
    var service_id: String? = null
    var price: String? = null
    var status: String? = null
    var created_at: String? = null
    var bookingDateUTC: String? = null
    var rating: String? = null
    var comment:String?=null
    var userIsApproved: Boolean? = null
    var user_status: String? = null
    var user_comment: String? = null
    var canReschedule = false
    var canCancel = false
    var total_hours :Double ?=null

    var extra_detail: Extra_detail? = null
    var last_location:Last_location?=null
    var duties:ArrayList<FilterOption>?=null
}