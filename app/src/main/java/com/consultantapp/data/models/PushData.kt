package com.consultantapp.data.models

import java.io.Serializable

data class PushData(
        val msg: String,
        var title: String,
        val sound: String,
        val pushType: String,
        val imageUrl: String,
        val message: String,
        val senderId: String,
        val senderName: String,
        val receiverId: String,
        val messageType: String,
        val request_id: String,
        val call_id: String,
        val service_type:String,
        val sentAt: Long,
        val request_time: String,
        val sender_name: String,
        val sender_image: String,
        val vendor_category_name: String,
        val transaction_id: String
) : Serializable