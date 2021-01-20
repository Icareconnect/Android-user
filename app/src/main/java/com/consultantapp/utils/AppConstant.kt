package com.consultantapp.utils

/*Links*/

const val PLAY_STORE = "https://play.google.com/store/apps/details?id="
const val SOCKET_URL = "https://node.royoconsult.com/"
const val APP_UNIQUE_ID = "fc3eda974104a85c07d59108190ad6056" /*a59ef14422c898df221e0f4da0ed85611*/

const val STORAGE_DIRECTORY = "/Callouts/"
const val FILE_PATH_DIRECTORY = "file://"

const val CATEGORY_ID = "1"
const val CATEGORY_SERVICE_ID = "1"

const val ANDROID = "ANDROID"
const val APP_TYPE = "customer"

const val USER_DATA = "user data"
const val APP_DETAILS = "APP_DETAILS"
const val USER_LANGUAGE = "user language"
const val COUNTRY_CODE = "COUNTRY_CODE"
const val PHONE_NUMBER = "PHONE_NUMBER"
const val UPDATE_NUMBER = "UPDATE_NUMBER"
const val UPDATE_PROFILE = "UPDATE_PROFILE"
const val USER_ADDRESS = "user address"

const val PUSH_DATA = "PUSH_DATA"
const val PAGE_TO_OPEN = "PAGE_TO_OPEN"

const val LAST_MESSAGE = "last message"
const val USER_ID = "user id"
const val USER_NAME = "user name"
const val OTHER_USER_ID = "other user id"
const val UPDATE_CHAT = "updateChat"
const val EXTRA_REQUEST_ID = "EXTRA_REQUEST_ID"
const val EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID"
const val EXTRA_CALL_NAME = "extra call name"
const val EXTRA_NAME = "extra name"
const val EXTRA_PRICE = "extra price"
const val EXTRA_TAB = "extra tab"


object CallType {
    const val CALL = "call"
    const val CHAT = "chat"
    const val ALL = "all"
}

object ClassType {
    const val ADDED = "added"
    const val STARTED = "started"
    const val COMPLETED = "completed"
}

object PaymentFrom {
    const val STRIPE = "stripe"
    const val RAZOR_PAY = "razor pay"
    const val CCA_VENUE = "cca venue"
}

object RequestType {
    const val INSTANT = "instant"
    const val SCHEDULE = "schedule"
}

object CallFrom {
    const val CALL = "call"
    const val VIDEO_CALL = "video call"
}

object CallAction {
    const val PENDING = "pending"
    const val ACCEPT = "accept"
    const val REJECT = "reject"
    const val COMPLETED = "completed"
    const val FAILED = "failed"
    const val CANCELED = "canceled"

    const val START = "start"
    const val REACHED = "reached"
    const val START_SERVICE = "start_service"
    const val CANCEL_SERVICE = "cancel_service"

    const val DECLINED = "declined"
    const val APPROVED = "approved"
}

object WalletMoney {
    const val DEPOSIT = "deposit"
    const val WITHDRAWAL = "withdrawal"
    const val REFUND = "refund"
    const val FAILED = "failed"
    const val ADD_PACKAGE = "add_package"
}

object AppRequestCode {
    const val AUTOCOMPLETE_REQUEST_CODE: Int = 100
    const val IMAGE_PICKER: Int = 101
    const val ADD_MONEY: Int = 102
    const val PROFILE_UPDATE: Int = 103
    const val PACKAGE_UPDATE: Int = 104
    const val LOCATION_PERMISSION_ID = 105
    const val REQ_CHAT = 106
    const val ASK_FOR_LOCATION: Int = 107
    const val ADD_FILTER: Int = 108
    const val NEW_APPOINTMENT: Int = 109
    const val ADD_CARD: Int = 110
    const val APPOINTMENT_BOOKING: Int = 111
    const val APPOINTMENT_DETAILS: Int = 112
}

object DocType {
    const val TEXT = "TEXT"
    const val IMAGE = "IMAGE"
    const val PDF = "PDF"
    const val AUDIO = "AUDIO"
    const val MESSAGE_TYPING = "TYPING"
}

object ImageFolder{
    const val UPLOADS = "uploads/"
    const val THUMBS = "thumbs/"
    const val PDF = "pdf/"
}

object MediaUploadStatus {
    const val NOT_UPLOADED = "not_uploaded"
    const val UPLOADING = "uploading"
    const val CANCELED = "canceled"
    const val UPLOADED = "unloaded"

}

object DeepLink {
    const val USER_PROFILE = "userProfile"
    const val INVITE = "Invite"
}

object PageLink {
    const val TERMS_CONDITIONS = "terms-conditions"
    const val PRIVACY_POLICY = "privacy-policy"
    const val CONTACT_US = "contact-us"
}

object CustomFields {
    const val ZIP_CODE = "Zip Code"
    const val QUALIFICATION = "Qualification"
    const val WORKING_SHIFTS = "Working shifts"
    const val WORK_EXPERIENCE = "Work experience"
    const val PROFESSIONAL_LISCENCE = "Professional Liscence"
    const val CERTIFICATION = "Certification"
    const val START_DATE = "Start Date"
}

object ClientFeatures {
    const val ADDRESS = "Address Required"
}

object CountryListType {
    const val COUNTRY = "country"
    const val STATE = "state"
    const val CITY = "city"
}

object PreferencesType {
    const val ALL = "All"
    const val COVID = "covid"
    const val PERSONAL_INTEREST = "personal_interest"
    const val WORK_ENVIRONMENT = "work_environment"
    const val PROVIDABLE_SERVICES = "duty"
}