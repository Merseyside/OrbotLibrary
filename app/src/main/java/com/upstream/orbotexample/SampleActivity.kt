package com.upstream.orbotexample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.merseyside.admin.library.OrbotConstants
import com.merseyside.admin.library.OrbotManager
import kotlinx.android.synthetic.main.activity_main.*
import java.net.SocketTimeoutException
import java.util.*

class SampleActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    private val orbotManager : OrbotManager = OrbotApplication.instance.getOrbotManager()

    private val networkUtils by lazy { NetworkUtils() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTor()
        doLayout()
    }

    private fun initTor() {
        orbotManager.setLoggingEnable(isLogging = BuildConfig.DEBUG)

        orbotManager.setOrbotListener(object : OrbotManager.OrbotListener {
            override fun onPercentsReceived(percents: Int) {
                if (percents == 100) {
                    open_connection_button.isEnabled = true
                }
            }

            override fun onMessageReceived(message: String) {
                log_tv.text = message
            }


            /**
            * Also you can call OrbotManager.getStatus()
            */

            override fun onStatusChanged(status: String) {
                when (status) {
                    OrbotConstants.STATUS_OFF -> {
                        status_tw.text = "Disconnected"
                        start_stop_button.text = "Start Tor"
                        open_connection_button.isEnabled = false
                    }

                    OrbotConstants.STATUS_ON -> {
                        status_tw.text = "Connected"
                    }

                    OrbotConstants.STATUS_STOPPING -> {
                        status_tw.text = "Stopping..."
                    }

                    OrbotConstants.STATUS_STARTING -> {
                        start_stop_button.text = "Stop Tor"
                        status_tw.text = "Starting..."
                    }
                }
            }
        })
    }

    private fun doLayout() {
        setContentView(R.layout.activity_main)
        status_tw.text = "Disconnected"

        start_stop_button.setOnClickListener {
            if (orbotManager.getCurrentStatus() == OrbotConstants.STATUS_OFF) {
                orbotManager.startTor()
            }
            else {
                orbotManager.stopTor()
                log_tv.text = ""
            }
        }

        open_connection_button.setOnClickListener {

            content.text = ""

            networkUtils.setResponseListener(object : NetworkUtils.ResponseListener {
                override fun onResponse(body: String) {
                    content.text = body
                }
            })

            try {
                networkUtils.openHttpConnection("https://www.propub3r6espa33w.onion/") //Put your URL here
            } catch (e : SocketTimeoutException) {
                Toast.makeText(this, "Socket timeout", Toast.LENGTH_SHORT).show()
            }
        }

        reconnect_button.setOnClickListener {
            try {
                orbotManager.reconnectTor()
            } catch (e : IllegalStateException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        new_identity_button.setOnClickListener {
            orbotManager.requestNewTorIdentity()
        }

        setCountrySpinner()
        setBridgeSelector()
    }

    private fun setCountrySpinner() {
        val currentExitNode = orbotManager.getExitNode()

        if (currentExitNode.length > 4) {
            //someone put a complex value in, so let's disable
            val cList = ArrayList<String>()
            cList.add(0, currentExitNode)

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cList)
            countrySpinner.adapter = adapter
            countrySpinner.isEnabled = false
        } else {
            var selIdx = -1

            val cList = ArrayList<String>()
            cList.add(0, getString(R.string.vpn_default_world))

            for (i in OrbotConstants.COUNTRY_CODES.indices) {
                val locale = Locale("", OrbotConstants.COUNTRY_CODES[i])
                cList.add(locale.displayCountry)

                if (currentExitNode.contains(OrbotConstants.COUNTRY_CODES[i])) {
                    selIdx = i + 1
                }
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, cList)
            countrySpinner.adapter = adapter

            if (selIdx > 0) {
                countrySpinner.setSelection(selIdx, true)
            }

            countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                var mOldPosition = countrySpinner.selectedItemPosition

                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {

                    if (mOldPosition == position) {
                        return
                    }

                    mOldPosition = position

                    val country = if (position == 0) {
                        ""
                    } else {
                        OrbotConstants.COUNTRY_CODES[position - 1]
                    }

                    orbotManager.setExitNode(country)
                }

                override fun onNothingSelected(parentView: AdapterView<*>) {}

            }
        }
    }

    private fun setBridgeSelector() {
        bridges.setOnValueChangeListener { value ->
            Log.d(TAG, value)
            orbotManager.setBridge(OrbotConstants.BRIDGES.getByValue(value))
        }

        bridges.currentEntryValue = orbotManager.getBridge()
    }

    override fun onDestroy() {
        super.onDestroy()

        orbotManager.stopTor()
    }
}
