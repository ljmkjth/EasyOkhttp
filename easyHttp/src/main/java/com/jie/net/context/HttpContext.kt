package com.jie.net.context

import com.jie.net.Method
import okhttp3.Request

internal interface IHttpContext {
    fun headers(init: Headers.() -> Unit)
}

abstract class HttpContext(private val method: Method) : IHttpContext {
    var url = ""

    protected val headers = Headers()
    protected val params = Params()

    override fun headers(init: Headers.() -> Unit) {
        headers.init()
    }

    fun params(init: Params.() -> Unit) {
        params.init()
    }

    internal fun getHeaders(): Headers {
        return headers
    }

    internal open fun makeOkHttpRequest(): Request {
        var builder = Request.Builder()
        if (params.exists()) {
            builder.url("$url?$params")
        } else {
            builder.url(url)
        }
        headers.forEach { k, v ->
            builder.addHeader(k, v)
        }
        builder = when (method) {
            Method.DELETE -> {
                builder.delete()
            }
            else -> {
                builder.get()
            }
        }
        return builder.build()
    }
}


