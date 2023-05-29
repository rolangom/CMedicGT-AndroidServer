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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.rolangom.cmedicgt.domains.AppService
import com.rolangom.cmedicgt.domains.Http4KAppService
import com.rolangom.cmedicgt.domains.buildRealm
import com.rolangom.cmedicgt.domains.patients.RealmDBPatientRepo
import com.rolangom.cmedicgt.domains.visits.FlatRealmDBVisitsRepo
import com.rolangom.cmedicgt.shared.MissingParamsException
import io.realm.kotlin.mongodb.exceptions.SyncException
import io.realm.kotlin.mongodb.sync.SyncSession
import android.R as RR


class Http4KService: Service() {
    private var appService: AppService? = null

    companion object {
        private val CHANNEL_ID = "CMedicoGTServiceChannel"
        private val NOTIFICATION: Int = R.string.local_service_started
    }

    val port: Int?
        get() = appService?.getPort()

    fun onSyncError(session: SyncSession, error: SyncException) {
        Log.e(TAG(), "Http4KService onSyncError", error)
        // TODO try to show while on service
//        Toast.makeText(
//            this,
//            error.message,
//            Toast.LENGTH_SHORT,
//        ).show()
    }

    private fun initService(port: Int?) {
        if (port == null) throw MissingParamsException("Port")
        val realm = buildRealm(app, ::onSyncError)
        val patientRepo = RealmDBPatientRepo(realm, app)
        val visitsRepo = FlatRealmDBVisitsRepo(realm, app)
        appService = Http4KAppService(port, this, patientRepo, visitsRepo)
        appService?.startService()
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
        createNotificationChannel()
        Log.d(TAG(),"Http4KService serviceStated")
        return START_NOT_STICKY
    }

    private val mBinder: IBinder = LocalBinder()
    override fun onBind(p0: Intent?): IBinder = mBinder

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
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
            .setPriority(NotificationCompat.PRIORITY_MAX)
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

