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
        } catch (Exception e) {
            call.reject("Error fetching stats");
        }
    }

    @PluginMethod
    public void executeRealClean(PluginCall call) {
        try {
            ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
            PackageManager pm = getContext().getPackageManager();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

            int killedCount = 0;
            for (ApplicationInfo packageInfo : packages) {
                if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && 
                    !packageInfo.packageName.equals(getContext().getPackageName())) {
                    am.killBackgroundProcesses(packageInfo.packageName);
                    killedCount++;
                }
            }
            JSObject ret = new JSObject();
            ret.put("killedApps", killedCount);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Clean failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void requestDeepPermissions(PluginCall call) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("Permission request failed");
        }
    }
}
