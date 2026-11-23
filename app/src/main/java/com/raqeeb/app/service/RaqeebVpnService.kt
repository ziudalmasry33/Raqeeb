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
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

class RaqeebVpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    @Volatile private var activeThread: Thread? = null

    companion object {
        @Volatile var isRunning: Boolean = false
        val customBlockedDomains = mutableSetOf<String>()
        private const val VPN_CHANNEL_ID = "raqeeb_dns_shield"
        private const val VPN_NOTIF_ID = 1001
        private const val UPSTREAM_DNS = "1.1.1.3" // Cloudflare Family DNS (سريع جداً ويحجب الضار تلقائياً)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_VPN") {
            stopVpn()
            return START_NOT_STICKY
        }
        startForegroundNotification()
        startFastDnsProxy()
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                VPN_CHANNEL_ID,
                "درع النقاء الرقمي",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, VPN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("درع النقاء مُفعّل 🌿")
            .setContentText("تصفية ذكية وفائقة السرعة للنطاقات الضارة.")
            .setOngoing(true)
            .build()

        startForeground(VPN_NOTIF_ID, notification)
    }

    private fun startFastDnsProxy() {
        if (vpnInterface != null) return
        try {
            // توجيه استعلامات DNS فقط (10.0.0.1) عبر النفق، وترك باقي الإنترنت مباشراً بسرعة الهاتف الكاملة
            val builder = Builder()
                .setSession("RAQEEB Purity Shield")
                .addAddress("10.0.0.2", 32)
                .addDnsServer("10.0.0.1")
                .addRoute("10.0.0.1", 32)

            val pfd = builder.establish() ?: return
            vpnInterface = pfd
            isRunning = true

            activeThread = Thread {
                val input = FileInputStream(pfd.fileDescriptor)
                val output = FileOutputStream(pfd.fileDescriptor)
                val packet = ByteArray(2048)
                val upstreamAddr = InetAddress.getByName(UPSTREAM_DNS)

                while (isRunning && !Thread.currentThread().isInterrupted) {
                    val length = try { input.read(packet) } catch (_: Exception) { -1 }
                    if (length <= 0) continue

                    // التحقق من أنها حزمة IPv4 UDP متجهة إلى منفذ 53 (DNS)
                    val version = (packet[0].toInt() shr 4) and 0xF
                    val protocol = packet[9].toInt() and 0xFF
                    if (version != 4 || protocol != 17) continue

                    val ihl = (packet[0].toInt() and 0x0F) * 4
                    if (length < ihl + 8) continue

                    val dstPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)
                    if (dstPort != 53) continue

                    val dnsOffset = ihl + 8
                    val dnsLen = length - dnsOffset
                    if (dnsLen <= 12) continue

                    val queriedDomain = extractDomainName(packet, dnsOffset + 12, length).lowercase()
                    val isCustomBlocked = customBlockedDomains.any {
                        queriedDomain == it || queriedDomain.endsWith(".$it")
                    }

                    val dnsResponse: ByteArray = if (isCustomBlocked) {
                        buildNxDomainResponse(packet, dnsOffset, dnsLen)
                    } else {
                        forwardDnsQuery(packet, dnsOffset, dnsLen, upstreamAddr) ?: continue
                    }

                    val ipResponse = buildUdpIpPacket(packet, ihl, dnsResponse)
                    synchronized(output) {
                        output.write(ipResponse)
                    }
                }
            }.apply { start() }
        } catch (_: Exception) {
            isRunning = false
        }
    }

    private fun extractDomainName(buf: ByteArray, start: Int, limit: Int): String {
        val sb = StringBuilder()
        var pos = start
        while (pos < limit) {
            val len = buf[pos].toInt() and 0xFF
            if (len == 0) break
            if (pos + 1 + len > limit) break
            if (sb.isNotEmpty()) sb.append('.')
            for (i in 1..len) {
                sb.append((buf[pos + i].toInt() and 0xFF).toChar())
            }
            pos += len + 1
        }
        return sb.toString()
    }

    private fun buildNxDomainResponse(req: ByteArray, offset: Int, len: Int): ByteArray {
        val resp = req.copyOfRange(offset, offset + len)
        // ضبط بت الاستجابة QR=1 و RCODE=3 (NXDOMAIN - النطاق محجوب)
        resp[2] = (resp[2].toInt() or 0x80).toByte()
        resp[3] = ((resp[3].toInt() and 0xF0) or 0x03).toByte()
        return resp
    }

    private fun forwardDnsQuery(req: ByteArray, offset: Int, len: Int, upstream: InetAddress): ByteArray? {
        var socket: DatagramSocket? = null
        return try {
            socket = DatagramSocket()
            protect(socket) // استثناء السوكيت من النفق لمنع الحلقة اللانهائية وضمان أقصى سرعة
            socket.soTimeout = 1500
            val outPacket = DatagramPacket(req, offset, len, upstream, 53)
            socket.send(outPacket)

            val recvBuf = ByteArray(1500)
            val inPacket = DatagramPacket(recvBuf, recvBuf.size)
            socket.receive(inPacket)
            recvBuf.copyOf(inPacket.length)
        } catch (_: Exception) {
            null
        } finally {
            socket?.close()
        }
    }

    private fun buildUdpIpPacket(orig: ByteArray, ihl: Int, udpPayload: ByteArray): ByteArray {
        val totalLen = 20 + 8 + udpPayload.size
        val out = ByteBuffer.allocate(totalLen)

        // IPv4 Header
        out.put(0x45.toByte())
        out.put(0x00.toByte())
        out.putShort(totalLen.toShort())
        out.putShort(0)
        out.putShort(0x4000.toShort())
        out.put(64.toByte())
        out.put(17.toByte()) // UDP
        out.putShort(0) // Checksum placeholder
        // عكس عنوان المصدر والوجهة
        out.put(orig, 16, 4)
        out.put(orig, 12, 4)

        val ipChecksum = computeChecksum(out.array(), 0, 20)
        out.putShort(10, ipChecksum)

        // UDP Header (عكس المنافذ)
        out.put(orig[ihl + 2])
        out.put(orig[ihl + 3])
        out.put(orig[ihl])
        out.put(orig[ihl + 1])
        out.putShort((8 + udpPayload.size).toShort())
        out.putShort(0) // UDP checksum optional in IPv4

        out.put(udpPayload)
        return out.array()
    }

    private fun computeChecksum(buf: ByteArray, offset: Int, len: Int): Short {
        var sum = 0
        var i = offset
        while (i < offset + len) {
            val word = ((buf[i].toInt() and 0xFF) shl 8) or (buf[i + 1].toInt() and 0xFF)
            sum += word
            i += 2
        }
        while ((sum shr 16) != 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return sum.inv().toShort()
    }

    private fun stopVpn() {
        isRunning = false
        activeThread?.interrupt()
        activeThread = null
        try { vpnInterface?.close() } catch (_: Exception) {}
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
