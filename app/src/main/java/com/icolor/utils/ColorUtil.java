package com.icolor.utils;

import android.util.Log;

import androidx.annotation.IntRange;

import com.icolor.ColorTextWheel;

import static java.lang.Math.round;

public class ColorUtil {

    public static int WHITE = 0xFFFFFFFF;
    public static int GRAY  = 0xFF808080;
    public static int BLACK = 0xFF000000;

    public enum ValueNumberFormat {
        HEX, DEC
    }

    public static int blend(int color1, int color2, float fraction) {
        int r = (int) round(int2ri(color1) * (1 - fraction) + int2ri(color2) * fraction);
        int g = (int) round(int2gi(color1) * (1 - fraction) + int2gi(color2) * fraction);
        int b = (int) round(int2bi(color1) * (1 - fraction) + int2bi(color2) * fraction);
        int a = (int) round(int2ai(color1) * (1 - fraction) + int2ai(color2) * fraction);
        return rgba2int(r, g, b, a);
    }

    // Color format conversions

    public static String vec2string(@IntRange(from = 0x00, to = 0xFF) int vec, ValueNumberFormat format) {
        switch (format) {
            case DEC:
                return vec2bitString(vec);
            case HEX:
                return vec2string(vec);
            default:
                return null;
        }
    }

    private static String vec2bitString(@IntRange(from = 0x00, to = 0xFF) int vec) {
        /// Maybe unaligned style looks better
        /*while (result.length() < 3) {
            result.insert(0, " ");
        }*/
        return String.valueOf(vec);
    }

    private static String vec2string(@IntRange(from = 0x00, to = 0xFF) int vec) {
        StringBuilder result = new StringBuilder();
        int _vec = vec;
        for (int i = 0; i < 2; i++) {
            result.append(digit2string(vec % 0x10));
            vec /= 0x10;
        }
        return result.reverse().toString();
    }

    private static String digit2string(@IntRange(from = 0x0, to = 0xF) int vec) {
        final int asciiOfA = 65;
        if (vec <= 9) return String.valueOf(vec);
        else return Character.toString((char) (vec - 10 + asciiOfA));
    }

    public static int lightness(int color) {
        return (int2ri(color) + int2gi(color) + int2bi(color)) / 3;
    }

    // Encode components
    public static int rgba2int(int r, int g, int b, int a) {
        return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
    }

    // Decode components to int
    public static int int2ri(int color) {
        return (color >> 16) & 0xFF;
    }

    public static int int2gi(int color) {
        return (color >> 8) & 0xFF;
    }

    public static int int2bi(int color) {
        return color & 0xFF;
    }

    public static int int2ai(int color) {
        return (color >> 24) & 0xFF;
    }

    // Decode components to float
    public static float int2rf(int color) {
        return ((color >> 16) & 0xFF) / 255f;
    }

    public static float int2gf(int color) {
        return ((color >> 8) & 0xFF) / 255f;
    }

    public static float int2bf(int color) {
        return (color & 0xFF) / 255f;
    }

    public static float int2af(int color) {
        return ((color >> 24) & 0xFF) / 255f;
    }
}
