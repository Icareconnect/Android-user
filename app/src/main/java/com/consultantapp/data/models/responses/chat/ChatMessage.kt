package com.consultantapp.data.models.responses.chat

import com.consultantapp.utils.MediaUploadStatus

data class ChatMessage(
    var imageUrl: String? = null,
    var conversationId: String? = null,
    //val deleteByList: List<Any>? = null,
    var message: String? = null,
    var sentAt: Long? = null,
    var messageType: String? = null,

    var status: String? = null,
    var isActive: Boolean? = null,
    var senderId: String? = null,
    var senderName: String? = null,
    var receiverId: String? = null,
    var createAt: String? = null,
    var __v: Int? = null,
    var mediaUploadStatus: String = MediaUploadStatus.UPLOADED,
    var id: String? = null,
    var messageId: String? = null,
    var request_id: String? = null

)
