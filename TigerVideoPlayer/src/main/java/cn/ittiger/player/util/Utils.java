package cn.ittiger.player.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * @author: ylhu
 * @time: 17-9-11
 */

public class Utils {
    private static final String UNKNOWN_SIZE = "00:00";

    /**
     * 转换视频时长(s)为时分秒的展示格式
     * @param miliseconds   视频总时长，单位毫秒
     * @return
     */
    public static String formatVideoTimeLength(long miliseconds) {

        int seconds = (int) (miliseconds / 1000);

        String formatLength;
        if(seconds == 0) {
            formatLength = UNKNOWN_SIZE;
        } else if(seconds < 60) {//小于1分钟
            formatLength = "00:" + (seconds < 10 ? "0" + seconds : seconds);
        } else if(seconds < 60 * 60) {//小于1小时
            long sec = seconds % 60;
            long min = seconds / 60;
            formatLength = (min < 10 ? "0" + min : String.valueOf(min)) + ":" +
                    (sec < 10 ? "0" + sec : String.valueOf(sec));
        } else {
            long hour = seconds / 3600;
            long min = seconds % 3600 / 60;
            long sec = seconds % 3600 % 60;
            formatLength = (hour < 10 ? "0" + hour : String.valueOf(hour)) + ":" +
                    (min < 10 ? "0" + min : String.valueOf(min)) + ":" +
                    (sec < 10 ? "0" + sec : String.valueOf(sec));
        }
        return formatLength;
    }

    public static void showViewIfNeed(View view) {

        if(view.getVisibility() == View.GONE || view.getVisibility() == View.INVISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void hideViewIfNeed(View view) {

        if(view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        }
    }

    public static boolean isViewShown(View view) {

        return view.getVisibility() == View.VISIBLE;
    }

    public static boolean isViewHide(View view) {

        return view.getVisibility() == View.GONE || view.getVisibility() == View.INVISIBLE;
    }

    public static void log(String message) {

        Log.d("__VideoPlayer__", message);
    }

    public static void logTouch(String message) {

        Log.d("__GestureTouch__", message);
    }

    /**
     * Get activity from context object
     *
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    public static Activity getActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return getActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;
    }

    /**
     * 返回屏幕宽度Px
     *
     * @param context
     * @return int
     */
    public static int getWindowWidth(Context context) {

        int screenWidthPixels = 0;

        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point outPoint = new Point();
            display.getRealSize(outPoint);
            screenWidthPixels = outPoint.x;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point outPoint = new Point();
            display.getSize(outPoint);
            screenWidthPixels = outPoint.x;
        } else {
            screenWidthPixels = display.getWidth();
        }
        return screenWidthPixels;
    }

    /**
     * 返回屏幕高度
     *
     * @param context
     * @return int
     */
    public static int getWindowHeight(Context context) {

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay()
                .getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 应用缓存目录，可以通过手机中的清除缓存把数据清除掉
     *
     * @param context
     * @return
     */
    public static String getCacheDir(Context context) {

        return context.getExternalCacheDir().getAbsolutePath() + "/VideoCache";
    }

    /**
     * 判断网络连接是否有效（此时可传输数据）。
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>}</p>
     * 如果未添加此权限则返回true
     *
     * @return boolean 不管wifi，还是mobile net，只有当前在连接状态（可有效传输数据）才返回true,反之false。
     */
    public static boolean isConnected(Context context) {
        try {
            NetworkInfo net = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            return net != null && net.isConnected();
        } catch(Exception e) {
            return true;
        }
    }
}
