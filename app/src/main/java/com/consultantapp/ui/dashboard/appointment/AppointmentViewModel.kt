package com.consultantapp.ui.dashboard.appointment

import androidx.lifecycle.ViewModel
import com.consultantapp.data.apis.WebService
import com.consultantapp.data.models.responses.CommonDataModel
import com.consultantapp.data.network.responseUtil.ApiResponse
import com.consultantapp.data.network.responseUtil.ApiUtils
import com.consultantapp.data.network.responseUtil.Resource
import com.consultantapp.di.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AppointmentViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val request by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val requestDetail by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val cancelRequest by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val addReview by lazy { SingleLiveEvent<Resource<Any>>() }

    val approveWorkingHour by lazy { SingleLiveEvent<Resource<Any>>() }

    val completeChat by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val notifications by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }


    fun request(hashMap: HashMap<String, String>) {
        request.value = Resource.loading()

        webService.request(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            request.value = Resource.success(response.body()?.data)
                        } else {
                            request.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        request.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun requestDetail(hashMap: HashMap<String, String>) {
        requestDetail.value = Resource.loading()

        webService.requestDetail(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            requestDetail.value = Resource.success(response.body()?.data)
                        } else {
                            requestDetail.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        requestDetail.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun cancelRequest(hashMap: HashMap<String, String>) {
        cancelRequest.value = Resource.loading()

        webService.cancelRequest(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        cancelRequest.value = Resource.success(response.body()?.data)
                    } else {
                        cancelRequest.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    cancelRequest.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }

    fun addReview(hashMap: HashMap<String, String>) {
        addReview.value = Resource.loading()

        webService.addReview(hashMap)
                .enqueue(object : Callback<ApiResponse<Any>> {

                    override fun onResponse(call: Call<ApiResponse<Any>>,
                                            response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            addReview.value = Resource.success(response.body()?.data)
                        } else {
                            addReview.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, throwable: Throwable) {
                        addReview.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun approveWorkingHour(hashMap: HashMap<String, String>) {
        approveWorkingHour.value = Resource.loading()

        webService.approveWorkingHour(hashMap)
                .enqueue(object : Callback<ApiResponse<Any>> {

                    override fun onResponse(call: Call<ApiResponse<Any>>,
                                            response: Response<ApiResponse<Any>>) {
                        if (response.isSuccessful) {
                            approveWorkingHour.value = Resource.success(response.body()?.data)
                        } else {
                            approveWorkingHour.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<Any>>, throwable: Throwable) {
                        approveWorkingHour.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun completeChat(hashMap: HashMap<String, Any>) {
        completeChat.value = Resource.loading()

        webService.completeChat(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            completeChat.value = Resource.success(response.body()?.data)
                        } else {
                            completeChat.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        completeChat.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun notifications(hashMap: HashMap<String, String>) {
        notifications.value = Resource.loading()

        webService.notifications(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            notifications.value = Resource.success(response.body()?.data)
                        } else {
                            notifications.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        notifications.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }
}