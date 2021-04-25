package com.icolor;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
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

    public interface ColorPaletteEventHandler {
        void onSwitchPalette();
        void onCopyToClipBoard(String valueString);
    }

    private enum ColorTheme {
        DARK, LIGHT
    }

    private final ColorUtil.ValueNumberFormat valueNumberFormat;
    private final ColorPaletteEventHandler colorPaletteEventHandler;
    private ColorTheme textColorTheme;
    public View primaryColorContainer;

    public ColorTextWheel[] colorTextWheels;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ColorPalette(ColorUtil.ValueNumberFormat valueNumberFormat, @Nullable ColorPaletteEventHandler handler) {
        this.valueNumberFormat = valueNumberFormat;
        if (handler == null) {
            this.colorPaletteEventHandler = new ColorPaletteEventHandler() {
                @Override public void onSwitchPalette() { }
                @Override public void onCopyToClipBoard(String valueString) { }
            };
        } else {
            this.colorPaletteEventHandler = handler;
        }
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

        ColorTextWheel.OnEventListener eventListenerImpl = new ColorTextWheel.OnEventListener() {
            @Override
            public void onClick() {
                colorPaletteEventHandler.onSwitchPalette();
            }

            @Override
            public void onTouch() {
                // What else to implement?
            }

            @Override
            public void onLongTouch() {
                String valueString = "";
                if (valueNumberFormat == ColorUtil.ValueNumberFormat.HEX) {
                    valueString = "#" + ColorUtil.vec2string(colorTextWheels[0].getCurrentValue(), ColorUtil.ValueNumberFormat.HEX)
                            + ColorUtil.vec2string(colorTextWheels[1].getCurrentValue(), ColorUtil.ValueNumberFormat.HEX)
                            + ColorUtil.vec2string(colorTextWheels[2].getCurrentValue(), ColorUtil.ValueNumberFormat.HEX);
                } else if (valueNumberFormat == ColorUtil.ValueNumberFormat.DEC) {
                    valueString = ColorUtil.vec2string(colorTextWheels[0].getCurrentValue(), ColorUtil.ValueNumberFormat.DEC)
                            + "," + ColorUtil.vec2string(colorTextWheels[1].getCurrentValue(), ColorUtil.ValueNumberFormat.DEC)
                            + "," + ColorUtil.vec2string(colorTextWheels[2].getCurrentValue(), ColorUtil.ValueNumberFormat.DEC);
                }
                colorPaletteEventHandler.onCopyToClipBoard(valueString);
            }
        };

        colorTextWheels = new ColorTextWheel[3];
        colorTextWheels[0] = new ColorTextWheel(requireActivity(),
                requireView().findViewById(R.id.red_value_text_container), valueNumberFormat, eventListenerImpl);
        colorTextWheels[1] = new ColorTextWheel(requireActivity(),
                requireView().findViewById(R.id.green_value_text_container), valueNumberFormat, eventListenerImpl);
        colorTextWheels[2] = new ColorTextWheel(requireActivity(),
                requireView().findViewById(R.id.blue_value_text_container), valueNumberFormat, eventListenerImpl);

        for (ColorTextWheel colorTextWheel: colorTextWheels) {
            colorTextWheel.setValueUpdateListener(value -> setGradientUseWheels());
        }
        setGradientUseWheels();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (valueNumberFormat == ColorUtil.ValueNumberFormat.HEX) {
            return inflater.inflate(R.layout.fragment_color_palette_hex, container, false);
        } else if (valueNumberFormat == ColorUtil.ValueNumberFormat.DEC) {
            return inflater.inflate(R.layout.fragment_color_palette_dec, container, false);
        } else {
            return null;
        }
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

        primaryColorGradient.changeColorTo(colorValue);
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
        ((TextView) requireView().findViewById(R.id.color_prefix_text)).setTextColor
                (ColorUtil.blend(color, ColorUtil.GRAY, 0.3f));
    }
}