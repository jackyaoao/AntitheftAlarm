package com.antitheft.alarm.model;

import com.antitheft.alarm.btkit.BluetoothManager;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.Log;
import com.antitheft.alarm.utils.MyPrefs;
import com.inuker.bluetooth.library.Constants;

public class LibraState {

    private static LibraState instance;
    private DetailItem detailItem;
    private int alarmType = Const.ALARM_TYPE_UNKNOWN;

    private LibraState() {
        if (MyPrefs.getInstance().getInt(Const.APPID) < 0) {
            MyPrefs.getInstance().put(Const.APPID, (int) ((Math.random() * 9 + 1) * 10000000));
        }
    }

    public static LibraState getInstance() {
        if (instance == null) {
            synchronized (LibraState.class) {
                if (instance == null) {
                    instance = new LibraState();
                }
            }
        }
        return instance;
    }

    public int getLibraState() {
        int ret = BluetoothManager.getClient().getConnectStatus(getMac()) == Constants.STATUS_DEVICE_CONNECTED
                ? Const.STATUS_DEVICE_CONNECTED : Const.STATUS_DEVICE_DISCONNECTED;
        Log.i("getLibraState " + ret);
        return ret;
    }

    public DetailItem getDetailItem() {
        return detailItem;
    }

    public void setDetailItem(DetailItem detailItem) {
        this.detailItem = detailItem;
    }

    public String getMac() {
        return MyPrefs.getInstance().getString(Const.CONNECTED_MAC);
    }

    public void setMac(String mac) {
        MyPrefs.getInstance().put(Const.CONNECTED_MAC, mac);
    }

    public String getAppId() {
        return MyPrefs.getInstance().getInt(Const.APPID) + "";
    }

    public int getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }
}
