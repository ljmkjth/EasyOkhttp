package com.jie.net.context

class Params : MapValueContext<Any>() {

    fun exists(): Boolean {
        return map.isNotEmpty()
    }
}