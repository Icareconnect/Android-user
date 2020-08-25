package com.consultantapp.data.network

object Config {

    var BASE_URL = ""
    var BASE_URL_DEV = "https://royoconsult.com/"
    var BASE_URL_TEST = "https://royoconsult.com/"
    var BASE_URL_CLIENT = "https://royoconsult.com/"
    var BASE_URL_LIVE = "https://royoconsult.com/"
    var BASE_URL_LOCAL = "https://royoconsult.com/"

    var SITE_URL = ""
    var IMAGE_URL = "https://consultants3assets.sfo2.digitaloceanspaces.com/"

    private val appMode = AppMode.LOCAL

    val baseURL: String
        get() {
            init(appMode)
            return BASE_URL
        }

    val siteURL: String
        get() {
            init(appMode)
            return SITE_URL
        }

    val imageURL: String
        get() {
            init(appMode)
            return IMAGE_URL
        }

    private fun init(appMode: AppMode) {

        when (appMode) {
            AppMode.LOCAL -> {
                BASE_URL = BASE_URL_LOCAL
            }
            AppMode.DEV -> {
                BASE_URL = BASE_URL_DEV
            }
            AppMode.TEST -> {
                BASE_URL = BASE_URL_TEST
            }
            AppMode.CLIENT -> {
                BASE_URL = BASE_URL_CLIENT
            }
            AppMode.LIVE -> {
                BASE_URL = BASE_URL_LIVE
            }
        }
    }

    private enum class AppMode {
        LOCAL, DEV, TEST, CLIENT, LIVE
    }
}