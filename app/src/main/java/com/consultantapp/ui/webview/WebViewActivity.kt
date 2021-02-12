package com.consultantapp.ui.webview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.consultantapp.R
import com.consultantapp.appClientDetails
import com.consultantapp.data.network.PushType
import com.consultantapp.data.repos.UserRepository
import com.consultantapp.databinding.ActivityWebViewBinding
import com.consultantapp.utils.*
import dagger.android.support.DaggerAppCompatActivity
import javax.inject.Inject


class WebViewActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityWebViewBinding

    private var isReceiverRegistered = false

    private var transactionId = ""

    private var loadUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view)

        initialise()
        bindViews()
        setListeners()
    }

    private fun initialise() {
        binding.toolbar.title = intent.getStringExtra(LINK_TITLE)
        binding.clLoader.setBackgroundResource(R.color.colorWhite)

        when (intent.getStringExtra(LINK_URL)) {
            PageLink.TERMS_CONDITIONS, PageLink.PRIVACY_POLICY -> {
                binding.tvAgree.visible()
            }
            else -> {
                binding.tvAgree.gone()
            }

        }

        if (intent.hasExtra(PAYMENT_URL)) {
            transactionId = intent.getStringExtra(EXTRA_REQUEST_ID) ?: ""
            loadUrl = intent.getStringExtra(PAYMENT_URL) ?: ""
        } else {
            loadUrl = "${appClientDetails.domain_url}/${intent.getStringExtra(LINK_URL)}"
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun bindViews() {

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true)
        } else {
            CookieManager.getInstance().setAcceptCookie(true)
        }

        binding.webView.setBackgroundColor(Color.TRANSPARENT)
        binding.webView.settings.setSupportZoom(true)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(wv: WebView?, url: String): Boolean {
                if (url.startsWith("tel:") || url.startsWith("mailto:")) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    return true
                }
                return false
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                if (url?.contains("PAYMENT_SUCCESS") == true) {
                }
            }
        }


        binding.webView.settings.setAppCacheEnabled(true)
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.loadWithOverviewMode = true
        binding.webView.settings.useWideViewPort = true
        binding.webView.setInitialScale(100)
        binding.webView.webChromeClient = WebChromeClient()


        // Return the app name after finish loading

        binding.webView.loadUrl(loadUrl)

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {

                // Return the app name after finish loading
                if (progress == 100)
                    binding.clLoader.gone()
            }
        }
    }

    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvAgree.setOnClickListener {
            finish()
        }

    }

    companion object {
        const val LINK_TITLE = "LINK_TITLE"
        const val LINK_URL = "LINK_URL"
        const val PAYMENT_URL = "PAYMENT_URL"
    }

    override fun onResume() {
        super.onResume()
        registerReceiver()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver()
    }

    private fun registerReceiver() {
        if (!isReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(PushType.BALANCE_ADDED)
            intentFilter.addAction(PushType.BALANCE_FAILED)
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    callCancelledReceiver, intentFilter
            )
            isReceiverRegistered = true
        }
    }

    private fun unregisterReceiver() {
        if (isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(callCancelledReceiver)
            isReceiverRegistered = false
        }
    }

    private val callCancelledReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra(EXTRA_REQUEST_ID) == transactionId) {
                if (intent.action == PushType.BALANCE_ADDED) {
                    longToast(getString(R.string.transaction_success))
                    setResult(Activity.RESULT_OK)
                    finish()
                } else if (intent.action == PushType.BALANCE_FAILED) {
                    longToast(getString(R.string.transaction_failed))
                    finish()
                }
            }
        }
    }
}
