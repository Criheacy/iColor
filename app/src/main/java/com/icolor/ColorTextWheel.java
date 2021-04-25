package com.icolor;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icolor.utils.ColorUtil;
import com.icolor.utils.GestureUtil;
import com.icolor.utils.WindowUtil;

public class ColorTextWheel {
    public interface OnValueUpdateListener {
        void onValueUpdate(int value);
    }

    public ColorTextWheel(Activity activity, RelativeLayout container, ColorUtil.ValueNumberFormat valueNumberFormat) {
        this.activity = activity;
        this.container = container;
        this.valueNumberFormat = valueNumberFormat;

        gestureUtil = new GestureUtil(container, new GestureUtil.OnGestureListener() {
            @Override public void click() { }
            @Override public void touch() { }

            @Override
            public void dragStart(GestureUtil.GestureOrientation orientation) {
                startScrolling();
                if (orientation == GestureUtil.GestureOrientation.HORIZONTAL) {
                    startHorizontalScroll();
                } else if (orientation == GestureUtil.GestureOrientation.VERTICAL) {
                    startVerticalScroll();
                }
            }

            @Override
            public void dragging(GestureUtil.GestureOrientation orientation, int draggingDistance) {
                scroll(draggingDistance);
            }

            @Override
            public void dragEnd() {
                endScrolling();
            }
        });

        textContainerInWheel = new View[numberOfTextInWheel];
        textViewInWheel = new TextView[numberOfTextInWheel];
        LayoutInflater inflater = (LayoutInflater)
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < numberOfTextInWheel; i++) {
            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

            textContainerInWheel[i] = inflater.inflate(R.layout.color_value_text, null);
            textContainerInWheel[i].setLayoutParams(textParams);
            textViewInWheel[i] = (TextView) textContainerInWheel[i].findViewById(R.id.value_text);
            container.addView(textContainerInWheel[i]);
        }

        globalAlphaAnimator = new ValueAnimator();
        globalAlphaAnimator.addUpdateListener(animation -> {
            globalAlpha = (float) animation.getAnimatedValue();
            updateTextContainer();
        });
        globalAlphaAnimator.addListener(new Animator.AnimatorListener() {
            @Override public void onAnimationStart(Animator animation) { }
            @Override public void onAnimationCancel(Animator animation) { }
            @Override public void onAnimationRepeat(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (globalAlpha == 0f) {
                    changeContainerSize(RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);
                }
            }

        });

        resetOffsetAnimator = new ValueAnimator();
        resetOffsetAnimator.setDuration(resetOffsetDuration);
        resetOffsetAnimator.addUpdateListener(animation -> {
            currentWheelOffset = (int) animation.getAnimatedValue();
            updateTextContainer();
        });

        containerOffsetAnimator = new ValueAnimator();
        containerOffsetAnimator.setDuration(containerOffsetDuration);
        containerOffsetAnimator.addUpdateListener(animation -> {
            container.setTranslationX((float) animation.getAnimatedValue());
        });

        currentValue = 240;

        startVerticalScroll();
        updateTextValue();
        updateTextContainer();
    }

    public void setValueUpdateListener(OnValueUpdateListener l) {
        valueUpdateListener = l;
    }

    public void setCurrentValue(int value) {
        currentValue = value;
        updateTextValue();
        updateTextContainer();
    }

    public boolean handleGestureEvent(MotionEvent event) {
        return gestureUtil.handle(event);
    }

    public enum ScrollingOrientation {
        NONE, HORIZONTAL, VERTICAL
    }

    public void startScrolling() {
        globalAlphaAnimator.setDuration(emergeDuration);
        globalAlphaAnimator.setFloatValues(globalAlpha, 0.8f);
        globalAlphaAnimator.start();
    }

    public void endScrolling() {
        globalAlphaAnimator.setDuration(fadeOutDuration);
        globalAlphaAnimator.setFloatValues(globalAlpha, 0f);
        globalAlphaAnimator.start();

        resetOffsetAnimator.setIntValues(currentWheelOffset, 0);
        resetOffsetAnimator.start();
    }

    public void startHorizontalScroll() {
        currentScrollingOrientation = ScrollingOrientation.HORIZONTAL;
        changeContainerSize((int) WindowUtil.dps2dp(180, activity),
                RelativeLayout.LayoutParams.WRAP_CONTENT);

        containerAlpha = new FocusedInterpolator(100f, 4, 0f, 1f);
        containerScale = new FocusedInterpolator(80f, 4, 0.6f, 1f);
        containerTranslation = new MagneticInterpolator(100f, 2.5f, 0.5f, 0.55f);

        updateTextValue();
    }

    public void startVerticalScroll() {
        currentScrollingOrientation = ScrollingOrientation.VERTICAL;
        changeContainerSize(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        containerAlpha = new FocusedInterpolator(150f, 3, 0f, 1f);
        containerScale = new FocusedInterpolator(100f, 3, 0.7f, 1f);
        containerTranslation = new MagneticInterpolator(100f, 3f, 0.7f, 0.65f);

        updateTextValue();
    }

    public void scroll(int scrollOffset) {
        updateWheelOffset(scrollOffset);
    }

    public View getContainerView() {
        return container;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    private void changeContainerSize(int widthDp, int heightDp) {
        float offsetX = container.getTranslationX();
        float offsetY = container.getTranslationY();
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(widthDp, heightDp);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        container.setLayoutParams(params);
        container.setTranslationX(offsetX);
        container.setTranslationY(offsetY);
    }

    private void updateWheelOffset(int deltaOffset) {
        currentWheelOffset += deltaOffset;
        boolean textHasChanged = false;
        if (currentScrollingOrientation == ScrollingOrientation.VERTICAL) {
            while (currentWheelOffset >= verticalSpacingInWheel / 2
                    && currentValue > minValue) {
                currentWheelOffset -= verticalSpacingInWheel;
                topTextViewIndex += numberOfTextInWheel - 1;
                topTextViewIndex %= numberOfTextInWheel;
                currentValue--;
                textHasChanged = true;
            }
            while (currentWheelOffset <= -verticalSpacingInWheel / 2
                    && currentValue < maxValue) {
                currentWheelOffset += verticalSpacingInWheel;
                topTextViewIndex += numberOfTextInWheel + 1;
                topTextViewIndex %= numberOfTextInWheel;
                currentValue++;
                textHasChanged = true;
            }
        } else if (currentScrollingOrientation == ScrollingOrientation.HORIZONTAL) {
            while (currentWheelOffset >= horizontalSpacingInWheel / 2
                    && currentValue > minValue) {
                currentWheelOffset -= horizontalSpacingInWheel;
                topTextViewIndex += numberOfTextInWheel - 1;
                topTextViewIndex %= numberOfTextInWheel;
                currentValue -= 0x10;
                if (currentValue < 0) {
                    currentValue = 0;
                }
                textHasChanged = true;
            }
            while (currentWheelOffset <= -horizontalSpacingInWheel / 2
                    && currentValue < maxValue) {
                currentWheelOffset += horizontalSpacingInWheel;
                topTextViewIndex += numberOfTextInWheel + 1;
                topTextViewIndex %= numberOfTextInWheel;
                currentValue += 0x10;
                if (currentValue > 0xFF) {
                    currentValue = 0xFF;
                }
                textHasChanged = true;
            }
        }
        if (textHasChanged) {
            updateTextValue();
            if (valueUpdateListener != null) {
                valueUpdateListener.onValueUpdate(currentValue);
            }
        }
        updateTextContainer();
    }

    private void updateTextValue() {
        int i = topTextViewIndex;
        int minValueIndex = -1;
        int maxValueIndex = -1;

        do {
            int value = currentValue;
            if (currentScrollingOrientation == ScrollingOrientation.HORIZONTAL) {
                value = currentValue + (int) (i - topTextViewIndex - numberOfTextInWheel / 2f + 0.5f) * 0x10;
            } else if (currentScrollingOrientation == ScrollingOrientation.VERTICAL) {
                value = currentValue + (int) (i - topTextViewIndex - numberOfTextInWheel / 2f + 0.5f);
            }

            if (value <= minValue ) {
                minValueIndex = i % numberOfTextInWheel;
                textViewInWheel[i % numberOfTextInWheel].setText(null);
            } else if (value >= maxValue) {
                if (maxValueIndex == -1) {
                    maxValueIndex = i % numberOfTextInWheel;
                }
                textViewInWheel[i % numberOfTextInWheel].setText(null);
            } else {
                textViewInWheel[i % numberOfTextInWheel].setText(ColorUtil.vec2string(value, valueNumberFormat));
            }
            i++;
        } while(i % numberOfTextInWheel != topTextViewIndex);

        if (minValueIndex != -1) {
            textViewInWheel[minValueIndex].setText(ColorUtil.vec2string(minValue, valueNumberFormat));
        }
        if (maxValueIndex != -1) {
            textViewInWheel[maxValueIndex].setText(ColorUtil.vec2string(maxValue, valueNumberFormat));
        }
    }

    private void updateTextContainer() {
        int i = topTextViewIndex;
        do {
            View currentContainer = textContainerInWheel[i % numberOfTextInWheel];
            float intervalCount = i - topTextViewIndex - numberOfTextInWheel / 2f + 0.5f;
            float offset = 0f;

            if (currentScrollingOrientation == ScrollingOrientation.HORIZONTAL) {
                offset = intervalCount * horizontalSpacingInWheel + currentWheelOffset;
                currentContainer.setTranslationX(containerTranslation.getInterpolation(offset));
                currentContainer.setTranslationY(0);
            } else if (currentScrollingOrientation == ScrollingOrientation.VERTICAL) {
                offset = intervalCount * verticalSpacingInWheel + currentWheelOffset;
                currentContainer.setTranslationX(0);
                currentContainer.setTranslationY(containerTranslation.getInterpolation(offset));
            }

            if (i % numberOfTextInWheel == (topTextViewIndex + numberOfTextInWheel / 2) % numberOfTextInWheel) {
                currentContainer.setAlpha(focusingAlpha);
            } else {
                currentContainer.setAlpha(containerAlpha.getInterpolation(offset) * globalAlpha);
            }

            currentContainer.setScaleX(containerScale.getInterpolation(offset));
            currentContainer.setScaleY(containerScale.getInterpolation(offset));
            i++;
        } while(i % numberOfTextInWheel != topTextViewIndex);
    }

    private final ColorUtil.ValueNumberFormat valueNumberFormat;

    private final int numberOfTextInWheel = 7;
    private final View[] textContainerInWheel;
    private final TextView[] textViewInWheel;
    private int topTextViewIndex;

    private final int verticalSpacingInWheel = 180;
    private final int horizontalSpacingInWheel = 180;
    private int currentWheelOffset;

    private final long emergeDuration = 150;
    private final long fadeOutDuration = 300;
    private final ValueAnimator globalAlphaAnimator;
    private final long resetOffsetDuration = 100;
    private final ValueAnimator resetOffsetAnimator;

    private float globalAlpha = 0f;
    private float focusingAlpha = 0.95f;
    private FocusedInterpolator containerAlpha;
    private FocusedInterpolator containerScale;
    private MagneticInterpolator containerTranslation;

    private int currentValue;
    private final int minValue = 0x00;
    private final int maxValue = 0xFF;
    private OnValueUpdateListener valueUpdateListener;
    private ScrollingOrientation currentScrollingOrientation;

    private final Activity activity;
    private final RelativeLayout container;
    private final long containerOffsetDuration = 300;
    private final ValueAnimator containerOffsetAnimator;

    private final GestureUtil gestureUtil;
}

class FocusedInterpolator implements Interpolator {
    public float scale;
    public int times;
    public float min;
    public float max;

    public FocusedInterpolator(float scale, int times, float min, float max) {
        this.scale = scale;
        this.times = times;
        this.min = min;
        this.max = max;
    }

    @Override
    public float getInterpolation(float input) {
        if (input < 0) input = -input;
        float rawValue = (float) (1f / (Math.pow(input / scale, times) + 1f));
        return rawValue * (max - min) + min;
    }
}

class MagneticInterpolator implements Interpolator {
    public float scale;
    public float times;
    public float assimilate;
    public float slope;

    public MagneticInterpolator(float scale, float times, float assimilate, float slope) {
        this.scale = scale;
        this.times = times;
        this.assimilate = assimilate;
        this.slope = slope;
    }

    @Override
    public float getInterpolation(float input) {
        boolean negative = false;
        if (input < 0) {
            input = -input;
            negative = true;
        }

        input /= scale;
        float fraction = (input - assimilate) / (1 - assimilate);
        if (fraction < 0) {
            fraction = 0;
        }
        if (fraction > 1) {
            fraction = 1;
        }

        float magneticValue = (float) Math.pow(input, times);
        float linearValue = (input - 1) * slope + 1;

        float value = magneticValue * (1 - fraction) + linearValue * fraction;
        if (negative) {
            value = -value;
        }
        return value * scale;
    }
}