package com.jie.net

import com.jie.net.context.HttpContext
import com.jie.net.context.HttpGetContext
import com.jie.net.context.HttpPostContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit


class EasyHttp private constructor(
    private val prepareHttpHeaders: EasyHttpFactory.PrepareHttpHeaders,
    private val interceptors: List<Interceptor>
) {

    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        interceptors.forEach {
            builder.addInterceptor(it)
        }
        builder.connectTimeout(10, TimeUnit.SECONDS)
        builder.build()
    }
    private val threadPool by lazy {
        ThreadPoolExecutor(
            8, 64,
            60L, TimeUnit.SECONDS,
            LinkedBlockingQueue<Runnable>()
        )
    }

    companion object {
        internal fun createInstance(
            prepareHttpHeaders: EasyHttpFactory.PrepareHttpHeaders,
            interceptors: List<Interceptor>
        ): EasyHttp {
            return EasyHttp(prepareHttpHeaders, interceptors)
        }
    }

    fun get(init: HttpGetContext.() -> Unit): Result<Response> {
        val context = HttpGetContext().apply(init)
        return wrapContinueTokenCall(context)
    }

    // 异步的GET
    fun get(
        init: HttpGetContext.() -> Unit,
        callback: (e: Throwable?, response: Response?) -> Unit,
    ) {
        threadPool.execute {
            val context = HttpGetContext().apply(init)
            val result = wrapContinueTokenCall(context)
            if (result.isFailure) {
                callback.invoke(result.exceptionOrNull(), null)
            } else {
                callback.invoke(null, result.getOrNull())
            }
        }
    }

    fun post(init: HttpPostContext.() -> Unit): Result<Response> {
        val context = HttpPostContext().apply(init)
        return wrapContinueTokenCall(context)
    }

    /**
     * 续token请求
     */
    private fun wrapContinueTokenCall(context: HttpContext): Result<Response> {
        val result = runCatching<Response> {
            val skipToken = prepareHttpHeaders.isSkipToken(context.url)
            if (!skipToken) {
                val token = prepareHttpHeaders.getToken()
                if (!token.isNullOrEmpty()) {
                    context.getHeaders().addHeader("Authorization", token)
                }
            }
            val request = context.makeOkHttpRequest()
            var response = okHttpClient.newCall(request).execute()
            val data = getBodyString(response)
            if (data != null) {
                if (skipToken) {
                    try {
                        JSONObject(data)
                        return Result.success(response)
                    } catch (e: Exception) {
                        throw Throwable(data)
                    }
                }
                val code = try {
                    JSONObject(data).optInt("code", -1)
                } catch (_: Exception) {
                    -1
                }
                when (code) {
                    401 -> {
                        // token过期，需要续token
                        val newToken = prepareHttpHeaders.getNewToken()
                        if (!newToken.isNullOrEmpty()) {
                            context.getHeaders().addHeader("Authorization", newToken)
                        }
                        val newRequest = context.makeOkHttpRequest()
                        response = okHttpClient.newCall(newRequest).execute()
                        getBodyString(response)
                            ?: throw Throwable("response is empty.")
                        return Result.success(response)
                    }

                    -1 -> {
                        throw Throwable(data)
                    }

                    else -> {
                        return Result.success(response)
                    }
                }
            }
            throw Throwable("response is empty.")
        }
        return result
    }

    private fun getBodyString(response: Response): String? {
        val responseBody = response.body
        var bodyString: String? = null
        if (responseBody != null) {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE)
            val bf = source.buffer()
            var charset = StandardCharsets.UTF_8
            val contentType = responseBody.contentType()
            if (contentType != null) {
                charset = contentType.charset(StandardCharsets.UTF_8)
            }
            if (bf.size > 0) {
                bodyString = bf.clone().readString(charset)
            }
        }
        return bodyString
    }
}