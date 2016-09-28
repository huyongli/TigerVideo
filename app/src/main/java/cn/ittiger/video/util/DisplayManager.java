/*******************************************************************************
 *
 *    Copyright (c) BaiNa Co. Ltd
 *
 *    DolphinCore
 *      DisplayManager.java
 *    DisplayManager
 *
 *    @author: DanLiu
 *    @since:  2010-12-15
 *    @version: 1.0
 *
 ******************************************************************************/

package cn.ittiger.video.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Random;

/**
 *
 */
public class DisplayManager {

    public final static float DENSITY = Resources.getSystem().getDisplayMetrics().density;
    public final static float SCALED_DENSITY = Resources.getSystem().getDisplayMetrics().scaledDensity;

    public final static int DEVICE_PHONE = -1;
    public final static int DEVICE_7INCH = 0;
    public final static int DEVICE_10INCH = 1;

    public final static int DEVICE_BELOW_5_INCH = -1;
    public final static int DEVICE_BETWEEN_5_AND_7_INCH = 0;

    private final static String TENDERLOIN = "tenderloin";
    private static boolean sIsSonyC1504 = Build.FINGERPRINT.contains("Sony") && Build.FINGERPRINT.contains("C1504");

    /*
     * This is galaxy tab size calculated by DPI, we don't use xDPI or yDPI as some manufacture's
     * xDPI and yDPI are very strange.
     */
    private final static float GALAXY_TAB_SIZE = 5f;

    /*
     * This is a middle value for Milestone and Galaxy Tab. We consider devices smaller than this as Phone,
     *  larger than this as Pad.
     *  changed from 4.5 to 4.6 cause some device with 540 * 960 resolution has a 4.5x size.
     *  changed from 4.6 to 4.72 cause gallaxy note with a size of 4.7169xx was a phone..
     */
    private final static float PAD_PHONE_THREDHOLD = 4.72f;
    private final static float PAD_TOP = 8.0f;

    private final static float DEVICE_5_INCH = 4.8f;

    private static int sScreenWidthDip;
    private static int sScreenHeightDip;
    private static int sScreenWidthPixels;
    private static int sScreenHeightPixels;

    public static float screenHeightPhysical(Context context) {
        Display display = getDisplay(context);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return (float)display.getHeight() / metrics.densityDpi;
    }

    public static float screenWidthPhysical(Context context) {
        Display display = getDisplay(context);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return (float)display.getWidth() / metrics.densityDpi;
    }

    public static float screenHeightPhysical2(Context context) {
        Display display = getDisplay(context);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return (float)display.getHeight() / metrics.ydpi;
    }

    public static float screenWidthPhysical2(Context context) {
        Display display = getDisplay(context);
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return (float)display.getWidth() / metrics.xdpi;
    }

    public static int dipToPixel(int dip) {
        if (dip < 0) {
            return -(int) (-dip * DENSITY + 0.5f);
        } else {
            return (int) (dip * DENSITY + 0.5f);
        }
    }

    public static int pixelToDip(int pixel) {
        if (pixel < 0) {
            return -(int) ((-pixel - 0.5f) / DENSITY);
        } else {
            return (int) ((pixel - 0.5f) / DENSITY);
        }
    }

    public static final void onConfigurationChanged() {
        sScreenHeightPixels = 0;
        sScreenWidthPixels = 0;
    }

    /**
     * Gets the width of the display, in pixels.
     *
     * @param context
     * @return
     */
    public static int screenWidthPixel(Context context) {

        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point outPoint = new Point();
            display.getRealSize(outPoint);
            sScreenWidthPixels = outPoint.x;
        }  else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point outPoint = new Point();
            display.getSize(outPoint);
            sScreenWidthPixels = outPoint.x;
        } else {
            sScreenWidthPixels = display.getWidth();
        }
        return sScreenWidthPixels;
    }

    /**
     * Gets the height of the display, in pixels.
     *
     * @param context
     * @return
     */
    public static int screenHeightPixel(Context context) {

        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point outPoint = new Point();
            display.getRealSize(outPoint);
            sScreenHeightPixels = outPoint.y;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point outPoint = new Point();
            display.getSize(outPoint);
            sScreenHeightPixels = outPoint.y;
        } else {
            sScreenHeightPixels = display.getHeight();
        }
        return sScreenHeightPixels;
    }

    public static int screenWidthDip(Context context) {

        if (sScreenWidthDip == 0) {
            sScreenWidthDip = pixelToDip(screenWidthPixel(context));
        }
        return sScreenWidthDip;
    }

    public static int screenHeightDip(Context context) {

        if (sScreenHeightDip == 0) {
            sScreenHeightDip = pixelToDip(screenHeightPixel(context));
        }
        return sScreenHeightDip;
    }

    // the Display.getRotation() method only exist on android 2.2
    @Deprecated
    public static int getRotation(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay().getRotation();
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static Display getDisplay(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay();
    }

    public static boolean isHardwareAccelerated(Canvas canvas) {
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                return (Boolean) Canvas.class.getDeclaredMethod("isHardwareAccelerated").invoke(
                        canvas);
            } catch (Exception e) {
            }
        }
        return false;
    }

    public static boolean isHardwareAccelerated(final View v) {
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                return v.isHardwareAccelerated();
            } catch (Exception e) {
            }
        }
        return false;
    }

    // private static final int LAYER_TYPE_NONE = 0;
    private static final int LAYER_TYPE_SOFTWARE = 1;
    private static final int LAYER_TYPE_HARDWARE = 2;

    public static void setHardwareAccelerated(View view) {
        if (Build.VERSION.SDK_INT >= 11) {
            SetLayerTypeWrapper.setLayerType(view, LAYER_TYPE_HARDWARE, null);
        }
    }

    public static void setSoftwareRendering(View view) {
        if (Build.VERSION.SDK_INT >= 11) {
            SetLayerTypeWrapper.setLayerType(view, LAYER_TYPE_SOFTWARE, null);
        }
    }

    private static class SetLayerTypeWrapper {

        public static void setLayerType(View view, int layerType, Paint paint) {
            view.setLayerType(layerType, paint);
        }
    }

    public static void setHardwareAccelerated(Window window) {
        if (Build.VERSION.SDK_INT >= 11) {
            window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        }
    }

    public static void setWindowBrightness(Window window, float brightness) {
        // 获取Activity窗口的属性
        WindowManager.LayoutParams lp = window.getAttributes();
        // 计算亮度值
        lp.screenBrightness = brightness;
        // 当亮度值设置成0时，会出现黑屏，手机只能重启
        if (lp.screenBrightness <= 0.03f && lp.screenBrightness != WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            lp.screenBrightness = 0.03f;
        }

        //当SonyC1504亮度值设置成0.1时，会出现黑屏
        if (lp.screenBrightness == 0.1f && sIsSonyC1504) {
            lp.screenBrightness = 0.7f;
        }
        // 设置亮度值
        window.setAttributes(lp);
    }

    public static float getDeviceSize(Context context) {

        final float width = screenWidthPhysical(context);
        final float height = screenHeightPhysical(context);
        final float physicalSize = (float) Math.sqrt(width * width + height * height);
        return physicalSize;
    }

    /***
     * 另外一种计算屏幕尺寸的方法，经过实际测试，发现这种方式得到的尺寸，相比于getDeviceSize,
     * 一般情况下更精准，但会在少量设备上会导致极端异常值
     * @param context
     * @return
     */
    public static float getDeviceSize2(Context context) {

        final float width = screenWidthPhysical2(context);
        final float height = screenHeightPhysical2(context);
        final float physicalSize = (float) Math.sqrt(width * width + height * height);
        return physicalSize;
    }

    /*
     * Whether it's a phone or pad(7inch or 10inch).
     */
    public static int getDeviceType(Context context) {

        final float physicalSize = getDeviceSize(context);
        if (physicalSize < PAD_PHONE_THREDHOLD) {
            return DEVICE_PHONE;
        } else if ((physicalSize > PAD_PHONE_THREDHOLD) && (physicalSize < PAD_TOP)) {
            return DEVICE_7INCH;
        } else {
            return DEVICE_10INCH;
        }
    }

    /*
     * Whether it's a phone or pad.
     */
    public static String getMobileDeviceType(Context context) {

        final float physicalSize = getDeviceSize(context);
        if (physicalSize < PAD_PHONE_THREDHOLD) {
            return "phone";
        } else {
            return "pad";
        }
    }

    /*
     * Whether it's a < 5inch device or 5_7inch device or 10inch device.
     */
    public static int getDeviceTypeForTab(Context context) {

        final float physicalSize = getDeviceSize(context);
        if (physicalSize < DEVICE_5_INCH) {
            return DEVICE_BELOW_5_INCH;
        } else if ((physicalSize > DEVICE_5_INCH) && (physicalSize < PAD_TOP)) {
            return DEVICE_BETWEEN_5_AND_7_INCH;
        } else {
            return DEVICE_10INCH;
        }
    }

    public static int dipToPixel(float dip) {
        if (dip < 0) {
            return -(int) (-dip * DENSITY + 0.5f);
        } else {
            return (int) (dip * DENSITY + 0.5f);
        }
    }

    public static boolean isPad(Context context) {
        return getDeviceType(context) != DisplayManager.DEVICE_PHONE;
    }

    /*
     * For debug purpose.
     */
    public static int getRandomColor() {
        final int colors[] = {
                Color.BLACK, Color.RED, Color.WHITE, Color.GRAY, Color.GREEN
        };
        final Random random = new Random();
        final int index = random.nextInt(colors.length);
        return colors[index];
    }

    public static float spToPixel(float value) {
        return value * SCALED_DENSITY;
    }

    public static boolean isPortrait(Context context) {

        Display display = getDisplay(context);
        return display.getWidth() < display.getHeight();
    }

    public static int getScreenOrientation(Context context) {

        if (isPortrait(context)) {
            return Configuration.ORIENTATION_PORTRAIT;
        } else {
            return Configuration.ORIENTATION_LANDSCAPE;
        }
    }

    // 判断是否是tenderloin这个特定平板 1024*768
    public static boolean isTenderloin() {

        return Build.DEVICE.equalsIgnoreCase(TENDERLOIN);
    }

    // return different short cut icon size according to difference screen size
    public static float getShortcutIconPixelSize() {
        if (Float.compare(DENSITY, 1.0f) < 0) { // ldpi, 36px
            return 36;
        } else if (Float.compare(DENSITY, 1.0f) == 0) { // mdpi, 48px
            return 48;
        } else if (Float.compare(DENSITY, 1.5f) == 0) { // hdpi, 72px
            return 72;
        } else if (Float.compare(DENSITY, 1.0f) > 0) {
            return 96;
        }

        // default use 72px to create shortcut
        return 72;
    }

    private static Boolean sIsTablet = null;
    private static Boolean sIsSmallScreen = null;

    public static boolean isTablet(Context context) {
        if (null == context && null == sIsTablet) {
            throw new IllegalArgumentException("context maynot be null.");
        }
        if (null == sIsTablet) {
            Resources resources = context.getResources();
            int id = resources.getIdentifier("is_tablet", "bool", context.getPackageName());
            if (id != 0) {
                sIsTablet = resources.getBoolean(id);
            } else {
                sIsTablet = false;
            }
        }
        return sIsTablet;
    }

    public static boolean isSmallScreenDevice(Context context) {
        if (null == context && null == sIsSmallScreen) {
            throw new IllegalArgumentException("context maynot be null.");
        }
        if (null == sIsSmallScreen) {
            sIsSmallScreen = Math.max(screenWidthDip(context), screenHeightDip(context)) <= 480;
        }

        return sIsSmallScreen;
    }

    /**
    *
    * @param activity
    * @return > 0 success; <= 0 fail
    */
   public static int getStatusBarHeight(Activity activity){
       int statusHeight = 0;
       Rect localRect = new Rect();
       activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
       statusHeight = localRect.top;
       if (0 == statusHeight){
           Class<?> localClass;
           try {
               localClass = Class.forName("com.android.internal.R$dimen");
               Object localObject = localClass.newInstance();
               int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
               statusHeight = activity.getResources().getDimensionPixelSize(i5);
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       return statusHeight;
   }
}

