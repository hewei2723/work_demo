package com.hewei.demoLock;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private MaterialButton btnStart;
    private MaterialButton btnStop;
    private MaterialButton btnPreview;
    private MaterialButton btnPromptFlow;
    private TextView tvStatus;
    private Handler timeHandler;
    private Runnable timeRunnable;
    private List<NotificationItem> notifications = new ArrayList<>();
    private int currentNotificationIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_control);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupButtons();
        updateServiceStatus();
    }

    private void initViews() {
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnPreview = findViewById(R.id.btnPreview);
        btnPromptFlow = findViewById(R.id.btnPromptFlow);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void setupButtons() {
        // 启动服务
        btnStart.setOnClickListener(v -> {
            // 检查悬浮窗权限
            if (!hasOverlayPermission()) {
                requestOverlayPermission();
                return;
            }

            // 启动锁屏服务（使用普通服务，不需要前台通知）
            Intent serviceIntent = new Intent(this, LockScreenService.class);
            startService(serviceIntent);

            Toast.makeText(this, "锁屏服务已启动", Toast.LENGTH_SHORT).show();
            updateServiceStatus();
        });

        // 停止服务
        btnStop.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, LockScreenService.class);
            stopService(serviceIntent);

            Toast.makeText(this, "锁屏服务已停止", Toast.LENGTH_SHORT).show();
            updateServiceStatus();
        });

        // 预览锁屏界面
        btnPreview.setOnClickListener(v -> {
            Intent previewIntent = new Intent(this, LockScreenActivity.class);
            startActivity(previewIntent);
        });

        // 查看提示词流程
        btnPromptFlow.setOnClickListener(v -> {
            Intent promptFlowIntent = new Intent(this, PromptFlowActivity.class);
            startActivity(promptFlowIntent);
        });
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(this, "请授予悬浮窗权限", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(android.net.Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private void updateServiceStatus() {
        boolean isRunning = LockScreenService.isRunning();
        if (isRunning) {
            tvStatus.setText("服务运行中");
            tvStatus.setTextColor(getColor(android.R.color.holo_green_dark));
            btnStart.setEnabled(false);
            btnStop.setEnabled(true);
        } else {
            tvStatus.setText("服务未启动");
            tvStatus.setTextColor(getColor(android.R.color.darker_gray));
            btnStart.setEnabled(true);
            btnStop.setEnabled(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeHandler != null && timeRunnable != null) {
            timeHandler.removeCallbacks(timeRunnable);
        }
    }

    // 通知数据类
    private static class NotificationItem {
        String title;
        String text;
        String time;
        int iconRes;

        NotificationItem(String title, String text, String time, int iconRes) {
            this.title = title;
            this.text = text;
            this.time = time;
            this.iconRes = iconRes;
        }
    }
}
