package ca.TransCanadaTrail.TheGreatTrail.utils;


import ca.TransCanadaTrail.TheGreatTrail.BuildConfig;

public class Logger {

    private static boolean IS_DEBUG = BuildConfig.DEBUG;

    public static void enableLog(boolean enable) {
        IS_DEBUG = enable;
    }

    public static String tag = "LOGGER --------> ";

    public static void obj(Object object) {
        try {
            if (IS_DEBUG)
                android.util.Log.i(tag, String.valueOf(object));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void obj(String desc, Object object) {
        try {
            if (IS_DEBUG)
                android.util.Log.i(tag + " " + desc + ": ", String.valueOf(object));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void i(String msg) {
        if (IS_DEBUG)
            android.util.Log.i(tag, msg);
    }

    public static void d(String msg) {
        if (IS_DEBUG)
            android.util.Log.d(tag, msg);
    }

    public static void e(String msg) {
        if (IS_DEBUG)
            android.util.Log.e(tag, msg);
    }

    public static void v(String msg) {
        if (IS_DEBUG)
            android.util.Log.v(tag, msg);
    }

    public static void w(String msg) {
        if (IS_DEBUG)
            android.util.Log.w(tag, msg);
    }

    public static void i(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.i(tag, msg, tr);
    }

    public static void d(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.d(tag, msg, tr);
    }

    public static void e(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.e(tag, msg, tr);
    }

    public static void v(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.v(tag, msg, tr);
    }

    public static void w(String msg, Throwable tr) {
        if (IS_DEBUG)
            android.util.Log.w(tag, msg, tr);
    }


// ----------------------------------------------- Log using class tag

    private static String getTag(Object clazz) {
        String tag = null;
        if (clazz != null) {
            tag = clazz.getClass().getSimpleName();
        } else {
            tag = "Unknown";
        }
        return tag;
    }

    public static void i(Object clazz, String msg) {
        if (IS_DEBUG) {
            Logger.i(getTag(clazz), msg != null ? msg : "Null");
        }
    }

    public static void d(Object clazz, String msg) {
        if (IS_DEBUG) {
            Logger.d(getTag(clazz), msg != null ? msg : "Null");
        }
    }

    public static void w(Object clazz, String msg) {
        if (IS_DEBUG) {
            Logger.w(getTag(clazz), msg != null ? msg : "Null");
        }
    }

    public static void e(Object clazz, String msg) {
        if (IS_DEBUG) {
            Logger.e(getTag(clazz), msg != null ? msg : "Null");
        }
    }

    public static void v(Object clazz, String msg) {
        if (IS_DEBUG) {
            Logger.v(getTag(clazz), msg != null ? msg : "Null");
        }
    }

    public static boolean isShowLog() {
        return IS_DEBUG;
    }

    public static void setShowLog(boolean showLog) {
        Logger.IS_DEBUG = showLog;
    }

    public static void i(Object clazz, String msg, boolean newShowLog) {
        if (IS_DEBUG) {
            Logger.i(getTag(clazz), msg != null ? msg : "Null");
        }
        IS_DEBUG = newShowLog;
    }

    public static void d(Object clazz, String msg, boolean newShowLog) {
        if (IS_DEBUG) {
            Logger.d(getTag(clazz), msg != null ? msg : "Null");
        }
        IS_DEBUG = newShowLog;
    }

    public static void w(Object clazz, String msg, boolean newShowLog) {
        if (IS_DEBUG) {
            Logger.w(getTag(clazz), msg != null ? msg : "Null");
        }
        IS_DEBUG = newShowLog;
    }

    public static void e(Object clazz, String msg, boolean newShowLog) {
        if (IS_DEBUG) {
            Logger.e(getTag(clazz), msg != null ? msg : "Null");
        }
        IS_DEBUG = newShowLog;
    }

    public static void v(Object clazz, String msg, boolean newShowLog) {
        if (IS_DEBUG) {
            Logger.v(getTag(clazz), msg != null ? msg : "Null");
        }
        IS_DEBUG = newShowLog;
    }

    //Custom TAG
    public static void i(String TAG, String msg) {
        if (IS_DEBUG)
            android.util.Log.i(TAG, msg);
    }

    public static void d(String TAG, String msg) {
        if (IS_DEBUG)
            android.util.Log.d(TAG, msg);
    }

    public static void e(String TAG, String msg) {
        if (IS_DEBUG)
            android.util.Log.e(TAG, msg);
    }
}
