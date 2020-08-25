package com.consultantapp.data.models.responses

import java.io.Serializable

class Categories : Serializable {
    var id: String? = null
    var name: String? = null
    var image: String? = null
    var parent_id: String? = null
    var created_at: String? = null
    var color_code: String? = null
    var description: String? = null
    var image_icon: String? = null
    var multi_select: String? = null
    //var subcategory: List<Any>? = null
    var is_subcategory: Boolean? = null
    var is_filters: Boolean? = null
}