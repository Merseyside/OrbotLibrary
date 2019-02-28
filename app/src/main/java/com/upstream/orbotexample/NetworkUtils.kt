package com.upstream.orbotexample

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


class NetworkUtils {

    private val okHttpClient : OkHttpClient by lazy {
        OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
    }

    private var responseListener : ResponseListener? = null

    interface ResponseListener {
        fun onResponse(body : String)
    }

    fun setResponseListener(responseListener : ResponseListener) {
        this.responseListener = responseListener
    }

    @Throws(Exception::class)
    fun openHttpConnection(url : String) {
        val connectionTask = ConnectionTask()
        connectionTask.execute(url)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ConnectionTask : AsyncTask<String, Void, String>() {

        override fun doInBackground(vararg url: String): String {
            val request = Request.Builder()
                    .url(url[0])
                    .build()

            return try {
                val response = okHttpClient.newCall(request).execute()

                response.body()?.string() ?: "null"
            } catch (e : SocketTimeoutException) {
                e.printStackTrace()

                "Request timeout"
            }
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            responseListener?.onResponse(result)
        }
    }
}