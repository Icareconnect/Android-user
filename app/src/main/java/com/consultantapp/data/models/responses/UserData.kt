package com.consultantapp.data.models.responses

import com.consultantapp.data.models.responses.appdetails.Insurance
import java.io.Serializable

class UserData : Serializable {
    var id: String? = null
    var name: String? = null
    var phone: String? = null
    var country_code: String? = null
    var profile_image: String? = null
    var fcm_id: String? = null
    var email: String? = null
    var email_verified_at: Any? = null
    var created_at: String? = null
    var account_verified_at: String? = null
    var account_rejected_at: String? = null
    var updated_at: String? = null
    var token: String? = null
    var provider_type: String? = null
    var profile: Profile? = null
    var services: ArrayList<Service>? = null
    var categoryData: Categories? = null
    //var roles: List<Role>? = null

    var subscriptions: List<Subscription>? = null
    var dob: String? = null
    var qualification: String? = null
    var patientCount: String? = null
    var experience: String? = null
    var speciality: String? = null
    var call_price: String? = null
    var chat_price: String? = null
    var totalRating: String? = null
    var reviewCount: String? = null
    var isAvailable = false

    /*Insurance*/
    var insurance_enable: String? = null
    var insurances: ArrayList<Insurance>? = null
    var custom_fields: ArrayList<Insurance>? = null

    var isApproved: Boolean? = null
    var price: String? = null

    var filters: ArrayList<Filter>? = null

    /*Prefrences*/
    var master_preferences: ArrayList<Filter>? = null
}
