package com.icolor.utils;

import static java.lang.Math.round;

public class ColorUtil {

    public static int WHITE = 0xFFFFFFFF;
    public static int BLACK = 0x00000000;

    public static int blend(int color1, int color2, float fraction) {
        int r = (int) round(int2ri(color1) * (1 - fraction) + int2ri(color2) * fraction);
        int g = (int) round(int2gi(color1) * (1 - fraction) + int2gi(color2) * fraction);
        int b = (int) round(int2bi(color1) * (1 - fraction) + int2bi(color2) * fraction);
        int a = (int) round(int2ai(color1) * (1 - fraction) + int2ai(color2) * fraction);
        return rgba2int(r, g, b, a);
    }

    // Color format conversions

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
