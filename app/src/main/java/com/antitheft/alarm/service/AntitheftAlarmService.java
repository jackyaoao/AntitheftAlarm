package com.antitheft.alarm.service;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.antitheft.alarm.activity.AlarmHandlerActivity;
import com.antitheft.alarm.AppContext;
import com.antitheft.alarm.IAntitheftAlarmService;
import com.antitheft.alarm.IServiceDataListener;
import com.antitheft.alarm.activity.MainActivity;
import com.antitheft.alarm.R;
import com.antitheft.alarm.activity.SplashActivity;
import com.antitheft.alarm.btkit.BluetoothManager;
import com.antitheft.alarm.fragments.FragmentStack;
import com.antitheft.alarm.mediaplay.MediaPlayer;
import com.antitheft.alarm.model.DetailItem;
import com.antitheft.alarm.model.LibraState;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.Log;
import com.antitheft.alarm.utils.MyPrefs;
import com.antitheft.alarm.utils.SystemUtils;
import com.antitheft.biometriclib.BiometricPromptManager;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static com.antitheft.alarm.utils.Const.USB_DISCONNECTED_W_EVENT;

public class AntitheftAlarmService extends Service {

    private static final String TAG = AntitheftAlarmService.class.getSimpleName();

    public static final int ANTITHEFTALARMSERVICE_STATUS = 1;
    public static final String CHANNEL_ID = "antitheft";
    public static final int INT_CHANNEL_ID = 0x200;
    public static final String BLE_WRITE_ACTION = "ble.write.action";
    public static final String BLE_READ_ACTION = "ble.read.action";
    public static final String BLE_NOTIFY_ACTION = "ble.notify.action";
    public static final String BLE_ALARM_START_ATION = "ble.alarm.start.action";
    public static final String BLE_ALARM_STOP_ATION = "ble.alarm.stop.action";
    public static final String AUTHENTICATE_ACTION = "authenticate.action";
    public static final String BLE_SEARCH_START_ACTION = "ble.search.start.action";
    public static final String BLE_SEARCH_STOP_ACTION = "ble.search.stop.ation";
    public static final String BLE_CONNECT_ACTION = "ble.connect.action";
    public static final String BLE_DISCONNECT_ACTION = "ble.disconnect.action";
    public static final String BLE_REGISTER_BT_STATE_ACTION = "ble.register.bt.state.action";
    public static final String BLE_UNREGISTER_BT_STATE_ACTION = "ble.unregister.bt.state.action";
    public static final String BLE_EVENT_NOTIFY_ACTION = "ble.event.notify.action";
    public static final String BLE_EVENT_UNNOTIFY_ACTION = "ble.event.unnotify.action";
    public static final String BLE_EVENT_RESTART_READ_ACTION = "ble.event.restart.read.action";


    public static final String BLE_STATE_ACTION = "ble.state.action";
    public static final String BLE_CONNECT_STATUS_ACTION = "ble.connect.status.action";
    public static final String BLE_POWER_CONNECTED_ACTION = "ble.power.connected.action";
    private boolean isRegister = false;
    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i("AntitheftAlarmService onBind");
        try {
            mBinder.initMediaPlayer();
            mBinder.biometricenable();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (LibraState.getInstance().getLibraState() != Const.STATUS_DEVICE_CONNECTED  &&
                LibraState.getInstance().getMac() != null) {
            try {
                mBinder.connect(LibraState.getInstance().getMac());
                register(LibraState.getInstance().getMac());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("AntitheftAlarmService onCreate");
        if (!isRegister) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            registerReceiver(receiver, filter);
            isRegister = true;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Daemon.getInstance().exit();
        try {
            mBinder.releaseMediaPlayer();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unregister(LibraState.getInstance().getMac());
        unregisterReceiver(receiver);
        isRegister = false;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG + " onReceive action: " + action);
            if (action.equals("ble.lock.screen.notify.action")) {
                KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                //启动Activity
                Intent alarmIntent = new Intent(context, AlarmHandlerActivity.class);
                //activity需要新的任务栈
                alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(alarmIntent);
            } else if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                //Toast.makeText(context, "Power Connected", Toast.LENGTH_SHORT).show();
                MyPrefs.getInstance().put(Const.POWER_CONNECTED, true);
                Intent it = new Intent();
                it.setAction(BLE_POWER_CONNECTED_ACTION);
                sendBroadcast(it);
            } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                MyPrefs.getInstance().put(Const.POWER_CONNECTED, false);
            }
        }
    };

    private BluetoothStateListener stateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            Log.i(TAG + "onBluetoothStateChanged openOrClosed: " + openOrClosed);
            if (!openOrClosed) {
                BluetoothManager.getClient().openBluetooth();
            } else {
                try {
                    mBinder.connect(LibraState.getInstance().getMac());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private BleConnectStatusListener connectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            Log.i(String.format("%s onConnectStatusChanged mac = %s, status = %d", TAG , mac, status));
            Intent it = new Intent();
            it.setAction(BLE_CONNECT_STATUS_ACTION);
            it.putExtra(Const.CONNECTED_MAC, mac);
            it.putExtra(Const.CONNECT_STATUS, status);
            sendBroadcast(it);
            if (status == Const.STATUS_DEVICE_DISCONNECTED) {
                LibraState.getInstance().setAlarmType(Const.ALARM_TYPE_UNKNOWN);
                try {
                    mBinder.connect(LibraState.getInstance().getMac());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                LibraState.getInstance().setAlarmType(Const.ALARM_TYPE_PROTECTION);
            }
            updateNotification();
        }
    };

    private void register(String mac) {
        BluetoothManager.getClient().registerBluetoothStateListener(stateListener);
        BluetoothManager.getClient().registerConnectStatusListener(mac, connectStatusListener);
    }

    private void unregister(String mac) {
        BluetoothManager.getClient().unregisterBluetoothStateListener(stateListener);
        BluetoothManager.getClient().unregisterConnectStatusListener(mac, connectStatusListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String mac = intent.getStringExtra(Const.CONNECTED_MAC);
            try {
                if (action != null) {
                    Log.i("AntitheftAlarmService action " + action);
                    if (action.equals(BLE_WRITE_ACTION)) {
                        DetailItem item = intent.getParcelableExtra(Const.CONNECTED_ITEM);
                        byte[] content = intent.getByteArrayExtra(Const.BLE_CONTENT);
                        int event = intent.getIntExtra(Const.BLE_EVENT, 0);
                        mBinder.write(mac, item, content, event);
                    } else if (action.equals(BLE_READ_ACTION)) {
                        DetailItem item = intent.getParcelableExtra(Const.CONNECTED_ITEM);
                        mBinder.read(mac, item);
                    } else if (action.equals(BLE_NOTIFY_ACTION)) {
                        updateNotification();
                    } else if (action.equals(BLE_ALARM_START_ATION)) {
                        mBinder.play("alarm", true);
                        updateNotification();
                    } else if (action.equals(BLE_ALARM_STOP_ATION)) {
                        mBinder.stop();
                        updateNotification();
                    } else if (action.equals(AUTHENTICATE_ACTION)) {
                        mBinder.authenticate();
                    } else if (action.equals(BLE_SEARCH_START_ACTION)) {
                        mBinder.searchBle();
                    } else if (action.equals(BLE_SEARCH_STOP_ACTION)) {
                        mBinder.stopBle();
                    } else if (action.equals(BLE_CONNECT_ACTION)) {
                        mBinder.connect(mac);
                    } else if (action.equals(BLE_DISCONNECT_ACTION)) {
                        mBinder.disconnect(mac);
                    } else if (action.equals(BLE_REGISTER_BT_STATE_ACTION)) {
                        register(mac);
                    } else if (action.equals(BLE_UNREGISTER_BT_STATE_ACTION)) {
                        unregister(mac);
                    } else if (action.equals(BLE_EVENT_NOTIFY_ACTION)) {
                        DetailItem item = intent.getParcelableExtra(Const.CONNECTED_ITEM);
                        mBinder.bleNotify(mac, item);
                    } else if (action.equals(BLE_EVENT_UNNOTIFY_ACTION)) {
                        DetailItem item = intent.getParcelableExtra(Const.CONNECTED_ITEM);
                        mBinder.bleUnnotify(mac, item);
                    } else if (action.equals("android.intent.action.SCREEN_ON")) {
                        mBinder.stop();
                        Intent it = new Intent(this, MainActivity.class);
                        it.putExtra(Const.UNLOCK, true);
                        startActivity(it);
                    } else if (action.equals(BLE_EVENT_RESTART_READ_ACTION)) {
                        startDaemon();
                    }
                }
            }catch (RemoteException e) {
                Log.i(TAG + " onStartCommand action " + action + " Caused by: " + e.getMessage());
            }
        }
        return START_STICKY;
    }

    public static void startDaemon() {
        Daemon.getInstance().reStart();
        if (!Daemon.getInstance().isRunning()) {
            Daemon.getInstance().start();
        }
    }

    static private class Daemon extends Thread {
        private boolean isExit = false;
        private boolean isPause = false;
        private boolean isRunning = false;
        private static Daemon instance;
        private int delay = 0;

        public static Daemon getInstance() {
            if (instance == null) {
                synchronized (Daemon.class) {
                    if (instance == null) {
                        instance = new Daemon();
                    }
                }
            }
            return instance;
        }

        @Override
        public void run() {
            isRunning = true;
            while (!isExit) {
                if (isPause) {
                    //Log.i(TAG + " Daemon pause");
                    continue;
                }
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    delay = 0;
                    e.printStackTrace();
                }
                //Log.i(TAG + " Daemon read start");
                try {
                    delay = 0;
                    if (LibraState.getInstance().getLibraState() ==
                            Const.STATUS_DEVICE_CONNECTED) {
                        Intent it = new Intent(AppContext.getContext(), AntitheftAlarmService.class);
                        it.setAction(AntitheftAlarmService.BLE_READ_ACTION);
                        it.putExtra(Const.CONNECTED_ITEM, LibraState.getInstance().getDetailItem());
                        it.putExtra(Const.CONNECTED_MAC, LibraState.getInstance().getMac());
                        AppContext.getContext().startService(it);

                    }
                } catch (Exception e) {
                    Log.e(TAG + " Daemon read Caused by " + e.getMessage());
                    isPause = false;
                }
                Log.i(TAG + " Daemon read end");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG + " Daemon exit");
            isRunning = false;
        }

        public void exit() {
            isExit = true;
            isRunning = false;
        }

        public void reStart() {
            isPause = false;
        }

        public void pause() {
            isPause = true;
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }
    }

    public void updateNotification() {
        String text = "";
        int alarmType = LibraState.getInstance().getAlarmType();
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.notify_layout);
        views.setImageViewResource(R.id.icon, R.mipmap.ic_logo);
        if (alarmType == Const.ALARM_TYPE_PROTECTION) {
            text = "Protection";
        } else if (alarmType == Const.ALARM_TYPE_ALARM) {
            text = "Alarm";
        } else if (alarmType == Const.ALARM_TYPE_FIND) {
            text = "Looking for phone.";
        } else if (alarmType == Const.ALARM_TYPE_UNKNOWN) {
            text = "Disconnected";
        }
        views.setTextViewText(R.id.alarmText, text);

        String CHANNEL_ONE_NAME = "Channel One";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification.Builder builder = new Notification.Builder(this);;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(CHANNEL_ID);
        }
        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.mipmap.ic_logo)
                .setTicker(getText(R.string.app_name))
                .setContentText(text)
                .setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(ANTITHEFTALARMSERVICE_STATUS, notification);
    }

    private IAntitheftAlarmService.Stub mBinder = new IAntitheftAlarmService.Stub() {

        private MediaPlayer mediaPlayer;
        private boolean isAlarm = false;
        private boolean isShowDialog = false;

        protected RemoteCallbackList<IServiceDataListener> mRemoteCallbackList =
                new RemoteCallbackList<>();

        private BiometricPromptManager biometricPromptManager = BiometricPromptManager.from(AppContext.getContext());

        private AntitheftAlarmService getService() {
            return AntitheftAlarmService.this;
        }

        private void showAlarmDialog() {
            if (isShowDialog) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(AppContext.getContext())
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("service中弹出Dialog了")
                    .setMessage("是否关闭dialog？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    isShowDialog = false;
                                }
                            })
                    .setNegativeButton("取消",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    isShowDialog = false;
                                }
                            });
            //下面这行代码放到子线程中会 Can't create handler inside thread that has not called Looper.prepare()
            AlertDialog dialog = builder.create();
            //设置点击其他地方不可取消此 Dialog
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            //8.0系统加强后台管理，禁止在其他应用和窗口弹提醒弹窗，如果要弹，必须使用TYPE_APPLICATION_OVERLAY，否则弹不出
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
            } else {
                dialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
            }
            dialog.show();
            isShowDialog = true;
        }

        @Override
        public void biometricenable() throws RemoteException {
            MyPrefs.getInstance().put(Const.BIOMETRIC, biometricPromptManager.isBiometricPromptEnable());
        }

        @Override
        public void authenticate() throws RemoteException {
            biometricPromptManager.authenticate(new BiometricPromptManager.OnBiometricIdentifyCallback() {
                @Override
                public void onUsePassword() {
                    Log.i(TAG + " onUsePassword");
                    try {
                        final int N = mRemoteCallbackList.beginBroadcast();
                        for (int i = 0; i < N; i ++) {
                            mRemoteCallbackList.getBroadcastItem(i).onUsePassword();
                        }
                    }
                    catch (RemoteException e) {
                        Log.e(TAG + " Caused by " + e.getMessage());
                    } finally {
                        mRemoteCallbackList.finishBroadcast();
                    }
                }

                @Override
                public void onSucceeded() {
                    Log.i(TAG + " onSucceeded");
                    try {
                        final int N = mRemoteCallbackList.beginBroadcast();
                        for (int i = 0; i < N; i++) {
                            mRemoteCallbackList.getBroadcastItem(i).onSucceeded();
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG + " Caused by " + e.getMessage());
                    } finally {
                        mRemoteCallbackList.finishBroadcast();
                    }
                }

                @Override
                public void onFailed() {
                    Log.i(TAG + " onFailed");
                    try {
                        final int N = mRemoteCallbackList.beginBroadcast();
                        for (int i = 0; i < N; i++) {
                            mRemoteCallbackList.getBroadcastItem(i).onFailed();
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG + " Caused by " + e.getMessage());
                    } finally {
                        mRemoteCallbackList.finishBroadcast();
                    }
                }

                @Override
                public void onError(int code, String reason) {
                    Log.i(String.format("%s onError code = %d reason = %s ", TAG, code, reason));
                    try {
                        final int N = mRemoteCallbackList.beginBroadcast();
                        for (int i = 0; i < N; i++) {
                            mRemoteCallbackList.getBroadcastItem(i).onError(code, reason);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG + " Caused by " + e.getMessage());
                    } finally {
                        mRemoteCallbackList.finishBroadcast();
                    }
                }

                @Override
                public void onCancel() {

                }
            });
        }

        @Override
        public void initMediaPlayer() {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer.Builder()
                        .addSound("alarm", R.raw.alarm)
                        .setStreamType(MediaPlayer.TYPE_MUSIC)
                        .setMaxStream(10)
                        .builder();
            }
        }

        @Override
        public void releaseMediaPlayer() {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = null;
        }

        @Override
        public void play(String name, boolean isLoop) throws RemoteException {
            if (!isAlarm && mediaPlayer != null) {
                isAlarm = true;
                mediaPlayer.setMaxStream();
                mediaPlayer.play(name, isLoop);
            }
        }

        @Override
        public void stop() throws RemoteException {
            isAlarm = false;
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        }

        @Override
        public void openBluetooth() throws RemoteException {
            BluetoothManager.getClient().openBluetooth();
        }

        @Override
        public void closeBluetooth() throws RemoteException {
            BluetoothManager.getClient().closeBluetooth();
        }

        @Override
        public void searchBle() throws RemoteException {
            List<SearchResult> devices = new ArrayList<>();
            SearchRequest request = new SearchRequest.Builder()
                    .searchBluetoothClassicDevice(3000, 1)
                    .searchBluetoothLeDevice(3000, 1)
                    .build();
            BluetoothManager.getClient().search(request, new SearchResponse() {
                @Override
                public void onSearchStarted() {
                    Log.i(TAG + " onSearchStarted");
                    try {
                        final int N = mRemoteCallbackList.beginBroadcast();
                        for (int i = 0; i < N; i++) {
                            mRemoteCallbackList.getBroadcastItem(i).onSearchStarted();
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG + " Caused by " + e.getMessage());
                    } finally {
                        mRemoteCallbackList.finishBroadcast();
                    }
                }

                @Override
                public void onDeviceFounded(SearchResult device) {
                    if (!devices.contains(device)) {
                        devices.add(device);
                    }
                }

                @Override
                public void onSearchStopped() {
                    Log.i(TAG + " onSearchStopped");
                    try {
                        final int N = mRemoteCallbackList.beginBroadcast();
                        for (int i = 0; i < N; i++) {
                            mRemoteCallbackList.getBroadcastItem(i).onDevicesFounded(devices);
                        }
                    } catch (RemoteException e) {
                        Log.e(TAG + " Caused by " + e.getMessage());
                    } finally {
                        mRemoteCallbackList.finishBroadcast();
                    }
                }

                @Override
                public void onSearchCanceled() {

                }
            });
        }

        @Override
        public void stopBle() throws RemoteException {
            BluetoothManager.getClient().stopSearch();
        }

        @Override
        public void connect(String mac) throws RemoteException {
            BleConnectOptions options = new BleConnectOptions.Builder()
                    .setConnectRetry(3)
                    .setConnectTimeout(20000)
                    .setServiceDiscoverRetry(3)
                    .setServiceDiscoverTimeout(10000)
                    .build();

            BluetoothManager.getClient().connect(mac, options, new BleConnectResponse() {
                @Override
                public void onResponse(int code, BleGattProfile profile) {
                    Log.i(TAG + " onConnectedResponse code " + code);
                    if (code == Const.BLE_REQUEST_SUCCESS) {
                        SystemUtils.setGattProfile(profile);
                        try {
                            write(mac, LibraState.getInstance().getDetailItem(),
                                    SystemUtils.getSendContent(Const.BLE_CONNECT_W_EVENT),
                                    Const.BLE_CONNECT_W_EVENT);
                        } catch (RemoteException e) {
                            Log.e(TAG + " Caused by " + e.getMessage());
                        }
                        if (FragmentStack.getInstance().size() > 0) {
                            try {
                                final int N = mRemoteCallbackList.beginBroadcast();
                                for (int i = 0; i < N; i++) {
                                    mRemoteCallbackList.getBroadcastItem(i).onConnectedResponse(code, profile);
                                }
                            } catch (RemoteException e) {
                                Log.e(TAG + " Caused by " + e.getMessage());
                            } finally {
                                mRemoteCallbackList.finishBroadcast();
                            }
                        }
                        startDaemon();
                    }
                }
            });
        }

        @Override
        public void disconnect(String mac) throws RemoteException {
            BluetoothManager.getClient().disconnect(mac);
        }

        @Override
        public void write(final String mac, final DetailItem item, final byte[] content, final int event) throws RemoteException {
            //Toast.makeText(AppContext.getContext(), "write: " + ByteUtils.byteToString(content), Toast.LENGTH_SHORT).show();
            if (mac != null && item != null) {
                Log.i(String.format("%s ble write mac = %s, service = %s, writeCharacter = %s, content = %s",
                    TAG, mac, item.service.toString(),
                        event == Const.BLE_CLEAR_W_EVENT ?
                            item.readCharacter.toString() :
                            item.writeCharacter.toString(),
                        ByteUtils.byteToString(content)));

                BluetoothManager.getClient().write(mac, item.service,
                    event == Const.BLE_CLEAR_W_EVENT ? item.readCharacter : item.writeCharacter,
                        content, new BleWriteResponse() {
                        @Override
                        public void onResponse(int code) {
                            Log.i(String.format("%s write onResponse code = %d, event = %d", TAG, code, event));
                            if (code == Const.BLE_REQUEST_SUCCESS) {
                                if (FragmentStack.getInstance().size() > 0) {
                                    try {
                                        final int N = mRemoteCallbackList.beginBroadcast();
                                        for (int i = 0; i < N; i++) {
                                            mRemoteCallbackList.getBroadcastItem(i).onWriteResponse(code, event);
                                        }
                                    } catch (RemoteException e) {
                                        Log.e(TAG + " Caused by " + e.getMessage());
                                    } finally {
                                        mRemoteCallbackList.finishBroadcast();
                                    }
                                }
                                startDaemon();
                            }
                        }
                    });
            }
        }

        @Override
        public void read(String mac, DetailItem item) throws RemoteException {
            if (mac != null && item != null) {
                BluetoothManager.getClient().read(mac, item.service, item.readCharacter, new BleReadResponse() {
                    @Override
                    public void onResponse(int code, byte[] data) {
                        String content = ByteUtils.byteToString(data);
                        Log.i(String.format("%s, onReadResponse code = %s, data = %s", TAG, code, content));
                        if (code == Const.BLE_REQUEST_SUCCESS && LibraState.getInstance().getAlarmType() == Const.ALARM_TYPE_PROTECTION) {
                            Daemon.getInstance().pause();
                            try {
                                if((content.substring(0, 4).equals("6101") && content.contains("4F4B")) ||
                                        content.equals("0000000000000000000000000000000000000000")) {
                                    Daemon.getInstance().reStart();
                                    return;
                                } else {
                                    if (content.substring(0, 4).equals("7501") && content.contains("414C41524D")) {
                                        LibraState.getInstance().setAlarmType(Const.ALARM_TYPE_ALARM);
                                        if (!isAlarm) {
                                            handler.removeCallbacks(stopAlarm);
                                            play("alarm", true);
                                            handler.postDelayed(stopAlarm, 1000 * 60 * 3);
                                        }
                                    } else if (content.substring(0, 4).equals("7601") && content.contains("46494E44")) {
                                        LibraState.getInstance().setAlarmType(Const.ALARM_TYPE_FIND);
                                        if (!isAlarm) {
                                            handler.removeCallbacks(stopAlarm);
                                            play("alarm", true);
                                            handler.postDelayed(stopAlarm, 1000 * 60 * 3);
                                        }
                                    }
                                    getService().updateNotification();
                                }
                            } catch (Exception e) {
                                Log.e(TAG + " Caused by " + e.getMessage());
                            } finally {
                                if (FragmentStack.getInstance().size() > 0) {
                                    try {
                                        final int N = mRemoteCallbackList.beginBroadcast();
                                        for (int i = 0; i < N; i++) {
                                            mRemoteCallbackList.getBroadcastItem(i).onReadResponse(code, data);
                                        }
                                    } catch (RemoteException e) {
                                        Log.e(TAG + " Caused by " + e.getMessage());
                                    } finally {
                                        mRemoteCallbackList.finishBroadcast();
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }

        private Runnable stopAlarm = () -> {
            try {
                LibraState.getInstance().setAlarmType(Const.ALARM_TYPE_PROTECTION);
                stop();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        };

        @Override
        public void bleNotify(String mac, DetailItem item) throws RemoteException {
            if(mac != null && item != null) {
                BluetoothManager.getClient().notify(mac, item.service, item.writeCharacter, new BleNotifyResponse() {
                    @Override
                    public void onNotify(UUID service, UUID character, byte[] value) {
                        Log.i(String.format("%s, onNotify service = %s, character = %s, data = %s",
                                TAG, service.toString(), character.toString(),
                                ByteUtils.byteToString(value)));
                        try {
                            final int N = mRemoteCallbackList.beginBroadcast();
                            for (int i = 0; i < N; i++) {
                                mRemoteCallbackList.getBroadcastItem(i).onNotify(new DetailItem(character,
                                        null, service), value);
                            }
                        } catch (RemoteException e) {
                            Log.e(TAG + " Caused by " + e.getMessage());
                        } finally {
                            mRemoteCallbackList.finishBroadcast();
                        }
                    }

                    @Override
                    public void onResponse(int code) {
                        Log.i(String.format("%s, notify onResponse code = %d", TAG, code));
                        try {
                            final int N = mRemoteCallbackList.beginBroadcast();
                            for (int i = 0; i < N; i++) {
                                mRemoteCallbackList.getBroadcastItem(i).onNotifyResponse(code);
                            }
                        } catch (RemoteException e) {
                            Log.e(TAG + " Caused by " + e.getMessage());
                        } finally {
                            mRemoteCallbackList.finishBroadcast();
                        }
                    }
                });
            }
        }

        @Override
        public void bleUnnotify(String mac, DetailItem item) throws RemoteException {
            if (mac != null && item != null) {
                BluetoothManager.getClient().unnotify(mac, item.service, item.writeCharacter, new BleUnnotifyResponse() {
                    @Override
                    public void onResponse(int code) {
                        Log.i(String.format("%s unnotify onResponse code = %d", TAG, code));
                        try {
                            final int N = mRemoteCallbackList.beginBroadcast();
                            for (int i = 0; i < N; i++) {
                                mRemoteCallbackList.getBroadcastItem(i).onUnNotifyResponse(code);
                            }
                        } catch (RemoteException e) {
                            Log.e(TAG + " Caused by " + e.getMessage());
                        } finally {
                            mRemoteCallbackList.finishBroadcast();
                        }
                    }
                });
            }
        }

        @Override
        public void registerServiceDataListener(IServiceDataListener listener) throws RemoteException {
            mRemoteCallbackList.register(listener);
        }

        @Override
        public void unRegisterServiceDataListener(IServiceDataListener listener) throws RemoteException {
            mRemoteCallbackList.unregister(listener);
        }

        private boolean parse(byte[] content) {
            return false;
        }
    };
}
