package com.icolor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import com.icolor.utils.ColorUtil;
import com.icolor.utils.GradientUtil;
import com.icolor.utils.WindowUtil;

public class MainActivity extends AppCompatActivity {

    public GradientUtil gradientUtil;
    public View primaryColorContainer;

    private ValueAnimator testAnimator;

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowUtil.SetFullScreen(this);
        WindowUtil.SetKeepScreenOn(this);

        setContentView(R.layout.activity_main);

        gradientUtil = new GradientUtil(ColorUtil.WHITE);
        primaryColorContainer = findViewById(R.id.primary_color_container);
        testAnimator = new ValueAnimator();

        testAnimator.setDuration(1000);
        testAnimator.setRepeatCount(ValueAnimator.INFINITE);
        testAnimator.setIntValues(0, 1);
        testAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updatePrimaryColorContainer(gradientUtil.getColor());
            }
        });
        testAnimator.start();

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



    private void updatePrimaryColorContainer(int color) {
        primaryColorContainer.setBackgroundColor(color);
    }
}