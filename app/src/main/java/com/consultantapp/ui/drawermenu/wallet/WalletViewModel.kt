package com.consultantapp.ui.drawermenu.wallet

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

class WalletViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val walletHistory by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val wallet by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val addCard by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val updateCard by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val deleteCard by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val orderCreate by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val addMoney by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val cardListing by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun walletHistory(hashMap: HashMap<String, String>) {
        walletHistory.value = Resource.loading()

        webService.walletHistory(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            walletHistory.value = Resource.success(response.body()?.data)
                        } else {
                            walletHistory.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        walletHistory.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun razorPayCreateOrder(hashMap: HashMap<String, String>) {
        orderCreate.value = Resource.loading()

        webService.orderCreate(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        orderCreate.value = Resource.success(response.body()?.data)
                    } else {
                        orderCreate.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    orderCreate.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }

    fun wallet(hashMap: HashMap<String, String>) {
        wallet.value = Resource.loading()

        webService.wallet(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        wallet.value = Resource.success(response.body()?.data)
                    } else {
                        wallet.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    wallet.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }

    fun addCard(hashMap: HashMap<String, Any>) {
        addCard.value = Resource.loading()

        webService.addCard(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        addCard.value = Resource.success(response.body()?.data)
                    } else {
                        addCard.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    addCard.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }

    fun updateCard(hashMap: HashMap<String, Any>) {
        updateCard.value = Resource.loading()

        webService.updateCard(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            updateCard.value = Resource.success(response.body()?.data)
                        } else {
                            updateCard.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        updateCard.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun deleteCard(hashMap: HashMap<String, Any>) {
        deleteCard.value = Resource.loading()

        webService.deleteCard(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            deleteCard.value = Resource.success(response.body()?.data)
                        } else {
                            deleteCard.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        deleteCard.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun addMoney(hashMap: HashMap<String, Any>) {
        addMoney.value = Resource.loading()

        webService.addMoney(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        addMoney.value = Resource.success(response.body()?.data)
                    } else {
                        addMoney.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    addMoney.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }

    fun cardListing(hashMap: HashMap<String, String>) {
        cardListing.value = Resource.loading()

        webService.cardListing(hashMap)
            .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                        response: Response<ApiResponse<CommonDataModel>>) {
                    if (response.isSuccessful) {
                        cardListing.value = Resource.success(response.body()?.data)
                    } else {
                        cardListing.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                    cardListing.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }
}