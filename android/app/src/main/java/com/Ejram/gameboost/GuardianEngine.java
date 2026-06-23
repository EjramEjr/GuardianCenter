package com.Ejram.gameboost;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.provider.Settings;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "GuardianEngine")
public class GuardianEngine extends Plugin {

    @PluginMethod
    public void forceAdminPermission(PluginCall call) {
        try {
            ComponentName compName = new ComponentName(getContext(), GuardianDeviceAdmin.class);
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Guardian Center: مطلوب لمنع مسح التطبيق وتفعيل الحماية");
            getActivity().startActivity(intent);
            call.resolve();
        } catch (Exception e) { call.reject(e.getMessage()); }
    }

    @PluginMethod
    public void startScreenCapture(PluginCall call) {
        try {
            MediaProjectionManager mpm = (MediaProjectionManager) getContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            if (mpm != null) {
                Intent intent = mpm.createScreenCaptureIntent();
                // نطلب الموافقة ونرسلها لـ MainActivity برقم 1001
                getActivity().startActivityForResult(intent, 1001);
            }
            call.resolve();
        } catch (Exception e) { call.reject(e.getMessage()); }
    }
}
