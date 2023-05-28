package com.rolangom.cmedicgt.domains

interface AppService {
    fun startService()
    fun stopService()

    fun getPort(): Int
}