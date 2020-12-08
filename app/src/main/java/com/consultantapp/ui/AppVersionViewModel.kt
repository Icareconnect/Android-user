package com.consultantapp.ui

import androidx.lifecycle.ViewModel
import com.consultantapp.data.apis.WebService
import com.consultantapp.data.models.responses.CommonDataModel
import com.consultantapp.data.models.responses.appdetails.AppVersion
import com.consultantapp.data.network.responseUtil.ApiResponse
import com.consultantapp.data.network.responseUtil.ApiUtils
import com.consultantapp.data.network.responseUtil.Resource
import com.consultantapp.di.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AppVersionViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val checkAppVersion by lazy { SingleLiveEvent<Resource<AppVersion>>() }

    val clientDetails by lazy { SingleLiveEvent<Resource<AppVersion>>() }

    val countryCity by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val preferences by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun checkAppVersion(hashMap: HashMap<String, String>) {
        checkAppVersion.value = Resource.loading()

        webService
            .appVersion(hashMap)
            .enqueue(object : Callback<ApiResponse<AppVersion>> {

                override fun onResponse(call: Call<ApiResponse<AppVersion>>,
                                        response: Response<ApiResponse<AppVersion>>) {
                    if (response.isSuccessful) {
                        checkAppVersion.value =Resource.success(response.body()?.data)
                    } else {
                        checkAppVersion.value = Resource.error(
                            ApiUtils.getError(response.code(), response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<AppVersion>>, throwable: Throwable) {
                    checkAppVersion.value = Resource.error(ApiUtils.failure(throwable))
                }
            })
    }

    fun clientDetails(hashMap: HashMap<String, String>) {
        clientDetails.value = Resource.loading()

        webService
                .clientDetails(hashMap)
                .enqueue(object : Callback<ApiResponse<AppVersion>> {

                    override fun onResponse(call: Call<ApiResponse<AppVersion>>,
                                            response: Response<ApiResponse<AppVersion>>) {
                        if (response.isSuccessful) {
                            clientDetails.value =Resource.success(response.body()?.data)
                        } else {
                            clientDetails.value = Resource.error(
                                    ApiUtils.getError(response.code(), response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<AppVersion>>, throwable: Throwable) {
                        clientDetails.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
    }


    fun countryCity(hashMap: HashMap<String, String>) {
        countryCity.value = Resource.loading()

        webService.countryData(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        countryCity.value =Resource.success(response.body()?.data)
                    } else {
                        countryCity.value = Resource.error(
                            ApiUtils.getError(response.code(), response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    countryCity.value = Resource.error(ApiUtils.failure(throwable))
                }
            })
    }

    fun preferences(hashMap: HashMap<String, String>) {
        preferences.value = Resource.loading()

        webService.preferences(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            preferences.value =Resource.success(response.body()?.data)
                        } else {
                            preferences.value = Resource.error(
                                    ApiUtils.getError(response.code(), response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        preferences.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
    }

    fun duty(hashMap: HashMap<String, String>) {
        preferences.value = Resource.loading()

        webService.duty(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            preferences.value =Resource.success(response.body()?.data)
                        } else {
                            preferences.value = Resource.error(
                                    ApiUtils.getError(response.code(), response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        preferences.value = Resource.error(ApiUtils.failure(throwable))
                    }
                })
    }
}