package com.antitheft.alarm.fragments;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.antitheft.alarm.R;
import com.antitheft.alarm.activity.MainActivity;
import com.antitheft.alarm.privacy.PrivacyPolicyActivity;
import com.antitheft.alarm.utils.Const;

import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

import static com.antitheft.alarm.utils.Const.DEVICE_PAIRING_ID;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends BaseFragment {

    @BindView(R.id.pairing)
    LinearLayout pairing;
    @BindView(R.id.restpsd)
    LinearLayout restpsd;
    @BindView(R.id.manual)
    LinearLayout manual;

    private static SettingFragment instance;

    public SettingFragment() {
        // Required empty public constructor
    }

    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_setting;
    }

    @Override
    public void initData() {

    }

    @Override
    public void initViewEvent() {

    }

    @Override
    public int getFragmentId() {
        return Const.SETTING_FRAGMENT_ID;
    }

    @OnClick({R.id.pairing, R.id.restpsd, R.id.manual})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pairing:
                goTo(DEVICE_PAIRING_ID);
                break;
            case R.id.restpsd:
                arg = Const.INPUT_RESET_PASSWORD;
                goTo(Const.INPUT_PASSWORD_ID);
                break;
            case R.id.manual:
                Intent intent = new Intent(getContext(), PrivacyPolicyActivity.class);
                intent.putExtra(Const.PRIVACY, 2);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        goBack();
    }
}
