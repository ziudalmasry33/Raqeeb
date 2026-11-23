package com.raqeeb.app.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor

class RaqeebVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_VPN") {
            stopVpn()
            return START_NOT_STICKY
        }
        startLocalDnsFilter()
        return START_STICKY
    }

    private fun startLocalDnsFilter() {
        if (vpnInterface != null) return
        val builder = Builder()
            .setSession("RAQEEB Local Protection")
            .addAddress("10.0.0.2", 32)
            .addDnsServer("1.1.1.3") // Cloudflare Family DNS (حظر تلقائي للمحتوى الإباحي والضار)
            .addDnsServer("94.140.14.15") // AdGuard Family DNS
            .addRoute("1.1.1.3", 32)
            .addRoute("94.140.14.15", 32)

        vpnInterface = builder.establish()
    }

    private fun stopVpn() {
        vpnInterface?.close()
        vpnInterface = null
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
