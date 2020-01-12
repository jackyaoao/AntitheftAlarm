package com.antitheft.alarm.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.SparseArray;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.antitheft.alarm.AppContext;
import com.antitheft.alarm.R;
import com.antitheft.alarm.btkit.BluetoothManager;
import com.antitheft.alarm.fragments.AuthorizationFragment;
import com.antitheft.alarm.fragments.BaseFragment;
import com.antitheft.alarm.fragments.CreateAccountFragment;
import com.antitheft.alarm.fragments.DeviceFoundFragment;
import com.antitheft.alarm.fragments.DevicePairingFragment;
import com.antitheft.alarm.fragments.FragmentStack;
import com.antitheft.alarm.fragments.InputPsdFragment;
import com.antitheft.alarm.fragments.LibraConnectStateFragment;
import com.antitheft.alarm.fragments.LibraProtectedFragment;
import com.antitheft.alarm.fragments.SettingFragment;
import com.antitheft.alarm.listener.IFragmentInteractionListener;
import com.antitheft.alarm.model.DetailItem;
import com.antitheft.alarm.model.LibraState;
import com.antitheft.alarm.service.AntitheftAlarmService;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.Log;
import com.antitheft.alarm.utils.MyPrefs;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchResult;
import java.util.List;

import static com.antitheft.alarm.utils.Const.AUTHORIZATION_ID;
import static com.antitheft.alarm.utils.Const.CREATE_ACCOUNT_ID;
import static com.antitheft.alarm.utils.Const.DEVICE_FOUND_ID;
import static com.antitheft.alarm.utils.Const.DEVICE_PAIRING_ID;
import static com.antitheft.alarm.utils.Const.INPUT_PASSWORD_ID;
import static com.antitheft.alarm.utils.Const.LIBRA_CONNECTED_STATE_ID;
import static com.antitheft.alarm.utils.Const.LIBRA_PROTECTED_ID;
import static com.antitheft.alarm.utils.Const.SETTING_FRAGMENT_ID;

public class MainActivity extends BaseActivity implements IFragmentInteractionListener {
    private static final String TAG = "LibraMainActivity";
    private SparseArray<BaseFragment> fragmentArrayList = new SparseArray<>();
    private Object arg = null;
    private List<String> deniedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG + " onStart");
        initFragments();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AntitheftAlarmService.BLE_STATE_ACTION);
        filter.addAction(AntitheftAlarmService.BLE_CONNECT_STATUS_ACTION);
        registerReceiver(bleConnectStateReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG + " onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG + " onResume");
    }

    private void initFragments() {
        fragmentArrayList.clear();
        fragmentArrayList.put(CREATE_ACCOUNT_ID, CreateAccountFragment.newInstance());
        fragmentArrayList.put(INPUT_PASSWORD_ID, InputPsdFragment.newInstance());
        fragmentArrayList.put(AUTHORIZATION_ID, AuthorizationFragment.newInstance());
        fragmentArrayList.put(DEVICE_PAIRING_ID, DevicePairingFragment.newInstance());
        fragmentArrayList.put(LIBRA_CONNECTED_STATE_ID, LibraConnectStateFragment.newInstance());
        fragmentArrayList.put(LIBRA_PROTECTED_ID, LibraProtectedFragment.newInstance());
        fragmentArrayList.put(DEVICE_FOUND_ID, DeviceFoundFragment.newInstance());
        fragmentArrayList.put(SETTING_FRAGMENT_ID, SettingFragment.newInstance());
    }

    public void showFragment(int id, Object arg) {
        Log.i(TAG + "showFragment id: " + id);
        BaseFragment fragment = fragmentArrayList.get(id);
        if (fragment != null) {
            fragment.setParentActivity(this);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            fragment.setArg(arg);
            transaction.replace(R.id.container, fragment);
            transaction.commitAllowingStateLoss();
        }
    }



    @Override
    public void onBond() {
        Log.i(TAG + " onBond");
        int id = 0;
        if (LibraState.getInstance().getAlarmType() == Const.ALARM_TYPE_ALARM ||
                LibraState.getInstance().getAlarmType() == Const.ALARM_TYPE_FIND) {
            id = INPUT_PASSWORD_ID;
            arg = Const.INPUT_ENTER_UNLOCK;
        } else if (MyPrefs.getInstance().getString(Const.KEY_PSD) == null) {
            id = INPUT_PASSWORD_ID;
            arg = Const.INPUT_SETTING_PASSWORD;
        } else if (mac == null) {
            id = DEVICE_PAIRING_ID;
        } else {
            if (LibraState.getInstance().getLibraState() == Const.STATUS_DEVICE_CONNECTED) {
                id = LIBRA_PROTECTED_ID;
            } else {
                id = LIBRA_CONNECTED_STATE_ID;
            }
        }
        showFragment(id, arg);
    }

    @Override
    public void onFragmentInteraction(int fragmentId, Object arg) {
        if (fragmentId > 0) {
            this.arg = arg;
            showFragment(fragmentId, arg);
        }
    }

    public void upNotify() {
        Intent it = new Intent(this, AntitheftAlarmService.class);
        it.setAction(AntitheftAlarmService.BLE_NOTIFY_ACTION);
        startService(it);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG + " onStop.");
        FragmentStack.getInstance().clear();
        unregisterReceiver(bleConnectStateReceiver);
        fragmentArrayList.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG + " onDestroy.");
    }

    @Override
    public void onBackPressed() {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (FragmentStack.getInstance().size() > 0 &&
                fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onBackPressed");
            fragmentArrayList.get(fragmentId).onBackPressed();
            return;
        }
        super.onBackPressed();
    }

    private BroadcastReceiver bleConnectStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                Log.i(TAG + "MainActivity bleConnectStateReceiver action " + action);
                if (action.equals(AntitheftAlarmService.BLE_STATE_ACTION)) {
                    boolean openOrClosed = intent.getBooleanExtra(Const.BT_STATE, false);
                } else if (action.equals(AntitheftAlarmService.BLE_CONNECT_STATUS_ACTION)) {
                    String mac = intent.getStringExtra(Const.CONNECTED_MAC);
                    int status = intent.getIntExtra(Const.CONNECT_STATUS, Const.STATUS_UNKNOWN);
                    int fragmentId = FragmentStack.getInstance().getTail();
                    if (fragmentArrayList.get(fragmentId) != null) {
                        Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onConnectStatusChanged");
                        fragmentArrayList.get(fragmentId).onConnectStatusChanged(mac, status);
                    }
                }
            }
        }
    };

    @Override
    public void onUsePassword() {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onUsePassword");
            fragmentArrayList.get(fragmentId).onUsePassword();
        }
    }

    @Override
    public void onSucceeded() {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onSucceeded");
            fragmentArrayList.get(fragmentId).onSucceeded();
        }
    }

    @Override
    public void onFailed() {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onFailed");
            fragmentArrayList.get(fragmentId).onFailed();
        }
    }

    @Override
    public void onError(int code, String reason) {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onError");
            fragmentArrayList.get(fragmentId).onError(code, reason);
        }
    }

    @Override
    public void onSearchStarted() {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onSearchStarted");
            fragmentArrayList.get(fragmentId).onSearchStarted();
        }
    }

    @Override
    public void onDevicesFounded(List<SearchResult> devices) {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onDevicesFounded");
            fragmentArrayList.get(fragmentId).onDevicesFounded(devices);
        }
    }

    @Override
    public void onSearchStopped() {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onSearchStopped");
            fragmentArrayList.get(fragmentId).onSearchStopped();
        }
    }

    @Override
    public void onSearchCanceled() {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onSearchCanceled");
            fragmentArrayList.get(fragmentId).onSearchCanceled();
        }
    }

    @Override
    public void onConnectedResponse(int code, BleGattProfile profile) {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onConnectedResponse");
            fragmentArrayList.get(fragmentId).onConnectedResponse(code, profile);
        }
    }

    @Override
    public void onReadResponse(int code, byte[] data) {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onReadResponse");
            fragmentArrayList.get(fragmentId).onReadResponse(code, data);
        }
    }

    @Override
    public void onWriteResponse(int code, int event) {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onWriteResponse");
            fragmentArrayList.get(fragmentId).onWriteResponse(code, event);
        }
    }

    @Override
    public void onNotify(DetailItem item, byte[] data) {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onNotify");
            fragmentArrayList.get(fragmentId).onNotify(item, data);
        }
    }

    @Override
    public void onNotifyResponse(int code) {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onNotifyResponse");
            fragmentArrayList.get(fragmentId).onNotifyResponse(code);
        }
    }

    @Override
    public void onUnNotifyResponse(int code) {
        int fragmentId = FragmentStack.getInstance().getTail();
        if (fragmentArrayList.get(fragmentId) != null) {
            Log.i(TAG + fragmentArrayList.get(fragmentId).getClass().getSimpleName() + " onUnNotifyResponse");
            fragmentArrayList.get(fragmentId).onUnNotifyResponse(code);
        }
    }

    /*private void autoObtainPermission(List<String> permissionList) {
        SimplePermissions.with(this)
            .constantRequest()
            .permission(permissionList)
            .request(new OnPermission() {
                @Override
                public void hasPermission(List<String> granted, boolean isAll) {

                }

                @Override
                public void noPermission(List<String> denied, boolean quick) {
                    if(PermissionUtils.getManifestPermissions(MainActivity.this).containsAll(denied)) {
                        Toast.makeText(MainActivity.this, getString(R.string.grant_permission_manual), Toast.LENGTH_SHORT).show();
                        SimplePermissions.gotoPermissionSettings(MainActivity.this);
                    }
                    deniedList = denied;
                }
            });
    }*/
}
