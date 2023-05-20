package com.rolangom.cmedicgt.presentations.home

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.rolangom.cmedicgt.Http4KService
import com.rolangom.cmedicgt.R
import com.rolangom.cmedicgt.TAG


class ServiceBinding(
    private val context: Context,
    private val reportIsConnected: () -> Unit,
    private val reportNotConnected: () -> Unit,
) {

    private var mShouldUnbind = false
    private var mBoundService: Http4KService? = null

    val port: Int?
        get() = mBoundService?.port

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG(), "onServiceConnected")
            mBoundService = (service as Http4KService.LocalBinder).service
            reportIsConnected()
            Toast.makeText(
                context, R.string.local_service_connected,
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.d(TAG(), "onServiceDisconnected")
            mBoundService = null
            reportNotConnected()
            Toast.makeText(
                context, R.string.local_service_disconnected,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun doBindService() {
        if (context.bindService(
                Intent(context, Http4KService::class.java),
                mConnection, ComponentActivity.BIND_AUTO_CREATE
            )
        ) {
            mShouldUnbind = true
        } else {
            Log.e(
                TAG(), "Error: The requested service doesn't " +
                        "exist, or this client isn't allowed access to it."
            )
        }
    }

    fun doUnbindService() {
        if (mShouldUnbind) {
            // Release information about the service's state.
            context.unbindService(mConnection)
            mShouldUnbind = false
        }
    }

    fun isServiceAlive(): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        // TODO check for other not deprecated solution
        for (service in manager!!.getRunningServices(Integer.MAX_VALUE)) {
            if (Http4KService::class.java.name.equals(service.service.className)) {
                return true
            }
        }
        return false
    }


    fun startHTTP4KServer(port: Int) {
        val serviceIntent = Intent(context, Http4KService::class.java)
        serviceIntent.putExtra("port", port)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopHTTP4KServer() {
        val serviceIntent = Intent(context, Http4KService::class.java)
        context.stopService(serviceIntent)
    }

}