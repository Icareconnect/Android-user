package com.consultantapp.di

import com.consultantapp.data.apis.WebService
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.network.Config
import com.consultantapp.utils.*
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
object NetworkModule {

    @Provides
    @Singleton
    @JvmStatic
    fun okHttpClient(prefsManager: PrefsManager): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .addInterceptor(getHttpLoggingInterceptor())
            .cache(null)
            .addInterceptor(getNetworkInterceptor(prefsManager))
            .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun retrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Config.baseURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    private fun getNetworkInterceptor(prefsManager: PrefsManager): Interceptor {
        return Interceptor { chain ->
            var request = chain.request()

            val user = prefsManager.getObject(USER_DATA, UserData::class.java)

            /*Get App Id*/
            val app_id = if (prefsManager.getString(APP_UNIQUE_ID, "").isNotEmpty())
                prefsManager.getString(APP_UNIQUE_ID, "")
            else
                APP_UNIQUE_ID

            val accessToken = user?.token

            request = if (accessToken?.isEmpty() == true) {
                request.newBuilder().addHeader("Accept", "application/json").build()
            } else {
                val requestBuilder = request.newBuilder()
                requestBuilder.addHeader("Accept", "application/json")
                    .header("Connection", "close")
                    .addHeader("authorization", "Bearer $accessToken")
                    .addHeader("language", prefsManager.getString(USER_LANGUAGE, "en"))
                    .addHeader("timezone", TimeZone.getDefault().id)
                    .addHeader("app-id", app_id)
                    .addHeader("devicetype", ANDROID)

                requestBuilder.build()
            }

            chain.proceed(request)
        }
    }


    private fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return httpLoggingInterceptor
    }

    @Provides
    @Singleton
    @JvmStatic
    fun webService(retrofit: Retrofit): WebService = retrofit.create(WebService::class.java)
}