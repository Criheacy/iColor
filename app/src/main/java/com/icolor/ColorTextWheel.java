package com.icolor;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.icolor.utils.WindowUtil;

public class ColorTextWheel {

    private String TAG = "Text Wheel";

    public ColorTextWheel(Activity activity, RelativeLayout container) {
        this.activity = activity;
        this.container = container;

        textContainerInWheel = new View[numberOfTextInWheel];
        textViewInWheel = new TextView[numberOfTextInWheel];
        LayoutInflater inflater = (LayoutInflater)
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < numberOfTextInWheel; i++) {
            RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams
                    (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

            textContainerInWheel[i] = inflater.inflate(R.layout.color_value_text, null);
            textContainerInWheel[i].setTranslationY(i * 50);
            textContainerInWheel[i].setLayoutParams(textParams);
            textViewInWheel[i] = (TextView) textContainerInWheel[i].findViewById(R.id.value_text);
            textViewInWheel[i].setText("AF");   // test
            container.addView(textContainerInWheel[i]);
        }
        focusedTextContainerInWheel = textContainerInWheel[(numberOfTextInWheel + 1) / 2];

        containerAlpha = new FocusedInterpolator(100f, 3, 0f, 1f);
        containerScale = new FocusedInterpolator(100f, 3, 0.7f, 1f);
        containerTranslation = new MagneticInterpolator(0.7f, 10f, 1f);

        currentValue = 128;
    }

    public enum ScrollingOrientation {
        NONE, HORIZONTAL, VERTICAL
    }

    public void scrollHorizontal() {
        currentScrollingOrientation = ScrollingOrientation.HORIZONTAL;

    }

    public void scrollVertical(int scrollOffset) {
        currentScrollingOrientation = ScrollingOrientation.VERTICAL;
        changeContainerSize(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        updateWheelOffset(scrollOffset);
    }

    public View getContainerView() {
        return container;
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
        while (currentWheelOffset >= textContainerSpacingInWheel) {
            currentWheelOffset -= textContainerSpacingInWheel;
            topTextViewIndex += numberOfTextInWheel - 1;
            topTextViewIndex %= numberOfTextInWheel;
            currentValue--;
            textHasChanged = true;
        }
        while (currentWheelOffset <= -textContainerSpacingInWheel) {
            currentWheelOffset += textContainerSpacingInWheel;
            topTextViewIndex += numberOfTextInWheel + 1;
            topTextViewIndex %= numberOfTextInWheel;
            currentValue++;
            textHasChanged = true;
        }
        if (textHasChanged) {
            Log.d(TAG, "text update: " + currentValue);
            updateTextValue();
        }
        updateTextContainer();
    }

    private void updateTextValue() {
        int i = topTextViewIndex;
        do {
            float intervalCount = i - topTextViewIndex - numberOfTextInWheel / 2f + 1f;
            // TODO: Customize an integer -> decimalView utility
            textViewInWheel[i % numberOfTextInWheel].setText(Integer.toString((int) (currentValue + intervalCount), 16));
            i++;
        } while(i % numberOfTextInWheel != topTextViewIndex);
    }

    private void updateTextContainer() {
        int i = topTextViewIndex;
        do {
            float intervalCount = i - topTextViewIndex - numberOfTextInWheel / 2f + 0.5f;
            float offset = intervalCount * textContainerSpacingInWheel + currentWheelOffset;
            textContainerInWheel[i % numberOfTextInWheel].setTranslationY(containerTranslation.getInterpolation(offset));
            textContainerInWheel[i % numberOfTextInWheel].setAlpha(containerAlpha.getInterpolation(offset));
            textContainerInWheel[i % numberOfTextInWheel].setScaleX(containerScale.getInterpolation(offset));
            textContainerInWheel[i % numberOfTextInWheel].setScaleY(containerScale.getInterpolation(offset));
            i++;
        } while(i % numberOfTextInWheel != topTextViewIndex);
    }

    // odd number performs better
    private final int numberOfTextInWheel = 7;
    private final View[] textContainerInWheel;
    private final View focusedTextContainerInWheel;
    private final TextView[] textViewInWheel;
    private int topTextViewIndex;

    private final int textContainerSpacingInWheel = 160;
    private int currentWheelOffset;

    private float globalAlpha = 1f;
    private FocusedInterpolator containerAlpha;
    private FocusedInterpolator containerScale;
    private MagneticInterpolator containerTranslation;

    private int currentValue;
    private ScrollingOrientation currentScrollingOrientation;

    private final Activity activity;
    private final RelativeLayout container;
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
    public float reduce;
    public float magnetic;

    public MagneticInterpolator(float scale, float reduce, float magnetic) {
        this.scale = scale;
        this.reduce = reduce;
        this.magnetic = magnetic;
    }

    @Override
    public float getInterpolation(float input) {
        // FIXME: Interpolator Error
        boolean isNegative = false;
        if (input < 0) {
            input = -input;
            isNegative = true;
        }
        float magneticValue;
        if (input < reduce) {
            magneticValue = (float) ((-scale / reduce) * Math.pow(input, 2) + scale * input);
        } else {
            magneticValue = (float) (1f / (scale * Math.pow(input - reduce, 2) - scale * (reduce - 1)));
        }
        Log.d("MAGENATIC", String.valueOf(magneticValue));
        if (isNegative) {
            return -input * scale + magnetic * magneticValue;
        } else {
            return input * scale - magnetic * magneticValue;
        }
    }
}