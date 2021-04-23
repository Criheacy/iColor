package com.icolor.utils;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

public class WindowUtil {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void SetFullScreen(Activity activity) {
        // Hide notification bar
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Hide bottom navigation bar
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_IMMERSIVE;
        activity.getWindow().setAttributes(params);
    }

    public static void SetKeepScreenOn(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams. FLAG_KEEP_SCREEN_ON);
    }
}
