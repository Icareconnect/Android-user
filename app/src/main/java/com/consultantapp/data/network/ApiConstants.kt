package com.consultantapp.data.network

const val PER_PAGE_LOAD = 20


object ApiKeys {
    /*facbook,google,email,phone*/
    const val PROVIDER_TYPE = "provider_type"

    /*optional only phone and email*/
    const val PROVIDER_ID = "provider_id"

    /*access_token or password or otp*/
    const val PROVIDER_VERIFICATION = "provider_verification"
    const val USER_TYPE = "user_type"

    const val AFTER = "after"
    const val PER_PAGE = "per_page"
}

object ProviderType {
    const val facebook = "facebook"
    const val google = "google"
    const val email = "email"
    const val phone = "phone"
}

object LoadingStatus {
    const val ITEM = 0
    const val LOADING = 1
}


object PushType {
    const val PROFILE_APPROVED = "PROFILE_APPROVED"
    const val CHAT = "chat"
    const val CHAT_STARTED = "CHAT_STARTED"
    const val NEW_REQUEST = "NEW_REQUEST"
    const val REQUEST_COMPLETED = "REQUEST_COMPLETED"
    const val REQUEST_FAILED = "REQUEST_FAILED"
    const val CANCELED_REQUEST = "CANCELED_REQUEST"
    const val RESCHEDULED_REQUEST = "RESCHEDULED_REQUEST"
    const val REQUEST_ACCEPTED = "REQUEST_ACCEPTED"
    const val BOOKING_RESERVED = "BOOKING_RESERVED"
    const val CALL = "CALL"
    const val CALL_RINGING = "CALL_RINGING"
    const val CALL_ACCEPTED = "CALL_ACCEPTED"
    const val CALL_CANCELED = "CALL_CANCELED"
    const val BALANCE_ADDED = "BALANCE_ADDED"
    const val BALANCE_FAILED="BALANCE_FAILED"

    const val START = "START"
    const val REACHED = "REACHED"
    const val START_SERVICE = "START_SERVICE"
    const val CANCEL_SERVICE = "CANCEL_SERVICE"
    const val COMPLETED = "COMPLETED"
}