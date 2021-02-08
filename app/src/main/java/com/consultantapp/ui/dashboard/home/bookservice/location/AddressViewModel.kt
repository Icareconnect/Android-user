package com.consultantapp.ui.dashboard.home.bookservice.location

import androidx.lifecycle.ViewModel
import com.consultantapp.data.apis.WebService
import com.consultantapp.data.models.requests.SaveAddress
import com.consultantapp.data.models.responses.CommonDataModel
import com.consultantapp.data.network.responseUtil.ApiResponse
import com.consultantapp.data.network.responseUtil.ApiUtils
import com.consultantapp.data.network.responseUtil.Resource
import com.consultantapp.di.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class AddressViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val saveAddress by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val getAddress by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }


    fun saveAddress(saveAddressModel: SaveAddress) {
        saveAddress.value = Resource.loading()

        webService.saveAddress(saveAddressModel).enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    saveAddress.value = Resource.success(response.body()?.data)
                } else {
                    saveAddress.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                    response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                saveAddress.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }

    fun getAddress() {
        getAddress.value = Resource.loading()

        webService.getAddress().enqueue(object : Callback<ApiResponse<CommonDataModel>> {

            override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                    response: Response<ApiResponse<CommonDataModel>>) {
                if (response.isSuccessful) {
                    getAddress.value = Resource.success(response.body()?.data)
                } else {
                    getAddress.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                    response.errorBody()?.string()))
                }
            }

            override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                getAddress.value = Resource.error(ApiUtils.failure(throwable))
            }

        })
    }
}