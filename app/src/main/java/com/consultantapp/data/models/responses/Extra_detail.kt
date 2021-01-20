package com.consultantapp.data.models.responses

import java.io.Serializable

class Extra_detail :Serializable{
    var id: Int? = null
    var request_id: Int? = null
    var first_name: String? = null
    var last_name: String? = null
    var service_for: String? = null
    var home_care_req: String? = null
    var service_address: String? = null
    var lat: String? = null
    var long: String? = null
    var reason_for_service: String? = null
    var created_at: String? = null
    var updated_at: String? = null
    var start_time: String? = null
    var end_time: String? = null
    var working_dates: String? = null
    var filter_id: String? = null
    var filter_name: String? = null
    var distance: String? = null
    var phone_number : String? = null
    var country_code: String? = null
}