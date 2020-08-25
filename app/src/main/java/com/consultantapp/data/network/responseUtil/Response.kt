package com.consultantapp.data.network.responseUtil

internal class Response {
    var msg: String? = null
    var data: Data? = null
    var statusCode: Int = 0

    override fun toString(): String {
        return "Response{" +
                "msg = '" + msg + '\''.toString() +
                ",data = '" + data + '\''.toString() +
                ",statusCode = '" + statusCode + '\''.toString() +
                "}"
    }
}
