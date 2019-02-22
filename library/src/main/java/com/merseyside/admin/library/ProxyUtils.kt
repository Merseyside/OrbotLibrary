package com.merseyside.admin.library;

import android.content.Context
import android.util.Log
import info.guardianproject.netcipher.web.WebkitProxy

internal class ProxyUtils(private val host : String, private val port : Int) {

    private val TAG = javaClass.simpleName

    fun initProxy(context: Context) {

        try {
            WebkitProxy.setProxy(context.javaClass.name, context, null, host, port)
        } catch (e: Exception) {
            Log.e(TAG, "error enabling web proxying", e)
        }

    }
}
