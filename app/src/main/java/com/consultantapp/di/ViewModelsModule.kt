package com.consultantapp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.consultantapp.ui.AppVersionViewModel
import com.consultantapp.ui.LoginViewModel
import com.consultantapp.ui.calling.CallViewModel
import com.consultantapp.ui.classes.ClassesViewModel
import com.consultantapp.ui.dashboard.DoctorViewModel
import com.consultantapp.ui.dashboard.appointment.AppointmentViewModel
import com.consultantapp.ui.dashboard.appointment.appointmentStatus.DirectionViewModel
import com.consultantapp.ui.dashboard.chat.ChatViewModel
import com.consultantapp.ui.dashboard.chat.UploadFileViewModel
import com.consultantapp.ui.dashboard.doctor.schedule.GetSlotsViewModel
import com.consultantapp.ui.dashboard.home.BannerViewModel
import com.consultantapp.ui.dashboard.home.bookservice.AllocateDoctorViewModel
import com.consultantapp.ui.dashboard.home.bookservice.location.AddressViewModel
import com.consultantapp.ui.drawermenu.wallet.WalletViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class ViewModelsModule {

    @Module
    companion object {

        @Provides
        @Singleton
        @JvmStatic
        fun viewModelProviderFactory(factory: ViewModelProviderFactory): ViewModelProvider.Factory =
                factory
    }

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel::class)
    abstract fun loginViewModel(viewModel: LoginViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DoctorViewModel::class)
    abstract fun doctorViewModel(viewModel: DoctorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(WalletViewModel::class)
    abstract fun walletViewModel(viewModel: WalletViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AppointmentViewModel::class)
    abstract fun appointmentViewModel(viewModel: AppointmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ChatViewModel::class)
    abstract fun chatViewModel(viewModel: ChatViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(UploadFileViewModel::class)
    abstract fun uploadFileViewModel(viewModel: UploadFileViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ClassesViewModel::class)
    abstract fun classesViewModel(viewModel: ClassesViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(GetSlotsViewModel::class)
    abstract fun getSlotsViewModel(viewModel: GetSlotsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BannerViewModel::class)
    abstract fun bannerViewModel(viewModel: BannerViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CallViewModel::class)
    abstract fun callViewModel(viewModel: CallViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AppVersionViewModel::class)
    abstract fun appVersionViewModel(viewModel: AppVersionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AllocateDoctorViewModel::class)
    abstract fun allocateDoctorViewModel(viewModel: AllocateDoctorViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DirectionViewModel::class)
    abstract fun directionViewModel(viewModel: DirectionViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(AddressViewModel::class)
    abstract fun addressViewModel(viewModel: AddressViewModel): ViewModel




}