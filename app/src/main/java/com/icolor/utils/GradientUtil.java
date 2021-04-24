package com.icolor.utils;

import android.animation.ValueAnimator;
import android.os.Build;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class GradientUtil {
    public static long gradientDuration = 300;

    public GradientUtil(int startColor) {
        animator = new ValueAnimator();
        animator = new ValueAnimator();
        animator.setInterpolator(input -> input);
        animator.setDuration(gradientDuration);
        animator.setFloatValues(0f, 1f);
        this.startColor = startColor;
    }

    public void gradientTo(int distColor) {
        startColor = getColor();
        this.distColor = distColor;
        animator.start();
    }

    public int getColor() {
        if (!animator.isRunning()) {
            return distColor;
        } else {
            float animatedValue = (float) animator.getAnimatedValue();
            return ColorUtil.blend(startColor, distColor, animatedValue);
        }
    }

    public void addGradientListener(GradientListener l) {
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                l.onColorChanged(getColor());
            }
        });
    }

    public interface GradientListener {
        void onColorChanged(int color);
    }

    private int startColor;
    private int distColor;
    private ValueAnimator animator;
}
