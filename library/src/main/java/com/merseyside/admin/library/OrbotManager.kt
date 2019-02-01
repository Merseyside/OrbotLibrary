package com.merseyside.admin.library

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.torproject.android.service.TorService
import org.torproject.android.service.TorServiceConstants

class OrbotManager private constructor(private val application : Application) {

    private val TAG = javaClass.simpleName

    interface OrbotListener {

        fun onStatusChanged(status : String)

        fun onMessageReceived(message : String)

        fun onPercentsReceived(percents : Int)
    }

    inner class DataCount constructor(// data uploaded
        var Upload: Long, // data downloaded
        var Download: Long
    )

    companion object {

        private var torStatus: String = TorServiceConstants.STATUS_OFF

        fun getStatus() : String = torStatus

        private var instance : OrbotManager? = null

        fun getInstance(application : Application? = null) : OrbotManager {
            if (instance == null) {
                if (application != null)
                    instance = OrbotManager(application)
                else
                    throw NullPointerException("Pass application instance")
            }

            return instance ?: throw NullPointerException("Instance is null")
        }
    }

    private val STATUS_UPDATE = 1
    private val MESSAGE_TRAFFIC_COUNT = 2

    private var lastStatusIntent: Intent? = null


    private lateinit var localBroadcastManager: LocalBroadcastManager
    private var proxyUtils: ProxyUtils = ProxyUtils()

    private var listener : OrbotListener? = null

    private val mLocalBroadcastReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            val action = intent.action ?: return

            when (action) {
                TorServiceConstants.LOCAL_ACTION_LOG -> {
                    val msg = messageHandler.obtainMessage(STATUS_UPDATE)
                    msg.data.putString("log", intent.getStringExtra(TorServiceConstants.LOCAL_EXTRA_LOG))
                    msg.data.putString("status", intent.getStringExtra(TorServiceConstants.EXTRA_STATUS))
                    messageHandler.sendMessage(msg)

                }
                TorServiceConstants.LOCAL_ACTION_BANDWIDTH -> {
                    val upload = intent.getLongExtra("up", 0)
                    val download = intent.getLongExtra("down", 0)
                    val written = intent.getLongExtra("written", 0)
                    val read = intent.getLongExtra("read", 0)

                    val msg = messageHandler.obtainMessage(MESSAGE_TRAFFIC_COUNT)
                    msg.data.putLong("download", download)
                    msg.data.putLong("upload", upload)
                    msg.data.putLong("readTotal", read)
                    msg.data.putLong("writeTotal", written)
                    msg.data.putString("status", intent.getStringExtra(TorServiceConstants.EXTRA_STATUS))

                    messageHandler.sendMessage(msg)

                }
                TorServiceConstants.ACTION_STATUS -> {
                    lastStatusIntent = intent

                    val msg = messageHandler.obtainMessage(STATUS_UPDATE)
                    msg.data.putString("status", intent.getStringExtra(TorServiceConstants.EXTRA_STATUS))

                    messageHandler.sendMessage(msg)
                }
            }
        }
    }

    fun setOrbotListener(listener : OrbotListener) {
        this.listener = listener
    }

    private val messageHandler = @SuppressLint("HandlerLeak")
    object : Handler() {

        override fun handleMessage(msg: Message) {

            when (msg.what) {
                STATUS_UPDATE -> {
                    val newTorStatus = msg.data.getString("status")
                    val log = msg.data.getString("log")

                    log?.let {
                        listener?.onMessageReceived(log)
                        getPercents(log)
                    }

                    if (torStatus != newTorStatus) {
                        torStatus = newTorStatus
                        updateStatus(newTorStatus)
                    }

                }

                MESSAGE_TRAFFIC_COUNT -> {
                    val data = msg.data
                    val datacount = DataCount(data.getLong("upload"), data.getLong("download"))
                    val totalRead = data.getLong("readTotal")
                    val totalWrite = data.getLong("writeTotal")
                }

                else -> super.handleMessage(msg)
            }
        }
    }


    private fun updateStatus(status : String?) {
        when(status) {
            null -> {
                return
            }

            TorServiceConstants.STATUS_ON -> {
                proxyUtils.initProxy(application)
            }
        }

        listener?.onStatusChanged(status!!)
    }

    private fun registerReceivers() {
        localBroadcastManager = LocalBroadcastManager.getInstance(application)
        localBroadcastManager.registerReceiver(
            mLocalBroadcastReceiver,
            IntentFilter(TorServiceConstants.ACTION_STATUS)
        )
        localBroadcastManager.registerReceiver(
            mLocalBroadcastReceiver,
            IntentFilter(TorServiceConstants.LOCAL_ACTION_BANDWIDTH)
        )
        localBroadcastManager.registerReceiver(
            mLocalBroadcastReceiver,
            IntentFilter(TorServiceConstants.LOCAL_ACTION_LOG)
        )
    }

    /*Starts tor daemon*/
    fun startTor() {

        if (torStatus == TorServiceConstants.STATUS_OFF) {

            registerReceivers()
            sendIntentToService(TorServiceConstants.ACTION_START)
        }
    }

    /*Stops tor daemon*/
    fun stopTor() {

        if (torStatus != TorServiceConstants.STATUS_OFF) {

            localBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver)

            requestTorStatus()
            val torService = Intent(application, TorService::class.java)
            application.stopService(torService)

            listener?.onStatusChanged(TorServiceConstants.STATUS_OFF)

            torStatus = TorServiceConstants.STATUS_OFF
        }
    }

    private fun sendIntentToService(action: String) {

        val torService = Intent(application, TorService::class.java)
        torService.action = action
        application.startService(torService)
    }

    private fun requestTorStatus() {
        sendIntentToService(TorServiceConstants.ACTION_STATUS)
    }

    fun getCurrentStatus() : String {
        return torStatus
    }

    private fun getPercents(message : String) {
        val regex = "^.+Bootstrapped\\s(\\d*)%.+$".toRegex()
        val matchResult = regex.find(message)

        val result = matchResult?.destructured?.component1()

        if (result != null)
            listener?.onPercentsReceived(result.toInt())
    }

}