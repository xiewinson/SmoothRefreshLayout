/*
 *  Copyright (c) 2016. Istuary Innovation Group, Ltd.
 *  Unauthorized copying of this file, via any medium is strictly prohibited proprietary and
 *  confidential.
 *  Created on Tue, 27 Sep 2016 14:56:35 +0800.
 *  ProjectName: IronHide; ModuleName: IronHideLibrary; ClassName: DeviceUtil.java.
 *  Author: Lena; Last Modified: Tue, 27 Sep 2016 14:56:35 +0800.
 *  This file is originally created by Lena.
 *
 */

package io.github.xiewinson.smoothrefreshlayout.library;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Window;
import android.view.WindowManager;

public class DeviceUtil {

    public static int sScreenWidth;
    public static int sScreenHeight;
    public static float sScreenDensity;
    public static float sScreenPPI;
    public static float sScreenInch;
    public static int sActionBarSize;
    public static String sAppVersion;
    public static String sSystemInfo;
    public static String sDeviceModel;
    public static String sSystemType;
    public static int sStatusBarHeight;

    public static void init(Context context) {
        WindowManager windowManger = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        sScreenWidth = getScreenWidth(windowManger);
        sScreenHeight = getScreenHeight(windowManger);
        sScreenDensity = getScreenDensity(windowManger);
        sScreenPPI = getScreenPPI(windowManger);
        sScreenInch = getScreenInch();
        sActionBarSize = getActionBarSize(context);
        sStatusBarHeight = getStatusBarHeight(context.getResources());
    }

    private static int getActionBarSize(Context context) {
        TypedValue typedValue = new TypedValue();
        int[] textSizeAttr = new int[]{android.R.attr.actionBarSize};
        int indexOfAttrTextSize = 0;
        TypedArray a = context.obtainStyledAttributes(typedValue.data, textSizeAttr);
        int actionBarSize = a.getDimensionPixelSize(indexOfAttrTextSize, -1);
        a.recycle();
        return actionBarSize;
    }

    public static int getPxByDp(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int getPxByDp(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int getDpByPx(Context context, int px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static float pxToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return px / scaledDensity;
    }

    private static int getScreenWidth(WindowManager windowManger) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManger.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private static int getScreenHeight(WindowManager windowManger) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManger.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }

    private static float getScreenDensity(WindowManager windowManger) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManger.getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    private static float getScreenPPI(WindowManager windowManger) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManger.getDefaultDisplay().getMetrics(metrics);
        return metrics.densityDpi;
    }

    private static float getScreenInch() {
        return (float) Math.sqrt((Math.pow(sScreenHeight, 2) + Math.pow(sScreenWidth, 2))) / sScreenPPI;

    }

    public float getStatusBarHight(Window window) {
        Rect rectgle = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int statusBarHeight = rectgle.top;
        return statusBarHeight;
    }

    public void setScreenMode(Activity activity, boolean fullscreen) {
        if (fullscreen) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private static int getStatusBarHeight(Resources resources) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return 0;
        }
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }

        return result;
    }

}
