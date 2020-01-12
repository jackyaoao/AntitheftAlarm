package com.antitheft.alarm;

import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.antitheft.alarm.model.DetailItem;

interface IServiceDataListener {
    /** Biometrics*/
    void onUsePassword();
    void onSucceeded();
    void onFailed();
    void onError(in int code, in String reason);
    /** BLE commuication*/
    void onSearchStarted();
    void onDevicesFounded(in List<SearchResult> device);
    void onSearchStopped();
    void onSearchCanceled();
    void onConnectedResponse(int code, in BleGattProfile profile);
    void onReadResponse(in int code, in byte[] data);
    void onWriteResponse(in int code, in int event);
    void onNotify(in DetailItem item, in byte[] data);
    void onNotifyResponse(in int code);
    void onUnNotifyResponse(in int code);
}
