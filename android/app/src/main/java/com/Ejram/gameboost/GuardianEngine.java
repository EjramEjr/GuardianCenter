package com.Ejram.gameboost;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.provider.Settings;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

@CapacitorPlugin(name = "GuardianEngine")
public class GuardianEngine extends Plugin {

    // 1. طلب إذن "الوصول لبيانات الاستخدام" (لمعرفة وقت الشاشة والتطبيقات)
    @PluginMethod
    public void requestUsagePermission(PluginCall call) {
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
            call.resolve();
        } catch (Exception e) { call.reject("Error"); }
    }

    // 2. طلب إذن "مدير الجهاز" بشفافية
    @PluginMethod
    public void requestAdminPermission(PluginCall call) {
        try {
            ComponentName compName = new ComponentName(getContext(), GuardianDeviceAdmin.class);
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "تطبيق Guardian: مطلوب لتفعيل الرقابة الأبوية الصارمة وحماية إعدادات الهاتف.");
            getActivity().startActivity(intent);
            call.resolve();
        } catch (Exception e) { call.reject("Error"); }
    }

    // 3. جلب حالة الجهاز (بطارية ورام)
    @PluginMethod
    public void getDeviceStats(PluginCall call) {
        try {
            // الرام
            ActivityManager actManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            long totalRam = memInfo.totalMem;
            long usedRam = totalRam - memInfo.availMem;
            int ramPercentage = (int) ((usedRam * 100) / totalRam);

            // البطارية
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = getContext().registerReceiver(null, ifilter);
            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
            float batteryPct = level * 100 / (float)scale;

            JSObject ret = new JSObject();
            ret.put("ram", ramPercentage);
            ret.put("battery", (int)batteryPct);
            call.resolve(ret);
        } catch (Exception e) { call.reject("Error"); }
    }
}
