package com.consultantapp.di

import com.consultantapp.pushNotifications.MessagingService
import com.consultantapp.ui.InstallReferrerReceiver
import com.consultantapp.ui.calling.IncomingCallNotificationService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {

    @ContributesAndroidInjector
    abstract fun messagingService(): MessagingService

    @ContributesAndroidInjector
    abstract fun incomingCallNotificationService(): IncomingCallNotificationService

    @ContributesAndroidInjector
    abstract fun installReferrerReceiver(): InstallReferrerReceiver

}