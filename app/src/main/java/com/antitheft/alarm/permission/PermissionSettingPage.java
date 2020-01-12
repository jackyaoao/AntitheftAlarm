package com.antitheft.alarm.permission;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

final class PermissionSettingPage {

    private static final String MARK = Build.MANUFACTURER.toLowerCase();

    /**
     * 跳转到应用权限设置页面
     *
     * @param context 上下文对象
     * @param newTask 是否使用新的任务栈启动
     */
    static void start(Context context, boolean newTask) {

        Intent intent = null;

        if (intent == null || !hasIntent(context, intent)) {
            intent = google(context);
        }

        if (newTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        try {
            context.startActivity(intent);
        } catch (Exception ignored) {
            intent = google(context);
            context.startActivity(intent);
        }
    }

    private static Intent google(Context context) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        return intent;
    }

    private static boolean hasIntent(Context context, Intent intent) {
        return context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }
}