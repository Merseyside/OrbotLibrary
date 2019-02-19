package com.merseyside.admin.library

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

        val COUNTRY_CODES = arrayOf(
                "DE", "AT", "SE", "CH", "IS", "CA", "US",
                "ES", "FR", "BG", "PL", "AU", "BR", "CZ",
                "DK", "FI", "GB", "HU", "NL", "JP", "RO",
                "RU", "SG", "SK")
    }

    enum class BRIDGES(val value : String) {DIRECTLY(""), COMMUNITY("obfs4"), CLOUD("meek");

        companion object {

            @Throws(IllegalArgumentException::class)
            fun getByValue(value: String): BRIDGES {
                BRIDGES.values().forEach {
                    if (it.value.equals(value, true)) {
                        return it
                    }
                }

                throw IllegalArgumentException("No bridge with this value")
            }

            fun contains(value: String): Boolean {
                BRIDGES.values().forEach {
                    if (it.value.equals(value, true))
                        return true
                }

                return false
            }
        }

    }

}