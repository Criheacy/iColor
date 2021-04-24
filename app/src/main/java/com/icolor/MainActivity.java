package com.icolor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.icolor.utils.ColorUtil;
import com.icolor.utils.GradientUtil;
import com.icolor.utils.WindowUtil;

public class MainActivity extends AppCompatActivity {

    public GradientUtil primaryColorGradient;
    public GradientUtil textColorGradient;

    private enum ColorTheme {
        DARK, LIGHT
    }

    private ColorTheme textColorTheme;
    public View primaryColorContainer;

    public ColorTextWheel[] colorTextWheels;

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowUtil.setFullScreen(this);
        WindowUtil.setKeepScreenOn(this);

        setContentView(R.layout.activity_main);

        primaryColorContainer = findViewById(R.id.primary_color_container);

        primaryColorGradient = new GradientUtil(ColorUtil.WHITE);
        primaryColorGradient.addGradientListener(color -> updatePrimaryColorContainer(color));

        textColorTheme = ColorTheme.LIGHT;
        textColorGradient = new GradientUtil(R.color.dark_text_color);
        textColorGradient.addGradientListener(color -> updateTextColor(color));

        colorTextWheels = new ColorTextWheel[3];
        colorTextWheels[0] = new ColorTextWheel(this, findViewById(R.id.red_value_text_container));
        colorTextWheels[1] = new ColorTextWheel(this, findViewById(R.id.green_value_text_container));
        colorTextWheels[2] = new ColorTextWheel(this, findViewById(R.id.blue_value_text_container));

        for (ColorTextWheel colorTextWheel: colorTextWheels) {
            colorTextWheel.setValueUpdateListener(value -> setGradientUseWheels());
        }
        setGradientUseWheels();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (ColorTextWheel colorTextWheel: colorTextWheels) {
            if (colorTextWheel.handleGestureEvent(event)) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setGradientUseWheels() {
        primaryColorGradient.gradientTo(ColorUtil.rgba2int(
                colorTextWheels[0].getCurrentValue(),
                colorTextWheels[1].getCurrentValue(),
                colorTextWheels[2].getCurrentValue(),
                0xFF
        ));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void updatePrimaryColorContainer(int color) {
        primaryColorContainer.setBackgroundColor(color);

        int lightness = ColorUtil.lightness(ColorUtil.rgba2int(
                colorTextWheels[0].getCurrentValue(),
                colorTextWheels[1].getCurrentValue(),
                colorTextWheels[2].getCurrentValue(),
                0xFF
        ));

        if (lightness > 0x80 && textColorTheme == ColorTheme.LIGHT) {
            Log.d("Text Theme", "TO DARK");
            textColorGradient.gradientTo(ColorUtil.BLACK);
            textColorTheme = ColorTheme.DARK;
        } else if (lightness < 0x60 && textColorTheme == ColorTheme.DARK) {
            Log.d("Text Theme", "TO LIGHT");
            textColorGradient.gradientTo(ColorUtil.WHITE);
            textColorTheme = ColorTheme.LIGHT;
        }
    }

    private void updateTextColor(int color) {
        Log.d("Update Text Color", String.valueOf(color));
        for (ColorTextWheel colorTextWheel : colorTextWheels) {
            int childCount = ((ViewGroup) colorTextWheel.getContainerView()).getChildCount();
            for (int i = 0; i < childCount; i++) {
                FrameLayout valueTextFrame = (FrameLayout) ((ViewGroup) colorTextWheel
                        .getContainerView())
                        .getChildAt(i);
                ((TextView) valueTextFrame
                        .getChildAt(0))
                        .setTextColor(color);
            }
        }
        ((TextView) findViewById(R.id.color_prefix_text)).setTextColor
                (ColorUtil.blend(color, ColorUtil.GRAY, 0.3f));
    }
}