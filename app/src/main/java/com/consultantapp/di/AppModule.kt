package com.consultantapp.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.consultantapp.ConsultantUserApplication
import com.consultantapp.pushNotifications.MessagingService
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
object AppModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideContext(app: ConsultantUserApplication): Context = app.applicationContext

    @Provides
    @Singleton
    @JvmStatic
    fun provideMessagingService(messagingService: MessagingService): MessagingService = MessagingService()

    @Provides
    @Singleton
    @JvmStatic
    fun sharedPreferences(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    @JvmStatic
    fun provideGson(): Gson {
        return GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .setPrettyPrinting()
                .setLenient()
                .create()
    }
}
