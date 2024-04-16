package com.jie.net.context

class Headers: MapValueContext<String>() {
    fun addHeader(name: String, value: String) {
        map[name] = value
    }

    fun removeHeader(name: String) {
        map.remove(name)
    }

    internal fun forEach(action: (k: String, v: String) -> Unit) = map.forEach { (k, v) -> action(k, v) }
}