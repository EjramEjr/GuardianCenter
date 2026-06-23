package com.Ejram.gameboost;

import android.content.Intent;
import android.provider.Settings;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "GuardianEngine")
public class GuardianEngine extends Plugin {

    @PluginMethod
    public void openAccessibilitySettings(PluginCall call) {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            call.resolve();
        } catch (Exception e) { call.reject("Error"); }
    }

    @PluginMethod
    public void openDeviceAdminSettings(PluginCall call) {
        try {
            ComponentName compName = new ComponentName(getContext(), GuardianDeviceAdmin.class);
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "تفعيل رقابة Guardian Family الصارمة");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            call.resolve();
        } catch (Exception e) { call.reject("Error"); }
    }
}
