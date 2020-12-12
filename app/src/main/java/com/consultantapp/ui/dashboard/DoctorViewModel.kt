package com.consultantapp.ui.dashboard

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

class DoctorViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val doctorList by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val doctorDetails by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val reviewList by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val createRequest by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val confirmRequest by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val services by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val requestCheck by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun doctorList(hashMap: HashMap<String, String>) {

        doctorList.value = Resource.loading()

        webService.doctorList(hashMap).enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    doctorList.value = Resource.success(response.body()?.data)
                } else {
                    doctorList.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                    response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                doctorList.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }

    fun doctorDetails(hashMap: HashMap<String, String>) {
        doctorDetails.value = Resource.loading()

        webService.doctorDetails(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            doctorDetails.value = Resource.success(response.body()?.data)
                        } else {
                            doctorDetails.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        doctorDetails.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun reviewList(hashMap: HashMap<String, String>) {
        reviewList.value = Resource.loading()

        webService.reviewList(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            reviewList.value = Resource.success(response.body()?.data)
                        } else {
                            reviewList.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        reviewList.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun createRequest(hashMap: HashMap<String, Any>) {
        createRequest.value = Resource.loading()

        webService.createRequest(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            createRequest.value = Resource.success(response.body()?.data)
                        } else {
                            createRequest.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        createRequest.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun confirmRequest(hashMap: HashMap<String, Any>) {
        confirmRequest.value = Resource.loading()

        webService.confirmRequest(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            confirmRequest.value = Resource.success(response.body()?.data)
                        } else {
                            confirmRequest.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        confirmRequest.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun services(hashMap: HashMap<String, String>) {
        services.value = Resource.loading()

        webService.services(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            services.value = Resource.success(response.body()?.data)
                        } else {
                            services.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        services.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun requestCheck(hashMap: HashMap<String, String>) {
        requestCheck.value = Resource.loading()

        webService.requestCheck(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            requestCheck.value = Resource.success(response.body()?.data)
                        } else {
                            requestCheck.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        requestCheck.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }
}