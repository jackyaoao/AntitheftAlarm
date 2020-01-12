package com.antitheft.alarm.btkit;

import com.antitheft.alarm.AppContext;
import com.inuker.bluetooth.library.BluetoothClient;

public class BluetoothManager {

    private static BluetoothClient client;

    private BluetoothManager() {

    }

    public static BluetoothClient getClient() {
        if (client == null) {
            synchronized (BluetoothManager.class) {
                if (client == null) {
                    client = new BluetoothClient(AppContext.getContext());
                }
            }
        }
        return client;
    }
}
