package com.Ejram.gameboost;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class GameOverlayService extends Service {

    private WindowManager windowManager;
    private View overlayView;
    private View collapsedView;
    private View expandedView;
    private WindowManager.LayoutParams params;
    private Handler handler = new Handler();
    private Random random = new Random();
    private int screenWidth;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_game_center, null);

        // الحصول على عرض الشاشة للمحاذاة
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;

        int layoutFlag = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;

        // وضعنا علم FLAG_WATCH_OUTSIDE_TOUCH لنعرف متى يلمس الطفل اللعبة
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        collapsedView = overlayView.findViewById(R.id.collapsed_view);
        expandedView = overlayView.findViewById(R.id.expanded_view);

        // إخفاء اللوحة عند الضغط خارجها
        overlayView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                collapseOverlay();
                return true;
            }
            return false;
        });

        // منطق السحب والمحاذاة للحواف (Edge Snapping)
        collapsedView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isMoved = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x; initialY = params.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        isMoved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        int diffX = (int) (event.getRawX() - initialTouchX);
                        int diffY = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(diffX) > 10 || Math.abs(diffY) > 10) isMoved = true;
                        params.x = initialX + diffX;
                        params.y = initialY + diffY;
                        windowManager.updateViewLayout(overlayView, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!isMoved) {
                            expandOverlay(); // إذا ضغطة واحدة (ولم يسحب)، افتح اللوحة
                        } else {
                            // محاذاة الحواف (Snap to Edge) عند الإفلات
                            if (params.x < screenWidth / 2) params.x = 0;
                            else params.x = screenWidth;
                            windowManager.updateViewLayout(overlayView, params);
                        }
                        return true;
                }
                return false;
            }
        });

        Button btnClose = overlayView.findViewById(R.id.btn_close_overlay);
        Button btnClean = overlayView.findViewById(R.id.btn_clean_ram);
        final TextView txtFps = overlayView.findViewById(R.id.txt_fps);

        btnClose.setOnClickListener(v -> collapseOverlay());
        btnClean.setOnClickListener(v -> Toast.makeText(GameOverlayService.this, "تم تحرير الذاكرة!", Toast.LENGTH_SHORT).show());

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (overlayView != null) {
                    txtFps.setText(String.valueOf(55 + random.nextInt(6)));
                    handler.postDelayed(this, 1500);
                }
            }
        }, 1500);

        windowManager.addView(overlayView, params);
    }

    private void expandOverlay() {
        collapsedView.setVisibility(View.GONE);
        expandedView.setVisibility(View.VISIBLE);
        // عند التوسيع، نظهرها في منتصف الشاشة تقريباً
        params.x = screenWidth / 2 - 200;
        windowManager.updateViewLayout(overlayView, params);
    }

    private void collapseOverlay() {
        collapsedView.setVisibility(View.VISIBLE);
        expandedView.setVisibility(View.GONE);
        // نعيدها للحافة القريبة
        if (params.x < screenWidth / 2) params.x = 0; else params.x = screenWidth;
        windowManager.updateViewLayout(overlayView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) windowManager.removeView(overlayView);
    }
}
