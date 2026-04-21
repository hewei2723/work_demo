package com.hewei.demoLock;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class UnlockSliderView extends FrameLayout {

    private View unlockHandle;
    private View unlockTrack;
    private TextView unlockHint;
    private View unlockContainer;

    private float startY;
    private float currentY;
    private boolean isDragging = false;
    private static final float UNLOCK_THRESHOLD = 300f; // 解锁需要滑动的距离

    private OnUnlockListener unlockListener;

    public interface OnUnlockListener {
        void onUnlock();
    }

    public UnlockSliderView(Context context) {
        super(context);
        init(context);
    }

    public UnlockSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public UnlockSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.unlock_slider_view, this, true);

        unlockContainer = findViewById(R.id.unlockContainer);
        unlockHandle = findViewById(R.id.unlockHandle);
        unlockTrack = findViewById(R.id.unlockTrack);
        unlockHint = findViewById(R.id.unlockHint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                isDragging = true;
                // 放大滑块
                animateHandleScale(1.2f);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!isDragging) return false;

                currentY = event.getY();
                float deltaY = startY - currentY; // 向上滑动为正

                if (deltaY > 0) {
                    // 限制最大移动距离
                    float translate = Math.min(deltaY, UNLOCK_THRESHOLD + 100);
                    unlockHandle.setTranslationY(-translate);

                    // 改变透明度
                    float alpha = 1f - (translate / UNLOCK_THRESHOLD);
                    unlockHandle.setAlpha(Math.max(0.3f, alpha));
                    unlockTrack.setAlpha(Math.max(0.2f, alpha));

                    // 检查是否达到解锁阈值
                    if (translate >= UNLOCK_THRESHOLD) {
                        performUnlock();
                    }
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (isDragging) {
                    isDragging = false;
                    // 回弹动画
                    resetHandle();
                }
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void performUnlock() {
        if (unlockListener != null) {
            // 滑块继续向上飞出
            ObjectAnimator flyOut = ObjectAnimator.ofFloat(unlockHandle, "translationY", -UNLOCK_THRESHOLD - 400);
            flyOut.setDuration(300);
            flyOut.setInterpolator(new DecelerateInterpolator());
            flyOut.start();

            // 淡出其他元素
            unlockTrack.animate().alpha(0f).setDuration(200).start();
            unlockHint.animate().alpha(0f).setDuration(200).start();

            // 延迟触发解锁回调
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (unlockListener != null) {
                        unlockListener.onUnlock();
                    }
                }
            }, 200);
        }
    }

    private void resetHandle() {
        // 回弹动画
        unlockHandle.animate()
                .translationY(0)
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        unlockTrack.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
    }

    private void animateHandleScale(float scale) {
        unlockHandle.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(150)
                .start();
    }

    public void setOnUnlockListener(OnUnlockListener listener) {
        this.unlockListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unlockHandle.animate().cancel();
    }
}
