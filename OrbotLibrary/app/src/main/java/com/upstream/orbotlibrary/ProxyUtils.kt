package com.upstream.orbotlibrary;

import android.app.Application
import android.util.Log
import info.guardianproject.netcipher.web.WebkitProxy

class ProxyUtils {

    private val TAG = javaClass.simpleName

    fun initProxy(application: Application) {

        val host = "localhost"
        val port = 8118

        try {
            WebkitProxy.setProxy(application.javaClass.name, application.applicationContext, null, host, port)
        } catch (e: Exception) {
            Log.e(TAG, "error enabling web proxying", e)
        }

    }
}
