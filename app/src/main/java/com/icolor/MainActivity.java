package com.icolor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;

import com.icolor.utils.ColorUtil;
import com.icolor.utils.GestureUtil;
import com.icolor.utils.GradientUtil;
import com.icolor.utils.TouchHandlerUtil;
import com.icolor.utils.WindowUtil;

public class MainActivity extends AppCompatActivity {

    public GradientUtil gradientUtil;
    public View primaryColorContainer;

    public ColorTextWheel[] colorTextWheels;

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowUtil.setFullScreen(this);
        WindowUtil.setKeepScreenOn(this);

        setContentView(R.layout.activity_main);

        primaryColorContainer = findViewById(R.id.primary_color_container);

        gradientUtil = new GradientUtil(ColorUtil.WHITE);
        gradientUtil.addGradientListener(color -> updatePrimaryColorContainer(color));

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
        gradientUtil.gradientTo(ColorUtil.rgba2int(
                colorTextWheels[0].getCurrentValue(),
                colorTextWheels[1].getCurrentValue(),
                colorTextWheels[2].getCurrentValue(),
                0xFF
        ));
    }

    private void updatePrimaryColorContainer(int color) {
        primaryColorContainer.setBackgroundColor(color);
    }
}