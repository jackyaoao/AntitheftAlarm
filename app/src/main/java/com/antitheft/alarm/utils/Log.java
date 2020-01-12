package com.antitheft.alarm.utils;

public class Log {

    private static final String TAG = Const.TAG;

    /**
     * debug log
     *
     * @param format
     * @param args
     */
    public static void d(String format, Object... args) {
        logMessage(android.util.Log.DEBUG, format, args);
    }

    /**
     * info log
     *
     * @param format
     * @param args
     */
    public static void i(String format, Object... args) {
        logMessage(android.util.Log.INFO, format, args);
    }

    /**
     * error log
     *
     * @param format
     * @param args
     */
    public static void e(String format, Object... args) {
        logMessage(android.util.Log.ERROR, format, args);
    }

    /**
     * print log
     *
     * @param level
     * @param format
     * @param args
     */
    private static void logMessage(int level, String format, Object... args) {

        String formattedString = String.format(format, args);
        switch (level) {
            case android.util.Log.DEBUG:
                android.util.Log.d(TAG, formattedString);
                break;
            case android.util.Log.INFO:
                android.util.Log.i(TAG, formattedString);
                break;
            case android.util.Log.ERROR:
                android.util.Log.e(TAG, formattedString);
                break;
        }
    }
}