package com.consultantapp.data.models.responses

class Banner {
    var id: Int? = null
    var image_web: Any? = null
    var image_mobile: String? = null
    var position: String? = null
    var banner_type: String? = null
    var start_date: String? = null
    var end_date: String? = null
    var category_id: String? = null
    var category: Categories? = null
    var sp_id: String? = null
    var class_id: String? = null
    var created_at: String? = null
    var updated_at: String? = null

    /*Coupon*/
    var service_id: String? = null
    var service :Service?=null
    var minimum_value: String? = null
    var limit: Int? = null
    var coupon_code: String? = null
    var maximum_discount_amount: String? = null
    var discount_type: String? = null
    var discount_value: String? = null
}