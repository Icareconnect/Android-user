package com.consultantapp.ui.dashboard.home

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

class BannerViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val banners by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val coupons by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val packSub by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val packDetail by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val purchasePack by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun banners() {
        banners.value = Resource.loading()

        webService.banners().enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    banners.value = Resource.success(response.body()?.data)
                } else {
                    banners.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                    response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                banners.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }

    fun coupons(hashMap: HashMap<String,String>) {
        coupons.value = Resource.loading()

        webService.coupons(hashMap).enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    coupons.value = Resource.success(response.body()?.data)
                } else {
                    coupons.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                    response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                coupons.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }

    fun packSub(hashMap: HashMap<String,String>) {
        packSub.value = Resource.loading()

        webService.packSub(hashMap).enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    packSub.value = Resource.success(response.body()?.data)
                } else {
                    packSub.value = Resource.error(
                        ApiUtils.getError(response.code(),
                            response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                packSub.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }


    fun packDetail(hashMap: HashMap<String,String>) {
        packDetail.value = Resource.loading()

        webService.packDetail(hashMap).enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    packDetail.value = Resource.success(response.body()?.data)
                } else {
                    packDetail.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                    response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                packDetail.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }

    fun purchasePack(hashMap: HashMap<String,String>) {
        purchasePack.value = Resource.loading()

        webService.purchasePack(hashMap).enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    purchasePack.value = Resource.success(response.body()?.data)
                } else {
                    purchasePack.value = Resource.error(
                        ApiUtils.getError(response.code(),
                            response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                purchasePack.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }
}