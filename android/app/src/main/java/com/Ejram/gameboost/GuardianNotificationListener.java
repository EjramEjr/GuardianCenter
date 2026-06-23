package com.Ejram.gameboost;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class GuardianNotificationListener extends NotificationListenerService {
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // تم إيقاف فايربيس هنا مؤقتاً لتخطي خطأ البناء (Build)
        // سيتم معالجة الإشعارات عبر الجافاسكريبت لاحقاً
    }
}
