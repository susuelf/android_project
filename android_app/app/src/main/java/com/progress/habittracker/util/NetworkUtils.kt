package com.progress.habittracker.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * NetworkUtils - Hálózati segédfüggvények
 *
 * Funkciók:
 * - Gateway IP cím automatikus felismerése
 * - Backend URL generálása futásidőben
 * - Hálózati kapcsolat ellenőrzése
 */
object NetworkUtils {

    private const val TAG = "NetworkUtils"
    private const val BACKEND_PORT = 8080

    /**
     * Backend base URL lekérése dinamikusan
     *
     * Stratégia:
     * 1. Először megpróbálja a WiFi gateway IP-t használni
     * 2. Ha nincs WiFi, akkor a mobilnet gateway IP-t
     * 3. Ha egyik sem működik, visszaad egy fallback címet
     *
     * @param context Android Context
     * @return Backend base URL (pl. "http://192.168.1.100:8080/")
     */
    fun getBackendBaseUrl(context: Context): String {
        // 1. Próbáljuk meg a WiFi gateway IP-t
        val gatewayIp = getWifiGatewayIp(context)
        if (gatewayIp != null) {
            val url = "http://$gatewayIp:$BACKEND_PORT/"
            Log.d(TAG, "WiFi Gateway IP detected: $url")
            return url
        }

        // 2. Ha nincs WiFi, próbáljuk meg a device IP alapján kitalálni
        val deviceIp = getDeviceIpAddress()
        if (deviceIp != null) {
            // Például 172.16.0.218 -> gateway valószínűleg 172.16.0.1 vagy 172.16.201.1
            val gatewayGuess = guessGatewayFromDeviceIp(deviceIp)
            val url = "http://$gatewayGuess:$BACKEND_PORT/"
            Log.d(TAG, "Gateway IP guessed from device IP: $url")
            return url
        }

        // 3. Fallback - emulator esetén 10.0.2.2
        val fallbackUrl = "http://10.0.2.2:$BACKEND_PORT/"
        Log.w(TAG, "Using fallback URL: $fallbackUrl")
        return fallbackUrl
    }

    /**
     * WiFi gateway IP cím lekérése
     *
     * A DHCP gateway címét kéri le a WifiManager-ből.
     * Ez általában a router IP címe (pl. 192.168.1.1, 172.16.201.1)
     *
     * @param context Android Context
     * @return Gateway IP cím vagy null
     */
    private fun getWifiGatewayIp(context: Context): String? {
        return try {
            val wifiManager = context.applicationContext
                .getSystemService(Context.WIFI_SERVICE) as? WifiManager

            if (wifiManager?.isWifiEnabled == true) {
                val dhcpInfo = wifiManager.dhcpInfo
                val gateway = dhcpInfo.gateway

                if (gateway != 0) {
                    // Int to IP string conversion
                    val gatewayIp = String.format(
                        "%d.%d.%d.%d",
                        gateway and 0xff,
                        gateway shr 8 and 0xff,
                        gateway shr 16 and 0xff,
                        gateway shr 24 and 0xff
                    )

                    // Ellenőrizzük, hogy valid IP-e
                    if (gatewayIp != "0.0.0.0") {
                        return gatewayIp
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting WiFi gateway IP: ${e.message}")
            null
        }
    }

    /**
     * Device saját IP címének lekérése
     *
     * A device WiFi vagy mobilnet IP címét kéri le.
     *
     * @return Device IP cím vagy null
     */
    private fun getDeviceIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses

                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()

                    // Csak IPv4 címeket nézünk, és kizárjuk a loopback-et
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting device IP: ${e.message}")
        }
        return null
    }

    /**
     * Gateway IP cím kitalálása a device IP alapján
     *
     * Ha a device IP-je 172.16.0.218, akkor a gateway valószínűleg:
     * - 172.16.0.1 vagy
     * - 172.16.201.1 (WSL/Docker esetén)
     *
     * @param deviceIp Device IP cím
     * @return Gateway IP cím becslés
     */
    private fun guessGatewayFromDeviceIp(deviceIp: String): String {
        val parts = deviceIp.split(".")
        if (parts.size == 4) {
            // Próbáljuk meg a .1-es végződést
            return "${parts[0]}.${parts[1]}.${parts[2]}.1"
        }
        return deviceIp // Fallback: használjuk magát a device IP-t
    }

    /**
     * Hálózati kapcsolat ellenőrzése
     *
     * @param context Android Context
     * @return true ha van internet kapcsolat
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as? ConnectivityManager ?: return false

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
               capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
