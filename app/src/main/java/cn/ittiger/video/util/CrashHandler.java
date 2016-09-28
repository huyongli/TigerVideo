package cn.ittiger.video.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CrashHandler implements UncaughtExceptionHandler {
    private static final String VERSION_NAME = "versionName = ";
    private static final String VERSION_CODE = "versionCode = ";
    public static final String TAG = "CrashHandler";
    public static final boolean DEBUG = true;
    private UncaughtExceptionHandler mDefaultHandler;
    private static CrashHandler INSTANCE;
    private Context mContext;

    private String mLogPath = "";


    private CrashHandler() {

    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {

        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象, 获取系统默认的UncaughtException处理器, 设置该CrashHandler为程序的默认处理器
     *
     * @param ctx
     */
    public void init(Context ctx) {

        mContext = ctx;
        this.mLogPath = ctx.getCacheDir().getAbsolutePath() + "/crash";
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public void setLogPath(String logPath) {

        this.mLogPath = logPath;
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */

    public void uncaughtException(Thread thread, Throwable ex) {

        String logdir = mLogPath;
        File file = new File(logdir);
        boolean mkSuccess;
        if (!file.isDirectory()) {
            mkSuccess = file.mkdirs();
            if (!mkSuccess) {
                mkSuccess = file.mkdirs();
            }
        }

        StringBuffer sb = new StringBuffer();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            String time = formatter.format(new Date());
            String logFile = logdir + File.separator + time + ".log";

            FileOutputStream fos = new FileOutputStream(logFile);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "an error occured while writing file...", e);
        }

        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error : ", e);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {

        if (ex == null) {
            return true;
        }
        final String msg = ex.getLocalizedMessage();
        new Thread() {
            public void run() {

                Looper.prepare();
                Toast.makeText(mContext, "程序出错啦:" + msg, Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();
        return true;
    }

    /**
     * 收集程序崩溃的设备信息
     *
     * @param ctx
     */
    public String collectCrashDeviceInfo(Context ctx) {

        StringBuffer sb = new StringBuffer();
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                sb.append(VERSION_NAME);
                sb.append(pi.versionName == null ? "not set" : pi.versionName);
                sb.append("\n");
                sb.append(VERSION_CODE);
                sb.append("" + pi.versionCode);
                sb.append("\n");
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Error while collect package info", e);
            sb.append(VERSION_NAME);
            sb.append(e.getMessage());
            sb.append("\n");
        }
        //使用反射来收集设备信息.在Build类中包含各种设备信息,
        //例如: 系统版本号,设备生产商 等帮助调试程序的有用信息
        //具体信息请参考后面的截图
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                sb.append(field.getName());
                sb.append(" = ");
                sb.append(field.get(null));
                sb.append("\n");
                if (DEBUG) {
                    Log.d(TAG, field.getName() + " : " + field.get(null));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error while collect crash info", e);
            }
        }
        return sb.toString();
    }
}
