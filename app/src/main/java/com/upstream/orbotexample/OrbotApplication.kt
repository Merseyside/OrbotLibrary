package com.upstream.orbotexample

import android.app.Application
import com.merseyside.admin.library.OrbotManager

class OrbotApplication  : Application() {

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
                .setHostAndPort(OrbotManager.DEFAULT_HOST, OrbotManager.DEFAULT_PORT)
                .build()

        orbotManager.setLoggingEnable(isLogging = BuildConfig.DEBUG)
    }

}