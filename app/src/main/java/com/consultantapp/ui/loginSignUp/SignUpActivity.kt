package com.consultantapp.ui.loginSignUp

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.consultantapp.R
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityContainerBinding
import com.consultantapp.ui.loginSignUp.login.LoginFragment
import com.consultantapp.ui.loginSignUp.loginemail.LoginEmailFragment
import com.consultantapp.ui.loginSignUp.signup.SignUpFragment
import com.consultantapp.ui.loginSignUp.welcome.WelcomeFragment
import com.consultantapp.utils.*
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject

class SignUpActivity : DaggerAppCompatActivity() {

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

        val fragment: Fragment
        val bundle = Bundle()

        when {
            intent.hasExtra(UPDATE_PROFILE) -> {
                fragment = SignUpFragment()
            }
            intent.hasExtra(UPDATE_NUMBER) -> {
                fragment = LoginFragment()
                bundle.putBoolean(UPDATE_NUMBER, true)
            }
            intent.hasExtra(EXTRA_LOGIN) -> {
                fragment = LoginFragment()
            }
            intent.hasExtra(EXTRA_LOGIN_EMAIL) -> {
                fragment = LoginEmailFragment()
            }
            intent.hasExtra(EXTRA_SIGNUP_EMAIL) -> {
                fragment = SignUpFragment()
            }
            else ->{
                fragment = WelcomeFragment()
            }
        }
        if (intent.hasExtra(UPDATE_PROFILE))
            bundle.putBoolean(UPDATE_PROFILE, true)

        fragment.arguments = bundle
        addFragment(supportFragmentManager, fragment, R.id.container)
    }

    companion object {
        const val EXTRA_LOGIN = "EXTRA_LOGIN"
        const val EXTRA_LOGIN_EMAIL = "EXTRA_LOGIN_EMAIL"
        const val EXTRA_SIGNUP_EMAIL = "EXTRA_SIGNUP_EMAIL"
    }

}
