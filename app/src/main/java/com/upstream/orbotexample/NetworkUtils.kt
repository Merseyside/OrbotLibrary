package com.upstream.orbotexample

import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request


class NetworkUtils {

    private var responseListener : ResponseListener? = null

    interface ResponseListener {
        fun onResponse(body : String)
    }

    fun setResponseListener(responseListener : ResponseListener) {
        this.responseListener = responseListener
    }

    @Throws(Exception::class)
    fun openHttpConnection(url : String) {
        Thread {

            val client = OkHttpClient()

            val request = Request.Builder()
                .url(url)
                .build()

            val response = client.newCall(request).execute()
            val body = response.body()?.string() ?: "null"


            val handler = Handler(Looper.getMainLooper())
            handler.post { responseListener?.onResponse(body) }

        }.start()
    }
}