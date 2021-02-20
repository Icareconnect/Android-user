package com.consultantapp.di


import com.consultantapp.ui.SplashActivity
import com.consultantapp.ui.calling.CallingActivity
import com.consultantapp.ui.classes.CategoriesFragment
import com.consultantapp.ui.classes.ClassesDetailFragment
import com.consultantapp.ui.classes.ClassesFragment
import com.consultantapp.ui.dashboard.MainActivity
import com.consultantapp.ui.dashboard.appointment.AppointmentDetailsFragment
import com.consultantapp.ui.dashboard.appointment.AppointmentFragment
import com.consultantapp.ui.dashboard.appointment.appointmentStatus.AppointmentStatusActivity
import com.consultantapp.ui.dashboard.appointment.appointmentStatus.CompletedRequestFragment
import com.consultantapp.ui.dashboard.appointment.appointmentStatus.StatusUpdateFragment
import com.consultantapp.ui.dashboard.appointment.rating.AddRatingFragment
import com.consultantapp.ui.dashboard.chat.ChatFragment
import com.consultantapp.ui.dashboard.chat.chatdetail.ChatDetailActivity
import com.consultantapp.ui.dashboard.doctor.DoctorActionActivity
import com.consultantapp.ui.dashboard.doctor.confirm.ConfirmBookingFragment
import com.consultantapp.ui.dashboard.doctor.detail.BottomRequestFragment
import com.consultantapp.ui.dashboard.doctor.detail.DoctorDetailActivity
import com.consultantapp.ui.dashboard.doctor.detail.prefrence.PrefrenceFragment
import com.consultantapp.ui.dashboard.doctor.listing.DoctorListActivity
import com.consultantapp.ui.dashboard.doctor.schedule.ScheduleFragment
import com.consultantapp.ui.dashboard.home.HomeFragment
import com.consultantapp.ui.dashboard.home.banner.BannerFragment
import com.consultantapp.ui.dashboard.home.bookservice.datetime.DateTimeFragment
import com.consultantapp.ui.dashboard.home.bookservice.location.AddAddressActivity
import com.consultantapp.ui.dashboard.home.bookservice.location.BottomAddressFragment
import com.consultantapp.ui.dashboard.home.bookservice.registerservice.RegisterServiceFragment
import com.consultantapp.ui.dashboard.home.bookservice.waiting.DialogAllocatedNurseFragment
import com.consultantapp.ui.dashboard.home.bookservice.waiting.WaitingAllocationFragment
import com.consultantapp.ui.dashboard.location.LocationFragment
import com.consultantapp.ui.dashboard.settings.SettingsFragment
import com.consultantapp.ui.dashboard.subcategory.SubCategoryFragment
import com.consultantapp.ui.dashboard.subscription.SubscriptionDetailFragment
import com.consultantapp.ui.dashboard.subscription.SubscriptionListFragment
import com.consultantapp.ui.drawermenu.DrawerActivity
import com.consultantapp.ui.drawermenu.addmoney.AddCardFragment
import com.consultantapp.ui.drawermenu.addmoney.AddMoneyActivity
import com.consultantapp.ui.drawermenu.history.HistoryFragment
import com.consultantapp.ui.drawermenu.notification.NotificationFragment
import com.consultantapp.ui.drawermenu.profile.ProfileFragment
import com.consultantapp.ui.drawermenu.wallet.WalletFragment
import com.consultantapp.ui.jitsimeet.JitsiActivity
import com.consultantapp.ui.loginSignUp.SignUpActivity
import com.consultantapp.ui.loginSignUp.changepassword.ChangePasswordFragment
import com.consultantapp.ui.loginSignUp.forgotpassword.ForgotPasswordFragment
import com.consultantapp.ui.loginSignUp.insurance.InsuranceFragment
import com.consultantapp.ui.loginSignUp.login.LoginFragment
import com.consultantapp.ui.loginSignUp.loginemail.LoginEmailFragment
import com.consultantapp.ui.loginSignUp.masterprefrence.MasterPrefrenceFragment
import com.consultantapp.ui.loginSignUp.signup.SignUpFragment
import com.consultantapp.ui.loginSignUp.verifyotp.VerifyOTPFragment
import com.consultantapp.ui.loginSignUp.welcome.WelcomeFragment
import com.consultantapp.ui.webview.WebViewActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class BindingsModule {

    @ContributesAndroidInjector
    abstract fun splashActivity(): SplashActivity

    @ContributesAndroidInjector
    abstract fun mainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun signUpActivity(): SignUpActivity

    @ContributesAndroidInjector
    abstract fun welcomeFragment(): WelcomeFragment

    @ContributesAndroidInjector
    abstract fun loginFragment(): LoginFragment

    @ContributesAndroidInjector
    abstract fun verifyOTPFragment(): VerifyOTPFragment

    @ContributesAndroidInjector
    abstract fun doctorListActivity(): DoctorListActivity

    @ContributesAndroidInjector
    abstract fun doctorDetailActivity(): DoctorDetailActivity

    @ContributesAndroidInjector
    abstract fun drawerActivity(): DrawerActivity

    @ContributesAndroidInjector
    abstract fun historyFragment(): HistoryFragment

    @ContributesAndroidInjector
    abstract fun signUpFragment(): SignUpFragment

    @ContributesAndroidInjector
    abstract fun loginEmailFragment(): LoginEmailFragment

    @ContributesAndroidInjector
    abstract fun profileFragment(): ProfileFragment

    @ContributesAndroidInjector
    abstract fun notificationFragment(): NotificationFragment

    @ContributesAndroidInjector
    abstract fun forgotPasswordFragment(): ForgotPasswordFragment

    @ContributesAndroidInjector
    abstract fun changePasswordFragment(): ChangePasswordFragment

    @ContributesAndroidInjector
    abstract fun walletFragment(): WalletFragment

    @ContributesAndroidInjector
    abstract fun addCardFragment(): AddCardFragment

    @ContributesAndroidInjector
    abstract fun chatFragment(): ChatFragment

    @ContributesAndroidInjector
    abstract fun chatDetailActivity(): ChatDetailActivity

    @ContributesAndroidInjector
    abstract fun appointmentFragment(): AppointmentFragment

    @ContributesAndroidInjector
    abstract fun appointmentDetailsFragment(): AppointmentDetailsFragment

    @ContributesAndroidInjector
    abstract fun addRatingFragment(): AddRatingFragment

    @ContributesAndroidInjector
    abstract fun categoriesFragment(): CategoriesFragment

    @ContributesAndroidInjector
    abstract fun classesFragment(): ClassesFragment

    @ContributesAndroidInjector
    abstract fun classesDetailFragment(): ClassesDetailFragment

    @ContributesAndroidInjector
    abstract fun jitsiActivity(): JitsiActivity


    @ContributesAndroidInjector
    abstract fun homeFragment(): HomeFragment


    @ContributesAndroidInjector
    abstract fun locationFragment(): LocationFragment

    @ContributesAndroidInjector
    abstract fun subCategoryFragment(): SubCategoryFragment

    @ContributesAndroidInjector
    abstract fun settingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun bottomRequestFragment(): BottomRequestFragment

    @ContributesAndroidInjector
    abstract fun prefrenceFragment(): PrefrenceFragment

    @ContributesAndroidInjector
    abstract fun doctorActionActivity(): DoctorActionActivity

    @ContributesAndroidInjector
    abstract fun scheduleFragment(): ScheduleFragment

    @ContributesAndroidInjector
    abstract fun confirmBookingFragment(): ConfirmBookingFragment

    @ContributesAndroidInjector
    abstract fun bannerFragment(): BannerFragment

    @ContributesAndroidInjector
    abstract fun callingActivity(): CallingActivity

    @ContributesAndroidInjector
    abstract fun webViewActivity(): WebViewActivity

    @ContributesAndroidInjector
    abstract fun addMoneyActivity(): AddMoneyActivity

    @ContributesAndroidInjector
    abstract fun insuranceFragment(): InsuranceFragment

    @ContributesAndroidInjector
    abstract fun subscriptionListFragment(): SubscriptionListFragment

    @ContributesAndroidInjector
    abstract fun subscriptionDetailFragment(): SubscriptionDetailFragment

    @ContributesAndroidInjector
    abstract fun registerServiceFragment(): RegisterServiceFragment

    @ContributesAndroidInjector
    abstract fun addAddressActivity(): AddAddressActivity

    @ContributesAndroidInjector
    abstract fun dateTimeFragment(): DateTimeFragment

    @ContributesAndroidInjector
    abstract fun waitingAllocationFragment(): WaitingAllocationFragment

    @ContributesAndroidInjector
    abstract fun dialogAllocatedNurseFragment(): DialogAllocatedNurseFragment

    @ContributesAndroidInjector
    abstract fun appointmentStatusActivity(): AppointmentStatusActivity

    @ContributesAndroidInjector
    abstract fun statusUpdateFragment(): StatusUpdateFragment

    @ContributesAndroidInjector
    abstract fun completedRequestFragment(): CompletedRequestFragment

    @ContributesAndroidInjector
    abstract fun masterPrefrenceFragment(): MasterPrefrenceFragment

    @ContributesAndroidInjector
    abstract fun bottomAddressFragment(): BottomAddressFragment

}