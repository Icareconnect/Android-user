package com.consultantapp.ui.calling

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

class CallViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val callStatus by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun callStatus(hashMap: HashMap<String, String>) {
        callStatus.value = Resource.loading()

        webService.callStatus(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            callStatus.value = Resource.success(response.body()?.data)
                        } else {
                            callStatus.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        callStatus.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

}