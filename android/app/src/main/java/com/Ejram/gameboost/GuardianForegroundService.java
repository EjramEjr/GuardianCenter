package com.Ejram.gameboost;
import android.app.*; import android.content.*; import android.os.*; import androidx.core.app.NotificationCompat;
public class GuardianForegroundService extends Service {
    @Override public void onCreate() { super.onCreate(); }
    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("guardian_foreground_channel", "System", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, "guardian_foreground_channel").setContentTitle("Guardian Center").setContentText("يعمل في الخلفية").setSmallIcon(android.R.drawable.ic_dialog_info).build();
        startForeground(1, notification); FirebaseSync.startListening(this); return START_STICKY;
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
