package com.Ejram.gameboost;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class GuardianDeviceAdmin extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        // عند تفعيلها، لن يتمكن الطفل من حذف التطبيق
        Toast.makeText(context, "تم تفعيل حماية الجهاز المتقدمة", Toast.LENGTH_SHORT).show();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        // رسالة ترهيب للطفل إذا حاول إلغاء الحماية
        return "تحذير: تعطيل هذه الحماية سيؤدي إلى إيقاف تسريع الألعاب وقد يسبب بطء النظام!";
    }
}
