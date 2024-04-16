package com.jie.net

import okhttp3.Interceptor

object EasyHttpFactory {
    interface PrepareHttpHeaders {
        fun isSkipToken(url: String): Boolean
        fun getToken(): String?
        fun getNewToken(): String?
    }

    fun create(prepareHttpHeaders: PrepareHttpHeaders, interceptors: List<Interceptor>): EasyHttp {
        return EasyHttp.createInstance(prepareHttpHeaders, interceptors)
    }
}