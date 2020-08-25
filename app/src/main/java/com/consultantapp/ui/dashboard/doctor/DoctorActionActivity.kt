package com.consultantapp.ui.dashboard.doctor

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.consultantapp.R
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityContainerBinding
import com.consultantapp.ui.dashboard.doctor.confirm.ConfirmBookingFragment
import com.consultantapp.ui.dashboard.doctor.detail.prefrence.PrefrenceFragment
import com.consultantapp.ui.dashboard.doctor.schedule.ScheduleFragment
import com.consultantapp.utils.*
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class DoctorActionActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    lateinit var binding: ActivityContainerBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initialise()
        //makeFullScreen(this)
    }

    private fun initialise() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_container)

        LocaleHelper.setLocale(this, userRepository.getUserLanguage(), prefsManager)

        when (intent.getStringExtra(PAGE_TO_OPEN)) {
            FILTER ->
                addFragment(supportFragmentManager, PrefrenceFragment(), R.id.container)
            RequestType.SCHEDULE ->
                addFragment(supportFragmentManager, ScheduleFragment(), R.id.container)
            RequestType.INSTANT ->
                addFragment(supportFragmentManager, ConfirmBookingFragment(), R.id.container)
        }
    }

    companion object {
        const val FILTER = "FILTER"
    }
}
