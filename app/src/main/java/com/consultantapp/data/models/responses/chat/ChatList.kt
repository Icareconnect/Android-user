package com.consultantapp.data.models.responses.chat

import com.consultantapp.data.models.responses.UserData

data class ChatList(
        var image: String? = null,
        val isDelivered: Boolean? = null,
        val from_user: UserData? = null,
        val to_user: UserData? = null,
        var messageType: String? = null,
        val isRead: Boolean? = null,
        val video: Any? = null,
        var id: String? = null,
        var last_message: ChatMessage? = null,
        var unReadCount: Int,
        val chatType: Any? = null,
        val status: String? = null
)
