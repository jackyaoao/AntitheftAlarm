package com.antitheft.alarm.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.antitheft.alarm.AppContext;
import com.antitheft.alarm.utils.Log;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        Log.i("onReceive action " + action);
        if (action != null && (action.equals("android.intent.action.BOOT_COMPLETED") ||
                action.equals("android.intent.action.TIME_TICK"))) {
            startService(context, action);
        }
    }

    private void startService(Context context, String action) {
        Intent it = new Intent(AppContext.getContext(), AntitheftAlarmService.class);
        it.setAction(action);
        context.startService(it);
    }
}
