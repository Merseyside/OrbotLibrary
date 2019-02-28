package com.upstream.orbotexample

import android.app.Application
import android.util.Log
import com.merseyside.admin.library.OrbotManager

class OrbotApplication  : Application() {

    private val TAG = javaClass.simpleName

    private lateinit var orbotManager : OrbotManager

    fun getOrbotManager() : OrbotManager {
        return orbotManager
    }

    companion object {
        lateinit var instance : OrbotApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        initOrbotManager()
    }

    private fun initOrbotManager() {
        orbotManager = OrbotManager.Builder(this)
                .build()

        orbotManager.setLoggingEnable(isLogging = BuildConfig.DEBUG)
    }

}