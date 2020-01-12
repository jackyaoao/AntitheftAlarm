package com.antitheft.alarm.utils;

import androidx.annotation.IntDef;

import com.antitheft.alarm.AppContext;
import com.inuker.bluetooth.library.Constants;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

final public class Const {
    public static final String TAG = "Anti-theft"; // miio-bluetooth
    /** 初始设置密码 */
    public static final int INPUT_SETTING_PASSWORD = 0;
    /** unlock*/
    public static final int INPUT_ENTER_UNLOCK = 1;
    /** reset password*/
    public static final int INPUT_RESET_PASSWORD = 2;

    /** SharedPreferences的文件名 */
    public static final String PREF_NAME = AppContext.getContext().getPackageName();
    /** 存储密码*/
    public static final String KEY_PSD = "psd";
    /** 授权使用指纹*/
    public static final String KEY_USE_FP = "fp";
    /** 链接设备的mac地址*/
    public static final String CONNECTED_MAC = "mac";
    /** connected profile*/
    public static final String CONNECTED_ITEM = "item";
    /** write ble content*/
    public static final String BLE_CONTENT = "content";
    /** the id of the latest entered the fragment*/
    public static final String FRAGMENT_ID = "id";
    /** ble device connect state*/
    public static final String CONNECT_STATUS = "status";
    /** bluetooth state*/
    public static final String BT_STATE = "state";
    /**app id*/
    public static final String APPID = "app_id";
    public static final String BIOMETRIC = "biometric";
    public static final String UNLOCK = "unlock";
    /** save key end*/

    /** anti-theft service name*/
    public static final String SERVICE_NAME = "anti-theft";

    /**Alaram type*/
    public static final int ALARM_TYPE_UNKNOWN = 0x400;
    public static final int ALARM_TYPE_PROTECTION = 0x401;
    public static final int ALARM_TYPE_ALARM = 0x402;
    public static final int ALARM_TYPE_FIND = 0x403;

    /** Fragment IDS*/
    public static final int CREATE_ACCOUNT_ID = 0x100; //256
    public static final int INPUT_PASSWORD_ID = 0x101;
    public static final int DEVICE_PAIRING_ID = 0x102;
    public static final int DEVICE_FOUND_ID = 0x103;
    public static final int AUTHORIZATION_ID = 0x104;
    public static final int LIBRA_CONNECTED_STATE_ID = 0x105;
    public static final int LIBRA_PROTECTED_ID = 0x106;
    public static final int SETTING_FRAGMENT_ID = 0x107;

    /** BLE COMMNUICATION EVENT*/

    public static final int BLE_REQUEST_SUCCESS = Constants.REQUEST_SUCCESS;
    public static final String BLE_EVENT = "event";
    public static final int BLE_CONNECT_W_EVENT = 0x200;              //WRITE //512
    public static final int BLE_CONNECT_ACK_R_EVENT = 0x201;          //READ
    public static final int BLE_REMOVE_ALARM_W_EVENT = 0x202;         //WRITE
    public static final int BLE_ALARM_R_EVENT = 0x203;                //READ
    public static final int BLE_FIND_PHONE_R_EVENT = 0x204;           //READ
    public static final int BLE_FOUND_ACK_W_EVENT = 0x205;            //WRITE
    public static final int BLE_CLEAR_W_EVENT = 0x206;                //WRITE
    public static final int USB_DISCONNECTED_W_EVENT = 0x207;         //WRITE
    @IntDef({BLE_CONNECT_W_EVENT, BLE_CONNECT_ACK_R_EVENT, BLE_REMOVE_ALARM_W_EVENT,
            BLE_ALARM_R_EVENT, BLE_FIND_PHONE_R_EVENT, BLE_FOUND_ACK_W_EVENT, BLE_CLEAR_W_EVENT, USB_DISCONNECTED_W_EVENT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BLE_EVENT {}

    /** the status of Libra's connection*/
    public static final int STATUS_UNKNOWN = -1;
    public static final int STATUS_DEVICE_CONNECTED = Constants.STATUS_CONNECTED;
    public static final int STATUS_DEVICE_DISCONNECTED = Constants.STATUS_DISCONNECTED;

}
