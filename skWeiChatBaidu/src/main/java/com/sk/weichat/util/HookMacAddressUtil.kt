package com.sk.weichat.util

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object HookMacAddressUtils {
    /**
     * 缓存wifi信息
     */
    private var cacheWifiInfo: WifiInfo? = null

    /**
     * 是否hook mac地址标示
     */
    private var isHookMacAddress = true

    /**
     * hook mac地址为空字符串
     * @param context 上下文
     */
    @JvmStatic
    @SuppressLint("PrivateApi", "WifiManagerPotentialLeak")
    fun hookMacAddress(context: Context) {
        try {
            isHookMacAddress = true
            val iWifiManager = Class.forName("android.net.wifi.IWifiManager")
            val serviceField = WifiManager::class.java.getDeclaredField("mService")
            serviceField.isAccessible = true
            val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val realIwm = serviceField[wifi]
            //hook getConnectionInfo的mac地址为空字符串
            serviceField[wifi] = Proxy.newProxyInstance(iWifiManager.classLoader, arrayOf(iWifiManager), MacAddressInvocationHandler("getConnectionInfo", realIwm))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 关闭hook mac地址
     */
    @JvmStatic
    fun closeHookMacAddress() {
        isHookMacAddress = false
    }


    class MacAddressInvocationHandler(private val methodName: String, private val real: Any?) :
        InvocationHandler {

        override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
            return if (isHookMacAddress && methodName == method.name) {
                cacheWifiInfo?.let {
                    return it
                }
                var wifiInfo: WifiInfo? = null
                try {
                    val cls: Class<*> = WifiInfo::class.java
                    wifiInfo = cls.newInstance() as WifiInfo
                    val mMacAddressField = cls.getDeclaredField("mMacAddress")
                    mMacAddressField.isAccessible = true
                    mMacAddressField[wifiInfo] = ""
                    cacheWifiInfo = wifiInfo
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                wifiInfo!!
            } else {
                method.invoke(real, *args)
            }
        }
    }
}
