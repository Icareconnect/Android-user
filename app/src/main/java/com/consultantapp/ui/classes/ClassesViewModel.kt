package com.consultantapp.ui.classes

import androidx.lifecycle.ViewModel
import com.consultantapp.data.apis.WebService
import com.consultantapp.data.models.responses.ClassData
import com.consultantapp.data.models.responses.CommonDataModel
import com.consultantapp.data.network.responseUtil.ApiResponse
import com.consultantapp.data.network.responseUtil.ApiUtils
import com.consultantapp.data.network.responseUtil.Resource
import com.consultantapp.di.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class ClassesViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val enrollUser by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val joinClass by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val categories by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val classes by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val classDetail by lazy { SingleLiveEvent<Resource<ClassData>>() }

    val getFilters by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }


    fun enrollUser(hashMap: HashMap<String, String>) {
        enrollUser.value = Resource.loading()

        webService.enrollUser(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            enrollUser.value = Resource.success(response.body()?.data)
                        } else {
                            enrollUser.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        enrollUser.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun joinClass(hashMap: HashMap<String, String>) {
        joinClass.value = Resource.loading()

        webService.joinClass(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            joinClass.value = Resource.success(response.body()?.data)
                        } else {
                            joinClass.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        joinClass.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun categories(hashMap: HashMap<String, String>) {
        categories.value = Resource.loading()

        webService.categories(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            categories.value = Resource.success(response.body()?.data)
                        } else {
                            categories.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        categories.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun classesList(hashMap: HashMap<String, String>) {
        classes.value = Resource.loading()

        webService.classesList(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            classes.value = Resource.success(response.body()?.data)
                        } else {
                            classes.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        classes.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun classDetail(hashMap: HashMap<String, String>) {
        classDetail.value = Resource.loading()

        webService.classDetail(hashMap)
                .enqueue(object : Callback<ApiResponse<ClassData>> {

                    override fun onResponse(call: Call<ApiResponse<ClassData>>,
                                            response: Response<ApiResponse<ClassData>>) {
                        if (response.isSuccessful) {
                            classDetail.value = Resource.success(response.body()?.data)
                        } else {
                            classDetail.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<ClassData>>, throwable: Throwable) {
                        classDetail.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun getFilters(hashMap: HashMap<String, String>) {
        getFilters.value = Resource.loading()

        webService.getFilters(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            getFilters.value = Resource.success(response.body()?.data)
                        } else {
                            getFilters.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        getFilters.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }
}