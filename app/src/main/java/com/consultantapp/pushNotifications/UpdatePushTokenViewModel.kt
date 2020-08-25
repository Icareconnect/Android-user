package com.consultantapp.pushNotifications

import android.service.autofill.UserData
import androidx.lifecycle.ViewModel
import com.consultantapp.data.apis.WebService
import com.consultantapp.data.network.responseUtil.Resource
import com.consultantapp.di.SingleLiveEvent
import javax.inject.Inject

class UpdatePushTokenViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val updatePushToken by lazy { SingleLiveEvent<Resource<UserData>>() }

    fun updatePushToken(hashMap: HashMap<String,Any>) {
        updatePushToken.value = Resource.loading()

       /* webService.numberLogin(hashMap)
            .enqueue(object : Callback<ApiResponse<UserData>> {

                override fun onResponse(
                    call: Call<ApiResponse<UserData>>,
                    response: Response<ApiResponse<UserData>>) {
                    if (response.isSuccessful) {
                        updatePushToken.value = Resource.success(response.body()?.data)
                    } else {
                        updatePushToken.value = Resource.error(
                            ApiUtils.getError(response.code(),
                            response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                    updatePushToken.value = Resource.error(ApiUtils.failure(throwable))
                }
            })*/
    }
}