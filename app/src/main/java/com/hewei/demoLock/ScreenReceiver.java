package com.hewei.demoLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";
    private static ScreenActionListener listener;

    public interface ScreenActionListener {
        void onScreenOn();
        void onScreenOff();
    }

    public static void setListener(ScreenActionListener l) {
        listener = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "收到广播: " + action);

        if (Intent.ACTION_SCREEN_ON.equals(action)) {
            Log.d(TAG, "屏幕点亮");
            if (listener != null) {
                listener.onScreenOn();
            }
            // 启动锁屏界面
            Intent lockIntent = new Intent(context, LockScreenActivity.class);
            lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            lockIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(lockIntent);

        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            Log.d(TAG, "屏幕关闭");
            if (listener != null) {
                listener.onScreenOff();
            }
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            Log.d(TAG, "用户解锁");
        }
    }
}
