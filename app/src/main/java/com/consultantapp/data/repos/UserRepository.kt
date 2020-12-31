package com.consultantapp.data.repos

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.consultantapp.ConsultantUserApplication
import com.consultantapp.data.apis.WebService
import com.consultantapp.data.models.PushData
import com.consultantapp.data.models.responses.CommonDataModel
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.models.responses.appdetails.AppVersion
import com.consultantapp.data.network.responseUtil.ApiResponse
import com.consultantapp.utils.*
import com.google.firebase.iid.FirebaseInstanceId
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class UserRepository @Inject constructor(
    private val app: ConsultantUserApplication,
    private val prefsManager: PrefsManager, private val webService: WebService
) {

    val groupCreatedCall = MutableLiveData<String>()
    val loginGuestUser = MutableLiveData<String>()
    val groupExitResponse = MutableLiveData<Pair<Boolean, String>>()
    val pushData = MutableLiveData<PushData>()
    val isNewNotification = MutableLiveData<Boolean>()


    fun isUserLoggedIn(): Boolean {
        val user = prefsManager.getObject(USER_DATA, UserData::class.java)

        return if (user?.id.isNullOrEmpty() || user?.name.isNullOrEmpty())
            false
        else if (user?.master_preferences.isNullOrEmpty())
            false
        else
            true
    }


    fun getUser(): UserData? {
        return prefsManager.getObject(USER_DATA, UserData::class.java)
    }

    fun getAppSetting(): AppVersion {
        return prefsManager.getObject(APP_DETAILS, AppVersion::class.java) ?:AppVersion()
    }

    fun getUserLanguage(): String {
        return prefsManager.getString(USER_LANGUAGE, "en")
    }

    fun getPushCallData(): PushData? {
        return prefsManager.getObject(PUSH_DATA, PushData::class.java)
    }

    fun pushTokenUpdate() {
        if (isUserLoggedIn()) {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                val hashMap = HashMap<String, Any>()
                hashMap["fcm_id"] = it.token

                webService.updateFcmId(hashMap)
                    .enqueue(object : Callback<ApiResponse<UserData>> {

                        override fun onResponse(
                            call: Call<ApiResponse<UserData>>,
                            response: Response<ApiResponse<UserData>>
                        ) {
                            if (response.isSuccessful) {
                                Log.e("fcmToken", "Success")
                            } else {
                                Log.e("fcmToken", "Faliure")
                            }
                        }

                        override fun onFailure(
                            call: Call<ApiResponse<UserData>>,
                            throwable: Throwable
                        ) {
                            Log.e("fcmToken", "faliue 500")
                        }
                    })

                Log.d("FCMToken", it.token)
            }
        }
    }

    fun callStatus(requestId: String, call_id: String, callStatus: String) {
        val hashMap = HashMap<String, String>()
        hashMap["request_id"] = requestId
        hashMap["call_id"] = call_id
        hashMap["status"] = callStatus

        webService.callStatus(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(
                    call: Call<ApiResponse<CommonDataModel>>,
                    response: Response<ApiResponse<CommonDataModel>>
                ) {
                    if (response.isSuccessful) {
                        Log.e("fcmToken", "Success")
                    } else {
                        Log.e("fcmToken", "Faliure")
                    }
                }

                override fun onFailure(
                    call: Call<ApiResponse<CommonDataModel>>,
                    throwable: Throwable
                ) {
                    Log.e("fcmToken", "faliue 500")
                }
            })
    }

}

