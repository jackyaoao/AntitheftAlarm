package com.antitheft.alarm;

import android.app.Application;
import android.content.Context;

import com.inuker.bluetooth.library.BluetoothContext;
import com.tencent.bugly.crashreport.CrashReport;


/**
 * <The trouble with the world is that the stupid are sure and the intelligent are full of doubt.>
 * <p>
 * AppContext
 * <p>
 * 作者：Jacky.Ao on 2019/11/16 15:44
 * <p>
 * 邮箱: jiazhi.ao@gmail.com
 */


public class AppContext extends Application {

    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        BluetoothContext.set(this);
        CrashReport.initCrashReport(getApplicationContext(), "459fb882b6", false);
    }

    public static Context getContext() {
        return context;
    }
}
