package com.jie.netexample

import android.os.Bundle
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        HttpUtil.easyHttp.get {  }
        HttpUtil.easyHttp.post {  }
    }
}
