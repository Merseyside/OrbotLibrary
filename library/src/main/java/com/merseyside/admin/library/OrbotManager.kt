package com.merseyside.admin.library

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Message
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import org.torproject.android.service.TorService
import org.torproject.android.service.TorServiceConstants
import org.torproject.android.service.util.Prefs

class OrbotManager private constructor(
        private val context: Context,
        host : String,
        port : Int)
{

    private val TAG = javaClass.simpleName

    interface OrbotListener {

        fun onStatusChanged(status : String)

        fun onMessageReceived(message : String)

        fun onPercentsReceived(percents : Int)

        fun onError(message : String)
    }

    inner class DataCount constructor(
            var Upload: Long,
            var Download: Long
    )

    private var torStatus: String = TorServiceConstants.STATUS_OFF

    private val STATUS_UPDATE = 1
    private val MESSAGE_TRAFFIC_COUNT = 2

    private var lastStatusIntent: Intent? = null

    private var isLogging = BuildConfig.DEBUG


    private lateinit var localBroadcastManager: LocalBroadcastManager
    private val proxyUtils: ProxyUtils

    private var listener : OrbotListener? = null

    class Builder(private val context : Context) {

        private var host : String = "localhost"
        private var port : Int = 8118

        fun setHostAndPort(host : String, port : Int) : Builder {
            this.host = host
            this.port = port

            return this
        }

        fun build() : OrbotManager {
            return OrbotManager(context, host, port)
        }
    }

    init {
        Prefs.setContext(context)
        proxyUtils = ProxyUtils(host, port)
    }

    fun getStatus() : String = torStatus

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
                        sendMessage(log)
                        getPercents(log)
                    }

                    if (torStatus != newTorStatus) {
                        torStatus = newTorStatus!!
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

            OrbotConstants.STATUS_ON -> {
                proxyUtils.initProxy(context)
            }
        }

        listener?.onStatusChanged(status!!)
    }

    private fun registerReceivers() {
        localBroadcastManager = LocalBroadcastManager.getInstance(context)
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
    @Throws(IllegalStateException::class)
    fun startTor() {

        if (torStatus == OrbotConstants.STATUS_OFF) {

            registerReceivers()
            val startIntent = Intent(context, TorService::class.java)
            startIntent.action = TorServiceConstants.ACTION_START

            sendIntentToService(startIntent)

            torStatus = OrbotConstants.STATUS_STARTING
        } else {
            val message = "Tor is already running"

            sendMessage(message)
            throw IllegalStateException(message)
        }
    }

    /*Stops tor daemon*/
    @Throws(IllegalStateException::class)
    fun stopTor() {

        if (torStatus != TorServiceConstants.STATUS_OFF) {

            localBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver)

            requestTorStatus()
            val torService = Intent(context, TorService::class.java)
            context.stopService(torService)

            listener?.onStatusChanged(TorServiceConstants.STATUS_OFF)

            torStatus = TorServiceConstants.STATUS_OFF
        } else {
            val message = "Tor isn't running"

            sendMessage(message)
            throw IllegalStateException(message)
        }
    }

    @Throws(IllegalStateException::class)
    fun reconnectTor() {
        if (torStatus != TorServiceConstants.STATUS_OFF) {
            stopTor()
            startTor()
        } else {
            val message = "Tor isn't running"

            sendMessage(message)
            throw IllegalStateException(message)
        }
    }

    private fun sendIntentToService(intent: Intent) {
        context.startService(intent)
    }

    private fun requestTorStatus() {
        val torStatusIntent = Intent(context, TorService::class.java)
        torStatusIntent.action = TorServiceConstants.ACTION_STATUS

        sendIntentToService(torStatusIntent)
    }

    private fun getPercents(message : String) {
        val regex = "^.+Bootstrapped\\s(\\d*)%.+$".toRegex()
        val matchResult = regex.find(message)

        val result = matchResult?.destructured?.component1()

        if (result != null)
            listener?.onPercentsReceived(result.toInt())
    }

    @Throws(IllegalArgumentException::class)
    fun setExitNode(node : String) {
        var validNode = ""

        if (OrbotConstants.COUNTRY_CODES.contains(node)) {
            validNode = "{$node}"
        } else if (node == "") {
        } else {
            throw IllegalArgumentException("Node you passed isn't valid")
        }

        val setNodeIntent = Intent(context, TorService::class.java)
        setNodeIntent.action = TorServiceConstants.CMD_SET_EXIT
        setNodeIntent.putExtra("exit", validNode)

        sendIntentToService(setNodeIntent)
    }

    fun getExitNode() : String = Prefs.getExitNodes()


    fun setBridge(bridge : OrbotConstants.BRIDGES) {

        when(bridge) {
            OrbotConstants.BRIDGES.DIRECTLY -> {
                Prefs.putBridgesEnabled(false)
            }

            else -> {
                Prefs.putBridgesEnabled(true)
            }
        }

        Prefs.setBridgesList(bridge.value)

        if (torStatus == TorServiceConstants.STATUS_ON) {
            val bridgeList = Prefs.getBridgesList()
            if (bridgeList != null && bridgeList.isNotEmpty()) {
                requestTorRereadConfig()
            }
        }
    }

    fun getBridge() : String = Prefs.getBridgesList()

    private fun requestTorRereadConfig() {
        val intent = Intent(context, TorService::class.java)
        intent.action = TorServiceConstants.CMD_SIGNAL_HUP

        sendIntentToService(intent)
    }

    fun setEntryNodes(nodes : String?) {

        nodes?.let {
            val result : String = getValidNodes(nodes)

            Prefs.putEntryNodes(result)
        }
    }

    fun getEntryNodes() : String {
        return Prefs.getEntryNodes()
    }

    fun setExcludeNodes(nodes : String?) {
        nodes?.let {
            val result : String = getValidNodes(nodes)

            Prefs.putExcludeNodes(result)
        }
    }

    fun getExcludeNodes() : String {
        return Prefs.getExcludeNodes()
    }

    private fun getValidNodes(nodes : String) : String {
        if (!nodes.isEmpty()) {

            var result = nodes

            if (!result.startsWith("{")) {
                result = "{$result"
            }

            if (!result.endsWith("}")) {
                result = "$result}"
            }

            return result.toUpperCase()
        } else {
            return nodes
        }
    }

    fun requestNewTorIdentity() {
        val newIdentityIntent = Intent(context, TorService::class.java)
        newIdentityIntent.action = TorServiceConstants.CMD_NEWNYM

        sendIntentToService(newIdentityIntent)
    }

    fun setLoggingEnable(isLogging : Boolean) {
        this.isLogging = isLogging
    }

    private fun sendMessage(message : String) {

        if (message.contains("^.+Exception.+$".toRegex())) {
            sendError(message)
        } else {
            if (isLogging) {
                Log.d(TAG, "Message received: $message")
            }

            listener?.onMessageReceived(message)
        }
    }

    private fun sendError(message : String) {
        if (isLogging) {
            Log.e(TAG, "Error received: $message")
        }

        listener?.onError(message)
    }

}