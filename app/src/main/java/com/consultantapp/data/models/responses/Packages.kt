package com.consultantapp.data.models.responses

import java.io.Serializable

class Packages : Serializable{
    var id: String? = null
    var title: String? = null
    var description: String? = null
    var price: String? = null
    var image: String? = null
    var total_requests: String? = null
    var category_id: String? = null
    var subscribe: Boolean? = null
}