package com.antitheft.alarm.fragments;

import android.widget.Button;
import androidx.fragment.app.Fragment;
import com.antitheft.alarm.R;
import com.antitheft.alarm.model.LibraState;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.Log;
import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class LibraProtectedFragment extends BaseFragment {

    private static final String TAG = "LibraProtectedFragment";

    @BindView(R.id.bnt_unlock)
    Button bnt_unlock;

    private static LibraProtectedFragment instance;

    public LibraProtectedFragment() {
        // Required empty public constructor
    }

    public static LibraProtectedFragment newInstance() {
        return new LibraProtectedFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_libra_protected;
    }

    @Override
    public void initData() {
        LibraState.getInstance().setAlarmType(Const.ALARM_TYPE_PROTECTION);
        upNotify();
    }

    @Override
    public void initViewEvent() {

    }

    @Override
    public int getFragmentId() {
        return Const.LIBRA_PROTECTED_ID;
    }

    @Override
    public void onConnectStatusChanged(String mac, int status) {
        Log.i(TAG + " onConnectStatusChanged status: " + status);
        if (status == Const.STATUS_DEVICE_DISCONNECTED) {
            goTo(Const.LIBRA_CONNECTED_STATE_ID);
        }
    }

    @Override
    public void onBackPressed() {
        parentActivity.finish();
    }

}
