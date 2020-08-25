package com.consultantapp.ui.dashboard.chat

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

class ChatViewModel @Inject constructor(private val webService: WebService): ViewModel() {

    val chatListing by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val chatMessages by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun getChatListing(hashMap: HashMap<String, String>) {
        chatListing.value = Resource.loading()

        webService.getChatListing(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        chatListing.value = Resource.success(response.body()?.data)
                    } else {
                        chatListing.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    chatListing.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }


    fun getChatMessage(hashmap: HashMap<String, String>) {
        chatMessages.value = Resource.loading()
        webService.getChatMessage(hashmap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {
                    override fun onFailure(
                            call: Call<ApiResponse<CommonDataModel>>,
                            throwable: Throwable) {
                        chatMessages.value = Resource.error(ApiUtils.failure(throwable))
                    }

                    override fun onResponse(
                            call: Call<ApiResponse<CommonDataModel>>,
                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            chatMessages.value = Resource.success(response.body()?.data)
                        } else {
                            chatMessages.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }
                })
    }

}