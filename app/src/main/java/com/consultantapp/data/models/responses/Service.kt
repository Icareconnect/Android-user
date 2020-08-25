package com.consultantapp.data.models.responses

import java.io.Serializable


class Service : Serializable {
    var id: String? = null
    var category_id: Int? = null
    var service_id: String? = null
    var is_active: String? = null
    var price_minimum: Int? = null
    var price_maximum: Int? = null
    var price_fixed: Int? = null
    var minimum_duration: Int? = null
    var gap_duration: Int? = null
    var created_at: String? = null
    var updated_at: String? = null
    var name: String? = null
    var description: String? = null
    var need_availability: String? = null
    var price_type: String? = null

    var isSelected = false
    var price: String? = null
    var unit_price: Int? = null
    var available: String? = null

    var sp_id: Int? = null
    var category_service_id: Int? = null
    var duration: String? = null
    var minimmum_heads_up: String? = null
    var deleted_at: Any? = null
    var category_name: String? = null
    var service_name: String? = null
    var color_code: String? = null
}