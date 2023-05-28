package com.rolangom.cmedicgt.shared

import android.content.Context
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.net.URL


fun readAssetsFile(context: Context, fileName: String): String {
    val fileContent = context.assets.open(fileName).bufferedReader().use { it.readText() }
    return fileContent
}

fun getLocalIpAddress(): String? {
    try {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val intf = en.nextElement()
            val enumIpAddr = intf.inetAddresses
            while (enumIpAddr.hasMoreElements()) {
                val inetAddress = enumIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    return inetAddress.getHostAddress()
                }
            }
        }
    } catch (ex: SocketException) {
        ex.printStackTrace()
    }
    return null
}

fun fetchPublicIPAddress(): String {
    val whatIsMyIPAddressAPIURL = "https://api.ipify.org/"
    val lines = URL(whatIsMyIPAddressAPIURL).openStream().use {
        it.bufferedReader().readLines()
    }
    return lines.first()
}