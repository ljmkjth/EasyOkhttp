package com.jie.netexample

import com.jie.net.EasyHttpFactory


object HttpUtil {

    private val prepareHeaders by lazy {
        object : EasyHttpFactory.PrepareHttpHeaders {
            override fun getNewToken(): String? {
                return ""
            }

            override fun getToken(): String? {
                return ""
            }

            override fun isSkipToken(url: String): Boolean {
                return true
            }

        }
    }
    val easyHttp by lazy { EasyHttpFactory.create(prepareHeaders, emptyList()) }
}