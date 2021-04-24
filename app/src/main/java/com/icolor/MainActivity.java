package com.icolor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.SeekBar;

import com.icolor.utils.ColorUtil;
import com.icolor.utils.GradientUtil;
import com.icolor.utils.TouchHandlerUtil;
import com.icolor.utils.WindowUtil;

public class MainActivity extends AppCompatActivity {

    public GradientUtil gradientUtil;
    public TouchHandlerUtil touchHandlerUtil;
    public View primaryColorContainer;
    public ColorTextWheel wheelTest;

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowUtil.setFullScreen(this);
        WindowUtil.setKeepScreenOn(this);

        setContentView(R.layout.activity_main);

        View redValueText = findViewById(R.id.red_value_text_container);

        wheelTest = new ColorTextWheel(this, findViewById(R.id.red_value_text_container));
        gradientUtil = new GradientUtil(ColorUtil.WHITE);

        gradientUtil.addGradientListener(new GradientUtil.GradientListener() {
            @Override
            public void onColorChanged(int color) {
                updatePrimaryColorContainer(color);
            }
        });

        touchHandlerUtil = new TouchHandlerUtil(new TouchHandlerUtil.OnGestureListener() {
            @Override
            public void onClick(Point p) {
                Log.d("Click", p.toString());
                if (WindowUtil.pointInView(p, redValueText)) {
                    Log.d("InRect", "True");
                }
            }

            @Override
            public void onTouch(Point p) {
                Log.d("Touch", p.toString());
            }

            @Override
            public void onDrag(Point origin, Point dragTo, Point lastDragVector) {
                // Log.d("Drag", origin.toString() + " -> " + dragTo.toString());
                if (WindowUtil.pointInView(dragTo, wheelTest.getContainerView()))
                    wheelTest.scrollVertical(lastDragVector.y);
            }

            @Override
            public void onLeave() {

            }
        });
        primaryColorContainer = findViewById(R.id.primary_color_container);

        ((SeekBar) findViewById(R.id.red_test)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int red = ((SeekBar) findViewById(R.id.red_test)).getProgress();
                int green = ((SeekBar) findViewById(R.id.green_test)).getProgress();
                int blue = ((SeekBar) findViewById(R.id.blue_test)).getProgress();
                Log.d("Color", "Color: " + red + " " + green + " " + blue);
                gradientUtil.gradientTo(ColorUtil.rgba2int(red, green, blue, 0xFF));
                // updatePrimaryColorContainer(ColorUtil.rgba2int(red, green, blue, 0xFF));
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return touchHandlerUtil.handle(event);
    }

    private void updatePrimaryColorContainer(int color) {
        primaryColorContainer.setBackgroundColor(color);
    }
}