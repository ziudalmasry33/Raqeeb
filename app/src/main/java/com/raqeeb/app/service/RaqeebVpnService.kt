package com.raqeeb.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.raqeeb.app.R

class RaqeebVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null

    companion object {
        var isRunning: Boolean = false
        private const val VPN_CHANNEL_ID = "raqeeb_vpn_shield"
        private const val VPN_NOTIF_ID = 1001
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_VPN") {
            stopVpn()
            return START_NOT_STICKY
        }
        startForegroundNotification()
        startLocalDnsFilter()
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                VPN_CHANNEL_ID,
                "درع الحماية المحلي (VPN)",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, VPN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("درع رقيب مُفعّل 🛡️")
            .setContentText("يتم الآن حظر المواقع الإباحية والضارة محلياً بأمان.")
            .setOngoing(true)
            .build()

        startForeground(VPN_NOTIF_ID, notification)
    }

    private fun startLocalDnsFilter() {
        if (vpnInterface != null) return
        try {
            val builder = Builder()
                .setSession("RAQEEB Local Shield")
                .addAddress("10.0.0.2", 32)
                .addDnsServer("1.1.1.3") // Cloudflare Family DNS
                .addDnsServer("94.140.14.15") // AdGuard Family DNS
                .addRoute("1.1.1.3", 32)
                .addRoute("94.140.14.15", 32)

            vpnInterface = builder.establish()
            isRunning = (vpnInterface != null)
        } catch (e: Exception) {
            isRunning = false
        }
    }

    private fun stopVpn() {
        isRunning = false
        try {
            vpnInterface?.close()
        } catch (_: Exception) {}
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
