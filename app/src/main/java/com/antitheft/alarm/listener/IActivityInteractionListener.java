package com.antitheft.alarm.listener;

import com.antitheft.alarm.model.DetailItem;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchResult;

import java.util.List;

public interface IActivityInteractionListener {
    /** Biometrics*/
    void onUsePassword();
    void onSucceeded();
    void onFailed();
    void onError(int code, String reason);
    /** BLE commuication*/
    void onSearchStarted();
    void onDevicesFounded(List<SearchResult> device);
    void onSearchStopped();
    void onSearchCanceled();
    void onConnectedResponse(int code, BleGattProfile profile);
    void onReadResponse(int code, byte[]data);
    void onWriteResponse(int code, int event);
    void onBackPressed();
    void onConnectStatusChanged(String mac, int status);
    void onBluetoothStateChanged(boolean openOrClosed);
    void onNotify(DetailItem item, byte[] data);
    void onNotifyResponse(int code);
    void onUnNotifyResponse(int code);

}
