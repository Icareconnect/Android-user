package com.consultantapp.ui.dashboard.home.bookservice

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

class AllocateDoctorViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val confirmAutoAllocate by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val autoAllocate by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }


    fun confirmAutoAllocate(hashMap: HashMap<String,String>) {
        confirmAutoAllocate.value = Resource.loading()

        webService.confirmAutoAllocate(hashMap).enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    confirmAutoAllocate.value = Resource.success(response.body()?.data)
                } else {
                    confirmAutoAllocate.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                    response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                confirmAutoAllocate.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }

    fun autoAllocate(hashMap: HashMap<String,String>) {
        autoAllocate.value = Resource.loading()

        webService.autoAllocate(hashMap).enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    autoAllocate.value = Resource.success(response.body()?.data)
                } else {
                    autoAllocate.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                    response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                autoAllocate.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }
}