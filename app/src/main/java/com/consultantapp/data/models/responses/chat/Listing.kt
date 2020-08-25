package com.consultantapp.data.models.responses.chat

data class Listing<out T>(
        val count: Int? = null,
        val isOnline: Boolean? = null,
        val listing: T? = null
)