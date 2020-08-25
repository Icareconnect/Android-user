package com.consultantapp.di

import com.consultantapp.ConsultantUserApplication
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton


@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    BindingsModule::class,
    ServiceBuilderModule::class,
    NetworkModule::class,
    ViewModelsModule::class
])
@Singleton
interface AppComponent : AndroidInjector<ConsultantUserApplication> {

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<ConsultantUserApplication>()
}