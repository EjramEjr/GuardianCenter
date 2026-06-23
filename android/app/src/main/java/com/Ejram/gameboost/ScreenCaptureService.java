package com.Ejram.gameboost;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ScreenCaptureService extends Service {
    private static final String TAG = "ScreenCapture";
    private static final String CHANNEL_ID = "screen_capture_channel";
    public static final int REQUEST_CODE = 1001;

    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler handler;
    private FirebaseFirestore db;
    private int screenWidth, screenHeight, screenDensity;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        db = FirebaseFirestore.getInstance();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        screenDensity = metrics.densityDpi;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Guardian Center")
            .setContentText("بث الشاشة نشط")
            .setSmallIcon(android.R.drawable.ic_menu_slideshow)
            .setOngoing(true)
            .build();

        startForeground(2, notification);

        int resultCode = intent.getIntExtra("resultCode", Activity.RESULT_CANCELED);
        Intent data = intent.getParcelableExtra("data");

        if (data != null) {
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            startCapture();
        }

        return START_STICKY;
    }

    private void startCapture() {
        imageReader = ImageReader.newInstance(screenWidth, screenHeight, PixelFormat.RGBA_8888, 2);

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "GuardianScreenCapture",
            screenWidth, screenHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.getSurface(), null, handler
        );

        // التقاط صورة كل ثانيتين وإرسالها للفايربيس
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                captureScreen();
                handler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    private void captureScreen() {
        Image image = imageReader.acquireLatestImage();
        if (image == null) return;

        try {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * screenWidth;

            Bitmap bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride, screenHeight, 
                Bitmap.Config.ARGB_8888
            );
            bitmap.copyPixelsFromBuffer(buffer);

            // ضغط الصورة وتحويلها لنص Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            // إرسال لفايربيس
            Map<String, Object> data = new HashMap<>();
            data.put("image", base64Image);
            data.put("timestamp", System.currentTimeMillis());
            data.put("width", screenWidth);
            data.put("height", screenHeight);

            db.collection("screen_captures").document("latest").set(data);

            bitmap.recycle();

        } catch (Exception e) {
            Log.e(TAG, "Capture error: " + e.getMessage());
        } finally {
            image.close();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Screen Capture", NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("بث الشاشة للأب");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (virtualDisplay != null) virtualDisplay.release();
        if (mediaProjection != null) mediaProjection.stop();
        if (imageReader != null) imageReader.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
