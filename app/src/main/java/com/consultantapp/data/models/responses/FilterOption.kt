package com.consultantapp.data.models.responses

import java.io.Serializable

class FilterOption : Serializable {
    var id: String? = null
    var option_name: String? = null
    var filter_type_id: Int? = null

    var isSelected = false
}