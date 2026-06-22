package com.Ejram.gameboost;
import android.app.admin.DeviceAdminReceiver; import android.content.Context; import android.content.Intent; import android.widget.Toast;
public class GuardianDeviceAdmin extends DeviceAdminReceiver {
    @Override public void onEnabled(Context context, Intent intent) { super.onEnabled(context, intent); Toast.makeText(context, "تم تفعيل حماية النظام", Toast.LENGTH_SHORT).show(); }
    @Override public CharSequence onDisableRequested(Context context, Intent intent) { return "تحذير: إلغاء التفعيل سيؤدي لبطء في الألعاب!"; }
}
