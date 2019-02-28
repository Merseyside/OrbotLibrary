package com.merseyside.admin.library

class OrbotConstants {

    companion object {
        const val STATUS_OFF : String = "OFF"

        const val STATUS_ON: String = "ON"

        const val STATUS_STARTING: String = "STARTING"

        const val STATUS_STOPPING: String = "STOPPING"

        val COUNTRY_CODES = arrayOf(
                "DE", "AT", "SE", "CH", "IS", "CA",
                "US", "ES", "FR", "BG", "PL", "AU",
                "BR", "CZ", "DK", "FI", "GB", "HU",
                "NL", "JP", "RO", "RU", "SG", "SK")

        @JvmStatic
        fun getCountryCodes() : Array<String> {
            return COUNTRY_CODES
        }

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

                return DIRECTLY
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