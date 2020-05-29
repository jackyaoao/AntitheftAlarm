package com.antitheft.alarm.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import com.antitheft.alarm.AppContext;
import com.antitheft.alarm.IAntitheftAlarmService;
import com.antitheft.alarm.model.DetailItem;
import com.antitheft.alarm.model.LibraState;
import com.antitheft.alarm.service.AntitheftAlarmService;
import com.inuker.bluetooth.library.model.BleGattCharacter;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.utils.ByteUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

public class SystemUtils {

	/**
	 * 获取屏幕的宽和高
	 * @param context
	 * 					参数为上下文对象Context
	 * @return
	 * 			返回值为长度为2int型数组,其中
	 * 			int[0] -- 表示屏幕的宽度
	 * 			int[1] -- 表示屏幕的高度
	 */
	public static int[] getSystemDisplay(Context context){
		//创建保存屏幕信息类
		DisplayMetrics dm = new DisplayMetrics();
		//获取窗口管理类
		WindowManager wm =  (WindowManager) context.getSystemService(
				Context.WINDOW_SERVICE);
		//获取屏幕信息并保存到DisplayMetrics中
		wm.getDefaultDisplay().getMetrics(dm);
		//声明数组保存信息
		int[] displays = new int[2];
		displays[0] = dm.widthPixels;//屏幕宽度(单位:px)
		displays[1] = dm.heightPixels;//屏幕高度
		return displays;
	}

	/**
	 * 获取当前app version code
	 */
	public static long getAppVersionCode(Context context) {
		long appVersionCode = 0;
		try {
			PackageInfo packageInfo = context.getApplicationContext()
					.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				appVersionCode = packageInfo.getLongVersionCode();
			} else {
				appVersionCode = packageInfo.versionCode;
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.i("[getAppVersionCode]-error：" + e.getMessage());
		}
		return appVersionCode;
	}

	public static void showToast(String message) {
		Toast.makeText(AppContext.getContext(), message, Toast.LENGTH_SHORT).show();
	}

	public static String md5(String content) {
		byte[] hash;
		try {
			hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("NoSuchAlgorithmException",e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UnsupportedEncodingException", e);
		}
		//对生成的16字节数组进行补零操作
		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10){
				hex.append("0");
			}
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
	}

	public static String parcel2String(Parcelable stu) {
		// 1.序列化
		Parcel p = Parcel.obtain();
		stu.writeToParcel(p, 0);
		byte[] bytes = p.marshall();
		p.recycle();
		// 2.编码
		String str = Base64.encodeToString(bytes, Base64.DEFAULT);
		return str;
	}

	private static Parcel unmarshall(byte[] bytes) {
		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(bytes, 0, bytes.length);
		parcel.setDataPosition(0); // this is extremely important!
		return parcel;
	}

	public static <T> T unmarshall(String str, Parcelable.Creator<T> creator) {
		// 1.解码
		byte[] bytes = Base64.decode(str, Base64.DEFAULT);
		// 2.反序列化
		Parcel parcel = unmarshall(bytes);
		return creator.createFromParcel(parcel);
	}

	public static IAntitheftAlarmService sService = null;

	private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<>();

	public static class ServiceToken {
		ContextWrapper mWrappedContext;
		ServiceToken(ContextWrapper context) {
			mWrappedContext = context;
		}
	}

	public static ServiceToken bindToService(Activity context) {
		return bindToService(context, null);
	}

	public static ServiceToken bindToService(Activity context, ServiceConnection callback) {
		Activity realActivity = context.getParent();
		if (realActivity == null) {
			realActivity = context;
		}
		ContextWrapper cw = new ContextWrapper(realActivity);
		cw.startService(new Intent(cw, AntitheftAlarmService.class));
		ServiceBinder sb = new ServiceBinder(callback);
		if (cw.bindService((new Intent()).setClass(cw, AntitheftAlarmService.class), sb, 0)) {
			sConnectionMap.put(cw, sb);
			return new ServiceToken(cw);
		}
		Log.e("Failed to bind to service");
		return null;
	}

	public static void unbindFromService(ServiceToken token) {
		if (token == null) {
			Log.e("Trying to unbind with null token");
			return;
		}
		ContextWrapper cw = token.mWrappedContext;
		ServiceBinder sb = sConnectionMap.remove(cw);
		if (sb == null) {
			Log.e("Trying to unbind for unknown Context");
			return;
		}
		cw.unbindService(sb);
		if (sConnectionMap.isEmpty()) {
			// presumably there is nobody interested in the service at this point,
			// so don't hang on to the ServiceConnection
			sService = null;
		}
	}

	private static class ServiceBinder implements ServiceConnection {
		ServiceConnection mCallback;
		ServiceBinder(ServiceConnection callback) {
			mCallback = callback;
		}

		public void onServiceConnected(ComponentName className, android.os.IBinder service) {
			sService = IAntitheftAlarmService.Stub.asInterface(service);
			if (mCallback != null) {
				mCallback.onServiceConnected(className, service);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			if (mCallback != null) {
				mCallback.onServiceDisconnected(className);
			}
			sService = null;
		}
	}

	public static void setGattProfile(BleGattProfile profile) {
		DetailItem detailItem = new DetailItem();
		List<BleGattService> services = profile.getServices();
		for (BleGattService service : services) {
			if (service.getUUID().toString().toUpperCase().contains("FFE0")) {
				detailItem.setService(service.getUUID());
				List<BleGattCharacter> characters = service.getCharacters();
				for (BleGattCharacter character : characters) {
					if (character.toString().toUpperCase().contains("FFE1")) {
						detailItem.setWriteCharacter(character.getUuid());
					} else if (character.toString().toUpperCase().contains("FFE2")) {
						detailItem.setReadCharacter(character.getUuid());
					}
				}
			}
		}
		LibraState.getInstance().setDetailItem(detailItem);
		Log.i("detailItem: " + detailItem.toString());
	}

	public static byte[] getSendContent(@Const.BLE_EVENT int event) {
		byte[] content = new byte[20];
		byte[] data = null;
		String mac = LibraState.getInstance().getMac();
		if (mac != null && !mac.isEmpty()) {
			mac = mac.replace(":", "");
		}
		String psd = parseAscii(MyPrefs.getInstance().getString(Const.KEY_PSD) + "01");

		switch (event) {
			case Const.BLE_CONNECT_W_EVENT:
				content[0] = 0x01;
				content[1] = 0x01;
				data = ByteUtils.stringToBytes(LibraState.getInstance().getAppId() + psd);
				break;
			case Const.BLE_REMOVE_ALARM_W_EVENT:
				content[0] = 0x31;
				content[1] = 0x01;
				data = ByteUtils.stringToBytes(
						psd.substring(0, 2) + mac.substring(10, 12) +
						psd.substring(2, 4) + mac.substring(8, 10) +
						psd.substring(4, 6) + mac.substring(6, 8) +
						psd.substring(6, 8) + mac.substring(4, 6) +
						psd.substring(8, 10) + mac.substring(2, 4) +
						psd.substring(10, 12) + mac.substring(0, 2));

				break;
			case Const.BLE_FOUND_ACK_W_EVENT:
				content[0] = 0x32;
				content[1] = 0x01;
				data = ByteUtils.stringToBytes(LibraState.getInstance().getAppId() + parseAscii("OK"));
				break;
			case Const.USB_DISCONNECTED_W_EVENT:
				content[0] = 0x41;
				content[1] = 0x01;
				data = ByteUtils.stringToBytes(
						mac.substring(10, 12) +
						mac.substring(8, 10) +
						mac.substring(6, 8) +
						mac.substring(4, 6) +
						mac.substring(2, 4) +
						mac.substring(0, 2));
				break;
		}
		System.arraycopy(data, 0, content, 2, data.length);
		return content;
	}

	private static String toHexUtil(int n) {
		String rt = "";
		switch (n) {
			case 10:
				rt += "A";
				break;
			case 11:
				rt += "B";
				break;
			case 12:
				rt += "C";
				break;
			case 13:
				rt += "D";
				break;
			case 14:
				rt += "E";
				break;
			case 15:
				rt += "F";
				break;
			default:
				rt += n;
		}
		return rt;
	}

	public static String toHex(int n) {
		StringBuilder sb = new StringBuilder();
		if (n / 16 == 0) {
			return toHexUtil(n);
		} else {
			String t = toHex(n / 16);
			int nn = n % 16;
			sb.append(t).append(toHexUtil(nn));
		}
		return sb.toString();
	}

	public static String parseAscii(String str) {
		StringBuilder sb = new StringBuilder();
		byte[] bs = str.getBytes();
		for (int i = 0; i < bs.length; i++)
			sb.append(toHex(bs[i]));
		return sb.toString();
	}

	public static boolean isAlarmHandlerActivity(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
		if (runningTaskInfos != null)
			return runningTaskInfos.get(0).topActivity.getClassName().contains("AlarmHandlerActivity");
		else
			return false;
	}

	public static final boolean IS_CHARGE_DISABLE = true;

	public static boolean isChargingDisable(Context context) {
		return IS_CHARGE_DISABLE && isCharging(context);
	}

	private static boolean isCharging(Context context) {
		Intent batteryBroadcast = context.registerReceiver(null,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		// 0 means we are discharging, anything else means charging
		boolean isCharging = batteryBroadcast.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0;
		return isCharging;
	}
}
