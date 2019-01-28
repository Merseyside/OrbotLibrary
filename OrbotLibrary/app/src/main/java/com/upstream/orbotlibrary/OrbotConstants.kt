package com.upstream.orbotlibrary

interface OrbotConstants {

    companion object {
        val STATUS_OFF : String
            get() = "OFF"

        val STATUS_ON: String
            get() = "ON"

        val STATUS_STARTING: String
            get() = "STARTING"

        val STATUS_STOPPING: String
            get() = "STOPPING"
    }

}