package com.antitheft.alarm.fragments;

import android.view.View;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.antitheft.alarm.R;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.MyPrefs;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class AuthorizationFragment extends BaseFragment implements View.OnClickListener {

    @BindView(R.id.bnt_allow)
    Button bnt_allow;

    public AuthorizationFragment() {
        // Required empty public constructor
    }

    public static AuthorizationFragment newInstance() {
        return new AuthorizationFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_finger_print_auth;
    }

    @Override
    public void initData() {

    }

    @Override
    public void initViewEvent() {
        bnt_allow.setOnClickListener(this);
    }

    @Override
    public int getFragmentId() {
        return Const.AUTHORIZATION_ID;
    }

    @Override
    public void onClick(View v) {
        goTo(Const.DEVICE_PAIRING_ID);
        MyPrefs.getInstance().put(Const.KEY_USE_FP, true);
    }

    @Override
    public void onBackPressed() {
        parentActivity.finish();
    }
}
