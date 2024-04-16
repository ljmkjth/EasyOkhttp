package com.jie.net.context

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class Body {
    private val form = Form()

    var text = ""
    var json = ""

    fun form(init: Form.() -> Unit) {
        form.init()
    }

    internal fun toRequestBody(): RequestBody {
        if (text.isNotEmpty()) {
            return text.toRequestBody("text/plain;charset=utf-8".toMediaTypeOrNull())
        } else if (json.isNotEmpty()) {
            return json.toRequestBody("application/json;charset=utf-8".toMediaTypeOrNull())
        }
        return form.toString()
            .toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())
    }

}