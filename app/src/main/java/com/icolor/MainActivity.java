package com.icolor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.icolor.utils.ColorUtil;
import com.icolor.utils.GradientUtil;
import com.icolor.utils.WindowUtil;

public class MainActivity extends AppCompatActivity {

    private Fragment activeFragment;

    private ColorPalette hexPalette;
    private ColorPalette decPalette;

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowUtil.setFullScreen(this);
        WindowUtil.setKeepScreenOn(this);

        setContentView(R.layout.activity_main);

        hexPalette = new ColorPalette(ColorUtil.ValueNumberFormat.HEX);
        decPalette = new ColorPalette(ColorUtil.ValueNumberFormat.DEC);

        FragmentManager manager = getSupportFragmentManager();

        manager.beginTransaction()
                .add(R.id.main_fragment_container, hexPalette)
                .add(R.id.main_fragment_container, decPalette)
                .show(hexPalette)
                .hide(decPalette)
                .commit();

        activeFragment = hexPalette;

        ((Button) findViewById(R.id.test_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                activeFragment = decPalette;
                decPalette.setCurrentColorValueImmediately(hexPalette.getCurrentValue());
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (activeFragment instanceof ColorPalette) {
            return ((ColorPalette) activeFragment).handleMotionEvent(event);
        }
        return false;
    }
}