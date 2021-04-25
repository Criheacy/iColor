package com.icolor.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import static android.content.Context.CLIPBOARD_SERVICE;

public class WindowUtil {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void setFullScreen(Activity activity) {
        // Hide notification bar
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Hide bottom navigation bar
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        activity.getWindow().setAttributes(params);
    }

    public static void setKeepScreenOn(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams. FLAG_KEEP_SCREEN_ON);
    }

    public static boolean pointInView(Point point, View view) {
        Rect viewRect = new Rect();
        int[] offset = new int[2];
        view.getDrawingRect(viewRect);
        view.getLocationInWindow(offset);
        viewRect.offset(offset[0], offset[1]);
        return viewRect.contains(point.x, point.y);
    }

    public static float dps2dp(float dps, Context context) {
        return dps * context.getResources().getDisplayMetrics().density;
    }

    public static void copyToClipBoard(String valueString, String label, Activity activity) {
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, valueString);
        clipboard.setPrimaryClip(clip);
    }
}
