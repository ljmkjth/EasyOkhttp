package com.jie.net.context

sealed class MapValueContext<T> {
    protected val map: MutableMap<String, T> = mutableMapOf()
    infix fun String.with(v: T) {
        map[this] = v
    }

    override fun toString(): String {
        val sb = StringBuilder()
        map.forEach {
            sb.append(it.key)
            sb.append("=")
            sb.append(it.value.toString())
            sb.append("&")
        }
        return sb.removeSuffix("&").toString()
    }
}