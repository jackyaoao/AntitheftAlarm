package com.antitheft.alarm.fragments;


import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.antitheft.alarm.R;
import com.antitheft.alarm.utils.Const;
import com.inuker.bluetooth.library.search.SearchResult;

import java.util.List;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class DevicePairingFragment extends BaseFragment {

    @BindView(R.id.device_pairing_view)
    RelativeLayout device_pairing_view;
    @BindView(R.id.search_txt)
    TextView search_txt;

    public DevicePairingFragment() {
        // Required empty public constructor
    }

    public static DevicePairingFragment newInstance() {
        return new DevicePairingFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_device_pairing_frgment;
    }

    @Override
    public void initData() {
        bleStartSearch();
    }

    @Override
    public void initViewEvent() {
        ScaleAnimation animation = new ScaleAnimation(0.5f, 1,
                0.5f, 1, Animation.RELATIVE_TO_SELF,
                0.5f,1, 0.5f);
        animation.setDuration(2000);
        //设置持续时间
        animation.setFillAfter(true);
        //设置动画结束之后的状态是否是动画的最终状态，true，表示是保持动画结束时的最终状态
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);
        //设置循环次数，0为1次
        device_pairing_view.startAnimation(animation);
        //开始动画
    }

    @Override
    public int getFragmentId() {
        return Const.DEVICE_PAIRING_ID;
    }

    @Override
    public void onSearchStarted() {
        //showShortToast("onSearchStarted");
    }

    @Override
    public void onDevicesFounded(List<SearchResult> devices) {
        arg = devices;
        goTo(Const.DEVICE_FOUND_ID);
    }

    @Override
    public void onSearchStopped() {
//        showShortToast("onSearchStopped");
    }

    @Override
    public void onSearchCanceled() {
//        showShortToast("onSearchCanceled");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        bleStopSearch();
    }

    @Override
    public void onBackPressed() {
        parentActivity.finish();
    }
}
