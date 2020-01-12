// IAntitheftAlarmService.aidl
package com.antitheft.alarm;

// Declare any non-default types here with import statements

import com.antitheft.alarm.IServiceDataListener;
import com.antitheft.alarm.model.DetailItem;

interface IAntitheftAlarmService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    /** Biometrics*/
    void biometricenable();
    void authenticate();
    /** play alarm-bell*/
    void initMediaPlayer();
    void releaseMediaPlayer();
    void play(in String name, in boolean isLoop);
    void stop();
    /** BLE commuication*/
    void openBluetooth();
    void closeBluetooth();
    void searchBle();
    void stopBle();
    void connect(in String mac);
    void disconnect(in String mac);
    void write(in String mac, in DetailItem item, in byte[] content, in int event);
    void read(in String mac, in DetailItem item);
    void bleNotify(in String mac, in DetailItem item);
    void bleUnnotify(in String mac, in DetailItem item);
    /** register data callback*/
    void registerServiceDataListener(IServiceDataListener listener);
    void unRegisterServiceDataListener(IServiceDataListener listener);
}
