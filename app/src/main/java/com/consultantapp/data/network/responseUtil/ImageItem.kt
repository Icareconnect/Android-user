package com.consultantapp.data.network.responseUtil

internal class ImageItem {
    var thumbnail: String? = null
    var original: String? = null
    var name: String? = null
    var id: String? = null
    var type: String? = null

    override fun toString(): String {
        return "ImageItem{" +
                "thumbnail = '" + thumbnail + '\''.toString() +
                ",original = '" + original + '\''.toString() +
                ",name = '" + name + '\''.toString() +
                ",_id = '" + id + '\''.toString() +
                ",type = '" + type + '\''.toString() +
                "}"
    }
}
