package com.antitheft.alarm.fragments;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.antitheft.alarm.R;
import com.antitheft.alarm.model.LibraState;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.Log;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.utils.ByteUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class LibraConnectStateFragment extends BaseFragment {

    @BindView(R.id.bnt_setting)
    ImageView bnt_setting;
    @BindView(R.id.libra_state)
    ImageView libra_state;
    @BindView(R.id.state_str)
    TextView state_str;
    @BindView(R.id.state_desc1)
    TextView state_desc1;
    @BindView(R.id.state_desc2)
    TextView state_desc2;
    @BindView(R.id.state_desc3)
    TextView state_desc3;
    @BindView(R.id.state_desc4)
    TextView state_desc4;
    @BindView(R.id.progress)
    ImageView progress;
    @BindView(R.id.progress_view)
    LinearLayout progress_view;

    private static final String TAG = LibraConnectStateFragment.class.getSimpleName();

    private String mac;
    private Handler handler = new Handler(Looper.getMainLooper());

    public LibraConnectStateFragment() {
        // Required empty public constructor
    }

    public static LibraConnectStateFragment newInstance() {
        return new LibraConnectStateFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_libra_state;
    }

    @Override
    public void initData() {
        LibraState.getInstance().setAlarmType(Const.ALARM_TYPE_PROTECTION);
        mac = LibraState.getInstance().getMac();
        if (mac != null) {
            if (LibraState.getInstance().getLibraState() == Const.STATUS_DEVICE_DISCONNECTED) {
                rotate();
                bleConnect(mac);
            }
            bleRegisterConnectListenter(mac);
        }
        updateView();
    }

    private void updateView() {
        /** Libra connected */
        if (LibraState.getInstance().getLibraState() == Const.STATUS_DEVICE_CONNECTED) {
            progress_view.setVisibility(View.GONE);
            state_str.setText("MY007 Connected");
            state_desc1.setVisibility(View.GONE);
            state_desc2.setText("Charge your device through MY007 to protect.");
            state_desc3.setText("The alarm will go on if charger is unplugged.");
            state_desc4.setVisibility(View.GONE);
            libra_state.setImageResource(R.drawable.libra_connected);
            handler.postDelayed(standby, 2 * 1000);
        } else {/** Libra lost*/
            handler.removeCallbacks(standby);
            rotate();
            state_str.setText("MY007 Lost");
            state_desc1.setVisibility(View.VISIBLE);
            state_desc1.setText("Oops~please make sure:");
            state_desc2.setText("1. Bluetooth ON");
            state_desc3.setText("2. MY007 ON");
            state_desc4.setVisibility(View.VISIBLE);
            state_desc4.setText("3. MY007 close to your phone");
            libra_state.setImageResource(R.drawable.libra_lost);
        }
    }

    private Runnable standby = () -> {
        if (LibraState.getInstance().getLibraState() == Const.STATUS_DEVICE_CONNECTED) {
            goTo(Const.LIBRA_PROTECTED_ID);
        }
    };

    private void rotate() {
        progress_view.setVisibility(View.VISIBLE);
        RotateAnimation anim = new RotateAnimation(0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(2000);
        progress.startAnimation(anim);
    }

    @Override
    public void initViewEvent() {

    }

    @Override
    public int getFragmentId() {
        return Const.LIBRA_CONNECTED_STATE_ID;
    }

    @OnClick(R.id.bnt_setting)
    public void onClick(View v) {
        goTo(Const.SETTING_FRAGMENT_ID);
    }

    @Override
    public void onConnectStatusChanged(String mac, int status) {
        Log.i(TAG + " onConnectStatusChanged status: " + status);
        updateView();
    }

    @Override
    public void onReadResponse(int code, byte[] data) {
        super.onReadResponse(code, data);
        if (code == Const.BLE_REQUEST_SUCCESS) {
            String content = ByteUtils.byteToString(data);
            Log.i(" onReadResponse content:  " + content);
            if ((content.substring(0, 4).equals("7501") && content.contains("414C41524D")) ||
                    (content.substring(0, 4).equals("7601") && content.contains("46494E44"))) {
                handler.removeCallbacks(standby);
            }
        }
    }

    @Override
    public void onConnectedResponse(int code, BleGattProfile profile) {
        updateView();
    }

    @Override
    public void onBackPressed() {
        parentActivity.finish();
    }
}
