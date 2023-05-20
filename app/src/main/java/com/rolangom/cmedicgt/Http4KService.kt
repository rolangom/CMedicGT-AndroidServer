package com.rolangom.cmedicgt

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rolangom.cmedicgt.domains.Http4KAppService
import com.rolangom.cmedicgt.domains.patients.InMemoryPatientRepo
import com.rolangom.cmedicgt.shared.MissingParamsException
import android.R as RR


class Http4KService: Service() {
    private var appService: Http4KAppService? = null

    companion object {
        private val CHANNEL_ID = "CMedicoGTServiceChannel"
        private val NOTIFICATION: Int = R.string.local_service_started
    }

    val port: Int?
        get() = appService?.port

    private fun initService(port: Int?) {
        if (port == null) throw MissingParamsException("Port")
        appService = Http4KAppService(port, this, InMemoryPatientRepo())
    }

    inner class LocalBinder : Binder() {
        val service: Http4KService
            get() = this@Http4KService
    }

    override fun onDestroy() {
        appService?.stopService()
        Log.d(TAG(), "Http4KService onDestroy")
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val maybePort = intent?.getIntExtra("port", 8080)
        initService(maybePort)
        Log.d(TAG(),"Http4KService serviceStated")
        createNotificationChannel()
        appService?.startService()
        return START_NOT_STICKY
    }

    private val mBinder: IBinder = LocalBinder()
    override fun onBind(p0: Intent?): IBinder = mBinder

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }


        val text = getText(R.string.local_service_started)
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CMedico-GT Service")
            .setTicker(text)  // the status text
            .setWhen(System.currentTimeMillis())  // the time stamp
            .setContentText(text)  // the contents of the entry
            .setSmallIcon(RR.drawable.btn_radio)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(NOTIFICATION, notification)
    }
}

