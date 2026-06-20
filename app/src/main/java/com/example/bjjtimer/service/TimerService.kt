package com.example.bjjtimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.bjjtimer.MainActivity
import com.example.bjjtimer.R
import com.example.bjjtimer.audio.SoundManager
import com.example.bjjtimer.data.model.TimerConfig
import com.example.bjjtimer.data.model.TimerPhase
import com.example.bjjtimer.data.model.TimerState
import com.example.bjjtimer.timer.TimerEngine
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.StateFlow

class TimerService : Service() {

    companion object {
        const val CHANNEL_ID = "bjj_timer_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PAUSE = "com.example.bjjtimer.PAUSE"
        const val ACTION_RESUME = "com.example.bjjtimer.RESUME"
        const val ACTION_STOP = "com.example.bjjtimer.STOP"
        const val ACTION_SKIP = "com.example.bjjtimer.SKIP"

        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, TimerService::class.java)
            context.stopService(intent)
        }
    }

    private val binder = TimerBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var timerEngine: TimerEngine
    lateinit var soundManager: SoundManager
        private set

    private var wakeLock: PowerManager.WakeLock? = null
    private var notificationUpdateJob: Job? = null

    val timerState: StateFlow<TimerState> get() = timerEngine.state

    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        timerEngine = TimerEngine(serviceScope)
        soundManager = SoundManager(this)
        soundManager.initialize()

        // Listen for timer events and play sounds
        serviceScope.launch {
            timerEngine.events.collect { event ->
                soundManager.handleEvent(event, timerEngine.state.value.config)
            }
        }

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> timerEngine.pause()
            ACTION_RESUME -> timerEngine.resume()
            ACTION_STOP -> {
                timerEngine.stop()
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }
            ACTION_SKIP -> timerEngine.skipToNext()
            else -> {
                // Normal start - promote to foreground
                try {
                    val notification = buildNotification(timerEngine.state.value)
                    ServiceCompat.startForeground(
                        this,
                        NOTIFICATION_ID,
                        notification,
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                        } else {
                            0
                        }
                    )
                    acquireWakeLock()
                    startNotificationUpdates()
                } catch (e: Exception) {
                    // Handle gracefully
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    fun configureTimer(config: TimerConfig) {
        timerEngine.configure(config)
    }

    fun startTimer() {
        timerEngine.start()
        startNotificationUpdates()
    }

    fun pauseTimer() = timerEngine.pause()
    fun resumeTimer() = timerEngine.resume()
    fun togglePause() = timerEngine.togglePause()
    fun skipToNext() = timerEngine.skipToNext()

    fun stopTimer() {
        timerEngine.stop()
    }

    private fun startNotificationUpdates() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = serviceScope.launch {
            timerEngine.state.collect { state ->
                val notification = buildNotification(state)
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, notification)

                if (state.phase == TimerPhase.COMPLETED || state.phase == TimerPhase.IDLE) {
                    delay(2000) // Brief delay before stopping
                    if (state.phase == TimerPhase.COMPLETED) {
                        // Keep notification showing completion
                    }
                }
            }
        }
    }

    private fun buildNotification(state: TimerState): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPi = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(buildNotificationTitle(state))
            .setContentText(buildNotificationText(state))
            .setContentIntent(openPi)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        // Add action buttons based on state
        if (state.isRunning) {
            builder.addAction(
                R.drawable.ic_pause, "Pause",
                buildActionPi(ACTION_PAUSE)
            )
        } else if (state.isPaused) {
            builder.addAction(
                R.drawable.ic_play, "Resume",
                buildActionPi(ACTION_RESUME)
            )
        }

        if (state.isRunning || state.isPaused) {
            builder.addAction(
                R.drawable.ic_skip, "Skip",
                buildActionPi(ACTION_SKIP)
            )
            builder.addAction(
                R.drawable.ic_stop, "Stop",
                buildActionPi(ACTION_STOP)
            )
        }

        return builder.build()
    }

    private fun buildNotificationTitle(state: TimerState): String {
        return when (state.phase) {
            TimerPhase.ROUND_RUNNING, TimerPhase.PAUSED_ROUND ->
                "Round ${state.currentRound}/${state.totalRounds}"
            TimerPhase.REST_RUNNING, TimerPhase.PAUSED_REST ->
                "Rest - Round ${state.currentRound}/${state.totalRounds}"
            TimerPhase.COMPLETED -> "Session Complete!"
            else -> "BJJ Timer Ready"
        }
    }

    private fun buildNotificationText(state: TimerState): String {
        return when (state.phase) {
            TimerPhase.COMPLETED -> "${state.totalRounds} rounds completed"
            TimerPhase.IDLE -> "Tap to configure"
            else -> state.formattedTime + if (state.isPaused) " (Paused)" else ""
        }
    }

    private fun buildActionPi(action: String): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "BJJ Timer",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Timer notifications for BJJ round tracking"
            setShowBadge(false)
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "BJJTimer::TimerWakeLock"
        ).apply {
            acquire(60 * 60 * 1000L) // Max 1 hour
        }
    }

    private fun stopForegroundAndSelf() {
        notificationUpdateJob?.cancel()
        releaseWakeLock()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }

    override fun onDestroy() {
        notificationUpdateJob?.cancel()
        serviceScope.cancel()
        soundManager.release()
        releaseWakeLock()
        super.onDestroy()
    }
}
