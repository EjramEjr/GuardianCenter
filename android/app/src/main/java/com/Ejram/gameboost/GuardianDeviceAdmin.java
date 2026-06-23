package com.Ejram.gameboost;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class GuardianDeviceAdmin extends DeviceAdminReceiver {
    @Override
    public void onEnabled(Context context, Intent intent) {
        Toast.makeText(context, "تم تفعيل صلاحيات الرقابة الأبوية بنجاح", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, "تحذير: تم إيقاف صلاحيات الرقابة الأبوية", Toast.LENGTH_SHORT).show();
    }
}
