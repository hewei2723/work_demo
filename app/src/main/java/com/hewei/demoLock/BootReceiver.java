package com.hewei.demoLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "收到广播: " + action);

        // 监听开机完成
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "设备启动完成，启动锁屏服务");

            // 启动锁屏服务
            Intent serviceIntent = new Intent(context, LockScreenService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

            Log.d(TAG, "锁屏服务已自动启动");
        }
    }
}
