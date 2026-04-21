package com.hewei.demoLock;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LockScreenActivity extends AppCompatActivity {

    private TextView tvTime;
    private TextView tvDate;
    private LinearLayout notificationContainer;
    private UnlockSliderView unlockSlider;
    private Handler timeHandler;
    private Runnable timeRunnable;
    private List<NotificationItem> notifications = new ArrayList<>();
    private int currentNotificationIndex = 0;
    private boolean isServiceRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置在锁屏上显示的标志
        setupLockScreenFlags();

        setContentView(R.layout.activity_main);

        initViews();
        setupTimeUpdate();

        // 检查服务是否在运行
        isServiceRunning = LockScreenService.isRunning();
        if (isServiceRunning) {
            loadSampleNotifications();
            startNotificationAnimation();
        }
    }

    private void setupLockScreenFlags() {
        // 在锁屏上显示（Android 10+ 使用新方式）
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            // 旧版本使用标志位
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }

        // 全屏显示
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 保持屏幕亮起
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 隐藏系统UI
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void initViews() {
        tvTime = findViewById(R.id.tvTime);
        tvDate = findViewById(R.id.tvDate);
        notificationContainer = findViewById(R.id.notificationContainer);
        unlockSlider = findViewById(R.id.unlockSlider);

        // 设置解锁监听
        unlockSlider.setOnUnlockListener(new UnlockSliderView.OnUnlockListener() {
            @Override
            public void onUnlock() {
                performUnlock();
            }
        });
    }

    private void performUnlock() {
        // 淡出整个界面
        View decorView = getWindow().getDecorView();
        decorView.animate()
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // 完成解锁，关闭 Activity
                        finish();
                        // 添加过渡动画
                        overridePendingTransition(0, android.R.anim.fade_out);
                    }
                })
                .start();
    }

    private void setupTimeUpdate() {
        timeHandler = new Handler(Looper.getMainLooper());
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                updateTime();
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void updateTime() {
        Date now = new Date();

        // 更新时间 HH:mm
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        tvTime.setText(timeFormat.format(now));

        // 更新日期 星期X，X月X日
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE，M月d日", Locale.CHINA);
        tvDate.setText(dateFormat.format(now));
    }

    private void loadSampleNotifications() {
        // 示例通知数据
        notifications.add(new NotificationItem("微信", "张三: 在吗？有个事想请教你", "刚刚", android.R.drawable.ic_dialog_email));
        notifications.add(new NotificationItem("QQ", "您有3条新消息", "5分钟前", android.R.drawable.ic_dialog_info));
        notifications.add(new NotificationItem("BOSS直聘", "您有一个新的面试", "10分钟前", android.R.drawable.ic_dialog_alert));
        notifications.add(new NotificationItem("短信", "验证码：123456", "15分钟前", android.R.drawable.ic_dialog_dialer));
        notifications.add(new NotificationItem("日历", "下午3点：项目会议", "1小时前", android.R.drawable.ic_menu_my_calendar));
    }

    private void startNotificationAnimation() {
        // 每3秒添加一个新通知，模拟动态效果
        Handler notificationHandler = new Handler(Looper.getMainLooper());
        notificationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentNotificationIndex < notifications.size()) {
                    addNotification(notifications.get(currentNotificationIndex));
                    currentNotificationIndex++;
                    notificationHandler.postDelayed(this, 3000);
                }
            }
        }, 1000);
    }

    private void addNotification(NotificationItem item) {
        View notificationView = getLayoutInflater().inflate(R.layout.notification_item, notificationContainer, false);

        TextView title = notificationView.findViewById(R.id.notificationTitle);
        TextView text = notificationView.findViewById(R.id.notificationText);
        TextView time = notificationView.findViewById(R.id.notificationTime);

        title.setText(item.title);
        text.setText(item.text);
        time.setText(item.time);

        // 初始透明度为0
        notificationView.setAlpha(0f);
        // 添加到列表末尾
        notificationContainer.addView(notificationView);

        // 淡入动画
        notificationView.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
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
