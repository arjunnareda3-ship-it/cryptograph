package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import java.util.Collections

object NetworkInspector {

    data class NetworkDetails(
        val ipAddress: String,
        val interfaceName: String,
        val connectionType: String,
        val isConnected: Boolean,
        val isVpn: Boolean
    )

    data class PortScanResult(
        val port: Int,
        val service: String,
        val isOpen: Boolean,
        val description: String,
        val riskLevel: String // SAFE, MEDIUM, HIGH
    )

    data class PingResult(
        val host: String,
        val isReachable: Boolean,
        val latencyMs: Long,
        val ipAddress: String?
    )

    fun getNetworkDetails(context: Context): NetworkDetails {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(activeNetwork)

        val isConnected = caps != null && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true

        val connectionType = when {
            caps == null -> "Disconnected"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular (Mobile Data)"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN Active"
            else -> "Other"
        }

        var ip = "Unavailable"
        var ifaceName = "None"

        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (iface in interfaces) {
                if (iface.isUp && !iface.isLoopback) {
                    val addrs = Collections.list(iface.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress && addr.hostAddress?.indexOf(':') == -1) {
                            ip = addr.hostAddress ?: "Unknown"
                            ifaceName = iface.displayName ?: iface.name
                            break
                        }
                    }
                }
                if (ip != "Unavailable") break
            }
        } catch (_: Exception) {}

        return NetworkDetails(
            ipAddress = ip,
            interfaceName = ifaceName,
            connectionType = connectionType,
            isConnected = isConnected,
            isVpn = isVpn
        )
    }

    private val COMMON_PORTS = listOf(
        PortInfo(21, "FTP", "File Transfer Protocol (often unencrypted)", "HIGH"),
        PortInfo(22, "SSH", "Secure Shell remote administration", "MEDIUM"),
        PortInfo(23, "Telnet", "Unencrypted remote terminal (cleartext passwords!)", "HIGH"),
        PortInfo(25, "SMTP", "Simple Mail Transfer Protocol", "MEDIUM"),
        PortInfo(53, "DNS", "Domain Name System resolution", "SAFE"),
        PortInfo(80, "HTTP", "Standard World Wide Web (Cleartext)", "MEDIUM"),
        PortInfo(110, "POP3", "Post Office Protocol (Email retrieval)", "MEDIUM"),
        PortInfo(143, "IMAP", "Internet Message Access Protocol", "MEDIUM"),
        PortInfo(443, "HTTPS", "Encrypted Web (TLS/SSL)", "SAFE"),
        PortInfo(3306, "MySQL", "Database Port (Should not be publicly exposed)", "HIGH"),
        PortInfo(8080, "HTTP-Alt", "Web Proxy / Secondary HTTP Server", "MEDIUM"),
        PortInfo(8443, "HTTPS-Alt", "Secondary SSL Web Service", "SAFE")
    )

    private data class PortInfo(val port: Int, val name: String, val desc: String, val risk: String)

    suspend fun scanTargetPorts(targetHost: String, timeoutMs: Int = 400): List<PortScanResult> =
        withContext(Dispatchers.IO) {
            val host = if (targetHost.isBlank()) "127.0.0.1" else targetHost.trim()
            val jobs = COMMON_PORTS.map { info ->
                async {
                    var isOpen = false
                    try {
                        Socket().use { socket ->
                            socket.connect(InetSocketAddress(host, info.port), timeoutMs)
                            isOpen = true
                        }
                    } catch (_: Exception) {
                        isOpen = false
                    }
                    PortScanResult(
                        port = info.port,
                        service = info.name,
                        isOpen = isOpen,
                        description = info.desc,
                        riskLevel = info.risk
                    )
                }
            }
            jobs.awaitAll()
        }

    suspend fun pingHost(targetHost: String, timeoutMs: Int = 1000): PingResult =
        withContext(Dispatchers.IO) {
            val host = if (targetHost.isBlank()) "8.8.8.8" else targetHost.trim()
            val start = System.currentTimeMillis()
            var reachable = false
            var resolvedIp: String? = null

            try {
                val inet = InetAddress.getByName(host)
                resolvedIp = inet.hostAddress
                reachable = inet.isReachable(timeoutMs)
                if (!reachable) {
                    // Fallback socket test to port 80/443
                    try {
                        Socket().use { s ->
                            s.connect(InetSocketAddress(host, 443), timeoutMs)
                            reachable = true
                        }
                    } catch (_: Exception) {
                        try {
                            Socket().use { s ->
                                s.connect(InetSocketAddress(host, 80), timeoutMs)
                                reachable = true
                            }
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {
                reachable = false
            }

            val elapsed = System.currentTimeMillis() - start
            PingResult(
                host = host,
                isReachable = reachable,
                latencyMs = if (reachable) elapsed.coerceAtLeast(1) else 0,
                ipAddress = resolvedIp
            )
        }
}
