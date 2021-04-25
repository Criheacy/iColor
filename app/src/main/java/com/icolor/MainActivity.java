package com.icolor;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

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

        FragmentManager manager = getSupportFragmentManager();

        // I don't know if there is a better way
        Activity self = this;
        hexPalette = new ColorPalette(ColorUtil.ValueNumberFormat.HEX, new ColorPalette.ColorPaletteEventHandler() {
            @Override
            public void onSwitchPalette() {
                decPalette.setCurrentColorValueImmediately(hexPalette.getCurrentValue());
                manager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .show(decPalette)
                        .hide(hexPalette)
                        .commit();
                activeFragment = decPalette;
            }

            @Override
            public void onCopyToClipBoard(String valueString) {
                Toast.makeText(self, R.string.copy_info, Toast.LENGTH_SHORT).show();
                WindowUtil.copyToClipBoard(valueString, "Color", self);
            }
        });

        decPalette = new ColorPalette(ColorUtil.ValueNumberFormat.DEC, new ColorPalette.ColorPaletteEventHandler() {
            @Override
            public void onSwitchPalette() {
                hexPalette.setCurrentColorValueImmediately(decPalette.getCurrentValue());
                manager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .show(hexPalette)
                        .hide(decPalette)
                        .commit();
                activeFragment = hexPalette;
            }

            @Override
            public void onCopyToClipBoard(String valueString) {
                Toast.makeText(self, R.string.copy_info, Toast.LENGTH_SHORT).show();
                WindowUtil.copyToClipBoard(valueString, "Color", self);
            }
        });

        manager.beginTransaction()
                .add(R.id.main_fragment_container, hexPalette)
                .add(R.id.main_fragment_container, decPalette)
                .show(hexPalette)
                .hide(decPalette)
                .commit();

        activeFragment = hexPalette;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (activeFragment instanceof ColorPalette) {
            return ((ColorPalette) activeFragment).handleMotionEvent(event);
        }
        return false;
    }
}