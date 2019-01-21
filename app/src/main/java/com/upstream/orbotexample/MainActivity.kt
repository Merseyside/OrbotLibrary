package com.upstream.orbotexample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.merseyside.admin.library.OrbotConstants
import com.merseyside.admin.library.OrbotManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var orbotManager : OrbotManager

    private var torStatus = OrbotConstants.STATUS_OFF

    private val networkUtils by lazy { NetworkUtils() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initTor()
        doLayout()
    }

    private fun initTor() {
        orbotManager = OrbotManager.getInstance(OrbotApplication.getInstance())
        orbotManager.setOnOrbotListener(object : OrbotManager.OrbotListener {
            override fun onPercentsReceived(percents: Int) {
                if (percents == 100) {
                    open_connection_button.isEnabled = true
                }
            }

            override fun onMessageReceived(message: String) {
                log_tv.text = message
            }


            override fun onStatusChanged(status: String) {
                this@MainActivity.torStatus = status

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

            networkUtils.openHttpConnection("https://www.propub3r6espa33w.onion/") //Put your URL here
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        orbotManager.stopTor()
    }
}
