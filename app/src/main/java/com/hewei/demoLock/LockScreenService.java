package com.hewei.demoLock;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class LockScreenService extends Service {
    private static final String TAG = "LockScreenService";
    private static final String CHANNEL_ID = "lockscreen_service_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static boolean isRunning = false;

    private ScreenReceiver screenReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "服务启动");
        isRunning = true;

        // 创建通知渠道（Android 8.0+）
        createNotificationChannel();

        // 启动为前台服务
        startForeground(NOTIFICATION_ID, createNotification());

        // 注册屏幕监听
        screenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenReceiver, filter);

        Log.d(TAG, "前台服务已启动，屏幕监听已注册");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 如果服务被杀死，自动重启
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "服务停止");
        isRunning = false;

        if (screenReceiver != null) {
            try {
                unregisterReceiver(screenReceiver);
            } catch (Exception e) {
                Log.e(TAG, "注销广播失败: " + e.getMessage());
            }
        }

        // 如果服务被意外杀死，尝试重启
        Log.d(TAG, "服务被销毁，将在5秒后尝试重启");
        restartService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "应用被移除，尝试保持服务运行");
        // 应用被移除时重启服务
        restartService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 创建通知渠道（Android 8.0+必需）
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "锁屏服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("保持锁屏服务在后台运行");
            channel.setShowBadge(false);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * 创建前台服务通知
     */
    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("锁屏服务运行中")
                .setContentText("点击可关闭通知")
                .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setAutoCancel(false);

        // 点击通知跳转到主界面
        Intent notificationIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(android.app.PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        ));

        return builder.build();
    }

    /**
     * 重启服务
     */
    private void restartService() {
        Intent restartIntent = new Intent(getApplicationContext(), LockScreenService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(restartIntent);
        } else {
            startService(restartIntent);
        }
    }

    /**
     * 检查服务是否在运行
     */
    public static boolean isRunning() {
        return isRunning;
    }

    /**
     * 设置服务运行状态
     */
    public static void setRunning(boolean running) {
        isRunning = running;
    }
}
