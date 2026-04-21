package com.hewei.demoLock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PermissionsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnRequestOverlay;
    private MaterialButton btnRequestBattery;
    private MaterialButton btnRequestAutoStart;
    private MaterialButton btnRequestLockTask;
    private MaterialButton btnGrantAll;
    private TextView tvOverlayStatus;
    private TextView tvBatteryStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        initViews();
        setupListeners();
        checkAllPermissions();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnRequestOverlay = findViewById(R.id.btnRequestOverlay);
        btnRequestBattery = findViewById(R.id.btnRequestBattery);
        btnRequestAutoStart = findViewById(R.id.btnRequestAutoStart);
        btnRequestLockTask = findViewById(R.id.btnRequestLockTask);
        btnGrantAll = findViewById(R.id.btnGrantAll);
        tvOverlayStatus = findViewById(R.id.tvOverlayStatus);
        tvBatteryStatus = findViewById(R.id.tvBatteryStatus);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        // 悬浮窗权限
        btnRequestOverlay.setOnClickListener(v -> requestOverlayPermission());

        // 电池优化
        btnRequestBattery.setOnClickListener(v -> requestIgnoreBatteryOptimization());

        // 自启动权限（跳转到各品牌设置）
        btnRequestAutoStart.setOnClickListener(v -> requestAutoStartPermission());

        // 后台进程锁定
        btnRequestLockTask.setOnClickListener(v -> requestLockTaskPermission());

        // 一键检查
        btnGrantAll.setOnClickListener(v -> checkAllPermissions());
    }

    /**
     * 检查所有权限状态
     */
    private void checkAllPermissions() {
        boolean overlayGranted = checkOverlayPermission();
        boolean batteryOptimized = checkBatteryOptimization();

        // 更新悬浮窗状态
        updatePermissionStatus(tvOverlayStatus, overlayGranted);

        // 更新电池优化状态
        updatePermissionStatus(tvBatteryStatus, batteryOptimized);

        // 提示未授权的权限
        List<String> missingPermissions = new ArrayList<>();
        if (!overlayGranted) missingPermissions.add("悬浮窗权限");
        if (!batteryOptimized) missingPermissions.add("忽略电池优化");

        if (missingPermissions.isEmpty()) {
            Toast.makeText(this, "所有核心权限已授予！", Toast.LENGTH_SHORT).show();
        } else {
            String message = "还需要授予：\n" + String.join("\n", missingPermissions) +
                    "\n\n自启动和进程锁定需手动在设置中开启";
            new AlertDialog.Builder(this)
                    .setTitle("权限提醒")
                    .setMessage(message)
                    .setPositiveButton("知道了", null)
                    .show();
        }
    }

    /**
     * 更新权限状态显示
     */
    private void updatePermissionStatus(TextView textView, boolean granted) {
        if (granted) {
            textView.setText("已授予");
            textView.setTextColor(getColor(android.R.color.holo_green_dark));
            textView.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
            textView.getBackground().setTint(getColor(android.R.color.holo_green_light));
        } else {
            textView.setText("未授予");
            textView.setTextColor(getColor(android.R.color.holo_red_dark));
            textView.setBackground(getDrawable(android.R.drawable.dialog_holo_light_frame));
            textView.getBackground().setTint(getColor(android.R.color.holo_red_light));
        }
    }

    // ==================== 悬浮窗权限 ====================

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "请授予悬浮窗权限", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "当前版本不需要此权限", Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== 电池优化 ====================

    private boolean checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(getPackageName());
        }
        return true;
    }

    private void requestIgnoreBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "请选择「不优化」", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "当前版本不需要此权限", Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== 自启动权限 ====================

    private void requestAutoStartPermission() {
        try {
            // 获取设备品牌
            String brand = Build.BRAND.toLowerCase();

            Intent intent = null;

            // 根据不同品牌跳转到对应的自启动设置页面
            if (brand.contains("xiaomi") || brand.contains("redmi")) {
                // 小米/红米
                intent = new Intent().setComponent(new android.content.ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if (brand.contains("huawei") || brand.contains("honor")) {
                // 华为/荣耀
                intent = new Intent().setComponent(new android.content.ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"));
            } else if (brand.contains("oppo") || brand.contains("realme") || brand.contains("oneplus")) {
                // OPPO/Realme/一加
                intent = new Intent().setComponent(new android.content.ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.startupapp.StartupAppListActivity"));
            } else if (brand.contains("vivo")) {
                // vivo
                intent = new Intent().setComponent(new android.content.ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.appengine.activity.VivoPermissionManagerActivity"));
            } else if (brand.contains("samsung")) {
                // 三星
                intent = new Intent().setComponent(new android.content.ComponentName(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"));
            }

            if (intent != null) {
                startActivity(intent);
            } else {
                // 通用设置页面
                showGenericAutoStartGuide();
            }

        } catch (Exception e) {
            // 如果跳转失败，显示通用指引
            showGenericAutoStartGuide();
        }
    }

    private void showGenericAutoStartGuide() {
        new AlertDialog.Builder(this)
                .setTitle("如何开启自启动")
                .setMessage("请按以下步骤操作：\n\n" +
                        "1. 打开手机「设置」\n" +
                        "2. 找到「应用管理」或「应用」\n" +
                        "3. 搜索「锁屏应用」或「DemoLOCK」\n" +
                        "4. 进入后找到「自启动」或「后台活动」\n" +
                        "5. 开启允许自启动")
                .setPositiveButton("知道了", null)
                .show();
    }

    // ==================== 后台进程锁定 ====================

    private void requestLockTaskPermission() {
        new AlertDialog.Builder(this)
                .setTitle("后台进程锁定")
                .setMessage("此功能需在手机设置中手动开启：\n\n" +
                        "1. 打开手机「设置」\n" +
                        "2. 进入「应用管理」\n" +
                        "3. 找到「锁屏应用」\n" +
                        "4. 进入「应用启动管理」\n" +
                        "5. 将此应用设为「手动管理」并允许后台活动")
                .setPositiveButton("知道了", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 返回时重新检查权限状态
        checkAllPermissions();
    }
}
