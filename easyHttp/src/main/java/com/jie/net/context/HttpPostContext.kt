package com.jie.net.context

import com.jie.net.Method
import okhttp3.Request

class HttpPostContext: HttpContext(method = Method.POST) {
    private val body = Body()

    fun body(init: Body.() -> Unit) {
        body.init()
    }

    override fun makeOkHttpRequest(): Request {
        val builder = Request.Builder()
        if (params.exists()) {
            builder.url("$url?$params")
        } else {
            builder.url(url)
        }
        headers.forEach { k, v ->
            builder.addHeader(k, v)
        }
        builder.post(body.toRequestBody())
        return builder.build()
    }
}