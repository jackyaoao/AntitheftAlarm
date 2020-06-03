package com.antitheft.alarm.activity;

import androidx.fragment.app.FragmentActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.widget.Toast;

import com.antitheft.alarm.AppContext;
import com.antitheft.alarm.IAntitheftAlarmService;
import com.antitheft.alarm.IServiceDataListener;
import com.antitheft.alarm.btkit.BluetoothManager;
import com.antitheft.alarm.model.DetailItem;
import com.antitheft.alarm.model.LibraState;
import com.antitheft.alarm.service.AntitheftAlarmService;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.Log;

import com.antitheft.alarm.utils.MyPrefs;
import com.antitheft.alarm.utils.SystemUtils;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchResult;

import java.util.List;

public class BaseActivity extends FragmentActivity {

    private SystemUtils.ServiceToken serviceToken;
    protected IAntitheftAlarmService antitheftAlarmService;
    protected String mac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mac = LibraState.getInstance().getMac();
        //checkOverlayPermission();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (serviceToken == null) {
            serviceToken = SystemUtils.bindToService(this, mConnection);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SystemUtils.unbindFromService(serviceToken);
        serviceToken = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void showShortToast(String message) {
        Toast.makeText(AppContext.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    protected void showLongToast(String message) {
        Toast.makeText(AppContext.getContext(), message, Toast.LENGTH_LONG).show();
    }

    protected String getResString(int resId) {
        return AppContext.getContext().getResources().getString(resId);
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                }else {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    Toast.makeText(this, "未被授予权限，相关功能不可用", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            int id = 0;
            antitheftAlarmService = IAntitheftAlarmService.Stub.asInterface(service);
            if (antitheftAlarmService != null) {
                Log.i("MainActivity bindService.");
                try {
                    antitheftAlarmService.registerServiceDataListener(new ServiceDataCallbackImp());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                onBound();
            }
        }
        // Called when the connection with the service disconnects unexpectedly
        public void onServiceDisconnected(ComponentName className) {
            try {
                antitheftAlarmService.unRegisterServiceDataListener(null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            antitheftAlarmService = null;
        }
    };

    private class ServiceDataCallbackImp extends IServiceDataListener.Stub {

        @Override
        public void onUsePassword() {
            BaseActivity.this.onUsePassword();
        }

        @Override
        public void onSucceeded() {
            BaseActivity.this.onSucceeded();
        }

        @Override
        public void onFailed() {
            BaseActivity.this.onFailed();
        }

        @Override
        public void onError(int code, String reason) {
            BaseActivity.this.onError(code, reason);
        }

        @Override
        public void onSearchStarted() {
            BaseActivity.this.onSearchStarted();
        }

        @Override
        public void onDevicesFounded(List<SearchResult> devices) {
            BaseActivity.this.onDevicesFounded(devices);
        }

        @Override
        public void onSearchStopped() {
            BaseActivity.this.onSearchStopped();
        }

        @Override
        public void onSearchCanceled() {
            BaseActivity.this.onSearchCanceled();
        }

        @Override
        public void onConnectedResponse(int code, BleGattProfile profile) {
            BaseActivity.this.onConnectedResponse(code, profile);
        }

        @Override
        public void onReadResponse(int code, byte[] data) {
            BaseActivity.this.onReadResponse(code, data);
        }

        @Override
        public void onWriteResponse(int code, int event) {
            BaseActivity.this.onWriteResponse(code, event);
        }

        @Override
        public void onNotify(DetailItem item, byte[] data) {
            BaseActivity.this.onNotify(item, data);
        }

        @Override
        public void onNotifyResponse(int code) {
            BaseActivity.this.onNotifyResponse(code);
        }

        @Override
        public void onUnNotifyResponse(int code) {
            BaseActivity.this.onUnNotifyResponse(code);
        }
    }

    public void onUsePassword() {

    }

    public void onSucceeded() {

    }

    public void onFailed() {

    }

    public void onError(int code, String reason) {

    }

    public void onSearchStarted() {

    }

    public void onDevicesFounded(List<SearchResult> devices) {

    }

    public void onSearchStopped() {

    }

    public void onSearchCanceled() {

    }

    public void onConnectedResponse(int code, BleGattProfile profile) {

    }

    public void onReadResponse(int code, byte[] data) {

    }

    public void onWriteResponse(int code, int event) {

    }

    public void onNotify(DetailItem item, byte[] data) {

    }

    public void onNotifyResponse(int code) {

    }

    public void onUnNotifyResponse(int code) {

    }

    public void onBound() {

    }

    /** Biometrics*/
    public void authenticate() {
        try {
            antitheftAlarmService.authenticate();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** BLE commuication*/
    public void bleStartSearch() {
        if (!BluetoothManager.getClient().isBluetoothOpened()) {
            BluetoothManager.getClient().openBluetooth();
        }
        try {
            antitheftAlarmService.searchBle();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void bleStopSearch() {
        try {
            antitheftAlarmService.stopBle();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void bleConnect(String mac) {
        try {
            antitheftAlarmService.connect(mac);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void bleDisconnect(String mac) {
        try {
            antitheftAlarmService.disconnect(mac);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void alarmStart() {
        try {
            antitheftAlarmService.play("alarm", true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void alarmStop() {
        try {
            antitheftAlarmService.stop();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void bleWrite(String mac, DetailItem item, byte[] content, int event) {
        try {
            antitheftAlarmService.write(mac, item, content, event);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void bleRead(String mac, DetailItem item) {
        try {
            antitheftAlarmService.read(mac, item);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void startInLineService(String action) {
        Intent it = new Intent(this, AntitheftAlarmService.class);
        it.setAction(action);
        startService(it);
    }

    public void bleRegisterConnectListenter(String mac) {
        Intent it = new Intent(this, AntitheftAlarmService.class);
        it.setAction(AntitheftAlarmService.BLE_REGISTER_BT_STATE_ACTION);
        it.putExtra(Const.CONNECTED_MAC, mac);
        startService(it);
    }

    public void bleReStartRead() {
        startInLineService(AntitheftAlarmService.BLE_EVENT_RESTART_READ_ACTION);
    }

    public void bleUnRegisterConnectListenter(String mac) {
        Intent it = new Intent(this, AntitheftAlarmService.class);
        it.setAction(AntitheftAlarmService.BLE_UNREGISTER_BT_STATE_ACTION);
        it.putExtra(Const.CONNECTED_MAC, mac);
        startService(it);
    }

    public void cancelAlarm() {
        alarmStop();
        if (LibraState.getInstance().getAlarmType() == Const.ALARM_TYPE_ALARM) {
            bleWrite(LibraState.getInstance().getMac(), LibraState.getInstance().getDetailItem(),
                    SystemUtils.getSendContent(Const.BLE_REMOVE_ALARM_W_EVENT), Const.BLE_REMOVE_ALARM_W_EVENT);
        } else if (LibraState.getInstance().getAlarmType() == Const.ALARM_TYPE_FIND) {
            bleWrite(MyPrefs.getInstance().getString(Const.CONNECTED_MAC), LibraState.getInstance().getDetailItem(),
                    SystemUtils.getSendContent(Const.BLE_FOUND_ACK_W_EVENT), Const.BLE_FOUND_ACK_W_EVENT);
        }
    }
}
