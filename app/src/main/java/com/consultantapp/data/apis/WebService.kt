package com.consultantapp.data.apis

import com.consultantapp.data.models.responses.ClassData
import com.consultantapp.data.models.responses.CommonDataModel
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.models.responses.appdetails.AppVersion
import com.consultantapp.data.models.responses.directions.Direction
import com.consultantapp.data.network.responseUtil.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface WebService {
    companion object {

        private const val LOGIN = "/api/login"
        private const val APP_VERSION = "/api/appversion"
        private const val CLIENT_DETAILS = "/api/clientdetail"
        private const val COUNTRY_DATA = "/api/countrydata"
        private const val PREFERENCES = "/api/master/preferences"
        private const val DUTY = "/api/master/duty"
        private const val UPDATE_NUMBER = "/api/update-phone"
        private const val VERIFY_OTP = "/api/verify-otp"
        private const val RESEND_OTP = "api/resend-otp"
        private const val REGISTER = "/api/register"
        private const val FORGOT_PASSWORD = "/api/forgot_password"
        private const val CHANGE_PASSWORD = "/api/password-change"
        private const val PROFILE_UPDATE = "/api/profile-update"
        private const val LOGOUT = "/api/app_logout"
        private const val SEND_SMS = "/api/send-sms"
        private const val UPDATE_FCM_ID = "/api/update-fcm-id"
        private const val CREATE_REQUEST = "/api/create-request"
        private const val CONFIRM_REQUEST = "/api/confirm-request"
        private const val ADD_CARD = "api/add-card"
        private const val UPDATE_CARD = "/api/update-card"
        private const val DELETE_CARD = "/api/delete-card"
        private const val ADD_MONEY = "/api/add-money"
        private const val ADD_REVIEW = "/api/add-review"
        private const val REQUEST_USER_APPROVE = "/api/request-user-approve"
        private const val COMPLETE_CHAT = "/api/complete-chat"
        private const val UPLOAD_IMAGE = "/api/upload-image"

        private const val REQUESTS = "/api/requests-cs"
        private const val REQUEST_DETAIL="/api/request-detail"
        private const val CANCEL_REQUEST = "/api/cancel-request"
        private const val DOCTOR_LIST = "/api/doctor-list"
        private const val BANNERS = "/api/banners"
        private const val COUPONS = "/api/coupons"
        private const val DOCTOR_DETAIL = "/api/doctor-detail"
        private const val REVIEW_LIST = "/api/review-list"
        private const val WALLET_HISTORY = "/api/wallet-history"
        private const val CARD_LISTING = "/api/cards"
        private const val WALLET = "/api/wallet"
        private const val CHAT_LISTING = "/api/chat-listing"
        private const val CHAT_MESSAGES = "/api/chat-messages"
        private const val NOTIFICATIONS = "/api/notifications"
        private const val CATEGORIES = "/api/categories"
        private const val CLASSES = "/api/classes"
        private const val CLASS_DETAIL = "/api/class/detail"
        private const val ENROLL_USER = "/api/enroll-user"
        private const val CLASS_JOIN = "/api/class/join"
        private const val ORDER_CREATE = "/api/order/create"
        private const val SERVICES = "/api/services"
        private const val GET_FILTERS = "/api/get-filters"
        private const val GET_SLOTS = "/api/get-slots"
        private const val CALL_STATUS = "/api/call-status"
        private const val PROFILE = "/api/profile"
        private const val PAGES = "/api/pages"
        private const val PACK_SUB = "/api/pack-sub"
        private const val PURCHASE_PACK = "/api/sub-pack"
        private const val PACK_DETAIL = "/api/pack-detail"
        private const val CONFIRM_AUTO_ALLOCATE = "/api/confirm-auto-allocate"
        private const val AUTO_ALLOCATE = "/api/auto-allocate"
        private const val DIRECTIONS="https://maps.googleapis.com/maps/api/directions/json"

        private const val WORKING_HOURS = "/api/workingHours"
        private const val SPEAKOUT_LIST = "/common/listSpeakouts"

    }

    /*POST APIS*/
    @FormUrlEncoded
    @POST(LOGIN)
    fun login(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(APP_VERSION)
    fun appVersion(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<AppVersion>>

    @FormUrlEncoded
    @POST(UPDATE_NUMBER)
    fun updateNumber(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(VERIFY_OTP)
    fun verifyOtp(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(RESEND_OTP)
    fun resendOtp(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @Multipart
    @POST(REGISTER)
    fun register(@PartMap map: HashMap<String, RequestBody>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(FORGOT_PASSWORD)
    fun forgotPassword(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(CHANGE_PASSWORD)
    fun changePassword(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>


    @Multipart
    @POST(PROFILE_UPDATE)
    fun updateProfile(@PartMap map: HashMap<String, RequestBody>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(COMPLETE_CHAT)
    fun completeChat(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(SEND_SMS)
    fun sendSMS(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @POST(LOGOUT)
    fun logout(): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(UPDATE_FCM_ID)
    fun updateFcmId(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<UserData>>

    @FormUrlEncoded
    @POST(CREATE_REQUEST)
    fun createRequest(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(CONFIRM_REQUEST)
    fun confirmRequest(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ADD_CARD)
    fun addCard(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(UPDATE_CARD)
    fun updateCard(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(DELETE_CARD)
    fun deleteCard(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ADD_MONEY)
    fun addMoney(@FieldMap hashMap: HashMap<String, Any>): Call<ApiResponse<CommonDataModel>>

    @Multipart
    @POST(UPLOAD_IMAGE)
    fun uploadFile(
            @Part("type") typeRequest: RequestBody,
            @Part file: Array<MultipartBody.Part?>
    ): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ADD_REVIEW)
    fun addReview(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<Any>>

    @FormUrlEncoded
    @POST(REQUEST_USER_APPROVE)
    fun approveWorkingHour(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<Any>>


    @FormUrlEncoded
    @POST(ENROLL_USER)
    fun enrollUser(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(CLASS_JOIN)
    fun joinClass(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(ORDER_CREATE)
    fun orderCreate(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(CANCEL_REQUEST)
    fun cancelRequest(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(CALL_STATUS)
    fun callStatus(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>


    @GET(PACK_DETAIL)
    fun packDetail(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(PURCHASE_PACK)
    fun purchasePack(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(CONFIRM_AUTO_ALLOCATE)
    fun confirmAutoAllocate(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>

    @FormUrlEncoded
    @POST(AUTO_ALLOCATE)
    fun autoAllocate(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<CommonDataModel>>


    /*GET*/

    @GET(PROFILE)
    fun profile(): Call<ApiResponse<UserData>>

    @GET(CLIENT_DETAILS)
    fun clientDetails(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<AppVersion>>

    @GET(COUNTRY_DATA)
    fun countryData(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(PREFERENCES)
    fun preferences(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(DUTY)
    fun duty(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(REQUESTS)
    fun request(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(REQUEST_DETAIL)
    fun requestDetail(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(DOCTOR_LIST)
    fun doctorList(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(BANNERS)
    fun banners(): Call<ApiResponse<CommonDataModel>>

    @GET(DOCTOR_DETAIL)
    fun doctorDetails(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(REVIEW_LIST)
    fun reviewList(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(WALLET_HISTORY)
    fun walletHistory(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(CARD_LISTING)
    fun cardListing(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(WALLET)
    fun wallet(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(CHAT_LISTING)
    fun getChatListing(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(CHAT_MESSAGES)
    fun getChatMessage(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(NOTIFICATIONS)
    fun notifications(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(CATEGORIES)
    fun categories(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(CLASSES)
    fun classesList(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(CLASS_DETAIL)
    fun classDetail(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<ClassData>>

    @GET(GET_FILTERS)
    fun getFilters(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>


    @GET(SERVICES)
    fun services(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(GET_SLOTS)
    fun getSlots(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(PAGES)
    fun getPages(): Call<ApiResponse<CommonDataModel>>

    @GET(COUPONS)
    fun coupons(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(PACK_SUB)
    fun packSub(@QueryMap hashMap: Map<String, String>): Call<ApiResponse<CommonDataModel>>

    @GET(DIRECTIONS)
    fun directions(@QueryMap hashMap: Map<String, String>): Call<Direction>


    /*PUT API*/
    @FormUrlEncoded
    @PUT(WORKING_HOURS)
    fun workingHours(@FieldMap hashMap: HashMap<String, String>): Call<ApiResponse<Any>>

}