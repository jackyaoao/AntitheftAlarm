package com.antitheft.alarm.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.antitheft.alarm.activity.MainActivity;
import com.antitheft.alarm.listener.IActivityInteractionListener;
import com.antitheft.alarm.listener.IFragmentInteractionListener;
import com.antitheft.alarm.model.DetailItem;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.Log;
import com.antitheft.alarm.utils.MyPrefs;
import com.antitheft.alarm.utils.SystemUtils;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.utils.ByteUtils;

import java.util.List;
import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment implements IActivityInteractionListener {
    protected IFragmentInteractionListener interactionListener;
    protected Object arg;
    private Context mContext;
    public MainActivity parentActivity;

    public void setParentActivity(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, view);
        initViewEvent();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        MyPrefs.getInstance().put(Const.POWER_CONNECTED, SystemUtils.isChargingDisable(getContext()));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof IFragmentInteractionListener) {
            interactionListener = (IFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement IFragmentInteractionListener");
        }
        FragmentStack.getInstance().push(getFragmentId());
        MyPrefs.getInstance().put(Const.FRAGMENT_ID, getFragmentId());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        interactionListener = null;
    }

    protected void goTo(int fragmentId) {
        parentActivity.showFragment(fragmentId, arg);
    }

    protected void goBack() {
        FragmentStack.getInstance().pop();
        goTo(FragmentStack.getInstance().getTop());
    }

    public void setArg(Object arg) {
        this.arg = arg;
    }

    protected void showShortToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        
    }

    protected void showLongToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    protected String getResString(int resId) {
        return getContext().getResources().getString(resId);
    }

    /** Biometrics*/
    public void authenticate() {
        parentActivity.authenticate();
    }

    /** BLE commuication*/
    public void bleStartSearch() {
        parentActivity.bleStartSearch();
    }

    public void bleStopSearch() {
        parentActivity.bleStopSearch();
    }

    public void bleRegisterConnectListenter(String mac) {
        parentActivity.bleRegisterConnectListenter(mac);
    }

    public void bleReStartRead() {
        parentActivity.bleReStartRead();
    }

    public void bleUnRegisterConnectListenter(String mac) {
        parentActivity.bleUnRegisterConnectListenter(mac);
    }

    public void bleConnect(String mac) {
        parentActivity.bleConnect(mac);
    }

    public void bleDisconnect(String mac) {
        parentActivity.bleDisconnect(mac);
    }

    public void alarmStart() {
        parentActivity.alarmStart();
    }

    public void upNotify() {
        parentActivity.upNotify();
    }

    public void alarmStop() {
        parentActivity.alarmStop();
    }

    public void bleRead(String mac, DetailItem item) {
        parentActivity.bleRead(mac, item);
    }

    public void bleWrite(String mac, DetailItem item, byte[] content, int event) {
        parentActivity.bleWrite(mac, item, content, event);
    }

    @Override
    public void onUsePassword() {

    }

    @Override
    public void onSucceeded() {

    }

    @Override
    public void onFailed() {

    }

    @Override
    public void onError(int code, String reason) {

    }

    @Override
    public void onSearchStarted() {

    }

    @Override
    public void onDevicesFounded(List<SearchResult> device) {

    }

    @Override
    public void onSearchStopped() {

    }

    @Override
    public void onSearchCanceled() {

    }

    @Override
    public void onReadResponse(int code, byte[] data) {
        if (code == Const.BLE_REQUEST_SUCCESS) {
            String content = ByteUtils.byteToString(data);
            Log.i(" onReadResponse content:  " + content);
            if ((content.substring(0, 4).equals("7501") && content.contains("414C41524D")) ||
                    (content.substring(0, 4).equals("7601") && content.contains("46494E44"))) {
                arg = Const.INPUT_ENTER_UNLOCK;
                goTo(Const.INPUT_PASSWORD_ID);
            }
        }
    }

    @Override
    public void onWriteResponse(int code, int event) {

    }

    @Override
    public void onConnectedResponse(int code, BleGattProfile profile) {

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onConnectStatusChanged(String mac, int status) {

    }

    @Override
    public void onBluetoothStateChanged(boolean openOrClosed) {

    }

    @Override
    public void onNotify(DetailItem item, byte[] data) {

    }

    @Override
    public void onNotifyResponse(int code) {

    }

    @Override
    public void onUnNotifyResponse(int code) {

    }

    @Override
    public void onPowerChanged(boolean plugged) {
        Log.i("onPowerChanged plugged: " + plugged);
    }

    abstract public int getLayoutId();
    abstract public void initData();
    abstract public void initViewEvent();
    abstract public int getFragmentId();
}
