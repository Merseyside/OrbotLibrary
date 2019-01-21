package com.upstream.orbotexample

import android.app.Application

class OrbotApplication  : Application() {

    companion object {
        private lateinit var application : OrbotApplication

        fun getInstance() : OrbotApplication {
            return application
        }
    }

    override fun onCreate() {
        super.onCreate()

        application = this
    }

}