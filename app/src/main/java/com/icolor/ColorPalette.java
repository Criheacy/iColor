package com.icolor;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.icolor.utils.ColorUtil;
import com.icolor.utils.GradientUtil;

public class ColorPalette extends Fragment {

    public GradientUtil primaryColorGradient;
    public GradientUtil textColorGradient;

    private enum ColorTheme {
        DARK, LIGHT
    }

    private final ColorUtil.ValueNumberFormat valueNumberFormat;
    private ColorTheme textColorTheme;
    public View primaryColorContainer;

    public ColorTextWheel[] colorTextWheels;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ColorPalette(ColorUtil.ValueNumberFormat valueNumberFormat) {
        this.valueNumberFormat = valueNumberFormat;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override @RequiresApi(api = Build.VERSION_CODES.O)
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        primaryColorContainer = requireActivity().findViewById(R.id.primary_color_container);

        primaryColorGradient = new GradientUtil(ColorUtil.WHITE);
        primaryColorGradient.addGradientListener(color -> updatePrimaryColorContainer(color));

        textColorTheme = ColorTheme.LIGHT;
        textColorGradient = new GradientUtil(R.color.dark_text_color);
        textColorGradient.addGradientListener(color -> updateTextColor(color));

        colorTextWheels = new ColorTextWheel[3];
        colorTextWheels[0] = new ColorTextWheel(requireActivity(),
                requireActivity().findViewById(R.id.red_value_text_container), valueNumberFormat);
        colorTextWheels[1] = new ColorTextWheel(requireActivity(),
                requireActivity().findViewById(R.id.green_value_text_container), valueNumberFormat);
        colorTextWheels[2] = new ColorTextWheel(requireActivity(),
                requireActivity().findViewById(R.id.blue_value_text_container), valueNumberFormat);

        for (ColorTextWheel colorTextWheel: colorTextWheels) {
            colorTextWheel.setValueUpdateListener(value -> setGradientUseWheels());
        }
        setGradientUseWheels();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_color_palette_hex, container, false);
    }

    public boolean handleMotionEvent(MotionEvent event) {
        for (ColorTextWheel colorTextWheel: colorTextWheels) {
            if (colorTextWheel.handleGestureEvent(event)) {
                return true;
            }
        }
        return false;
    }

    public int getCurrentValue() {
        return ColorUtil.rgba2int(
                colorTextWheels[0].getCurrentValue(),
                colorTextWheels[1].getCurrentValue(),
                colorTextWheels[2].getCurrentValue(),
                0xFF
        );
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setCurrentColorValueImmediately(int colorValue) {
        int r = ColorUtil.int2ri(colorValue);
        int g = ColorUtil.int2gi(colorValue);
        int b = ColorUtil.int2bi(colorValue);
        colorTextWheels[0].setCurrentValue(r);
        colorTextWheels[1].setCurrentValue(g);
        colorTextWheels[2].setCurrentValue(b);

        updatePrimaryColorContainer(colorValue);
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

        if (lightness >= 0x60 && textColorTheme == ColorTheme.LIGHT) {
            textColorGradient.gradientTo(ColorUtil.BLACK);
            textColorTheme = ColorTheme.DARK;
        } else if (lightness < 0x60 && textColorTheme == ColorTheme.DARK) {
            textColorGradient.gradientTo(ColorUtil.WHITE);
            textColorTheme = ColorTheme.LIGHT;
        }
    }

    private void updateTextColor(int color) {
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
        ((TextView) requireActivity().findViewById(R.id.color_prefix_text)).setTextColor
                (ColorUtil.blend(color, ColorUtil.GRAY, 0.3f));
    }
}