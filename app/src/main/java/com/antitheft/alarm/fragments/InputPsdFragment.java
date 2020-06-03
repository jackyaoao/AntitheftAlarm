package com.antitheft.alarm.fragments;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.antitheft.alarm.R;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.MyPrefs;
import com.antitheft.alarm.view.NumericKeyboard;
import com.antitheft.alarm.view.PasswordTextView;

import butterknife.BindView;

public class InputPsdFragment extends BaseFragment implements NumericKeyboard.OnNumberClick,
        PasswordTextView.OnTextChangedListener {

    private static final String TAG = InputPsdFragment.class.getSimpleName();

    @BindView(R.id.keyboard)
    NumericKeyboard keyboard;
    @BindView(R.id.et_pwd1)
    PasswordTextView passwordTextView1;
    @BindView(R.id.et_pwd2)
    PasswordTextView passwordTextView2;
    @BindView(R.id.et_pwd3)
    PasswordTextView passwordTextView3;
    @BindView(R.id.et_pwd4)
    PasswordTextView passwordTextView4;
    @BindView(R.id.tv_info)
    TextView tv_info;
    @BindView(R.id.fingerprtint_view)
    FrameLayout fingerprtint_view;

    private boolean isUsingFp = false;
    private int action = Const.INPUT_SETTING_PASSWORD;
    private String verifyPsd = null;
    private Handler handler = new Handler(Looper.getMainLooper());
    private String reason;
    private int count = 0;

    public InputPsdFragment() {
        // Required empty public constructor
    }

    public static InputPsdFragment newInstance() {
        return new InputPsdFragment();
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_input_psd;
    }

    @Override
    public void initData() {
        verifyPsd = null;
        if (arg instanceof Integer) {
            action = (Integer) arg;
        }
        isUsingFp = MyPrefs.getInstance().getBoolean(Const.BIOMETRIC);
        if (action == Const.INPUT_SETTING_PASSWORD) {
            fingerprtint_view.setVisibility(View.GONE);
            keyboard.setShowFingprintFlag(false);
            tv_info.setText(getResString(R.string.please_input_pwd));
        } else if (action == Const.INPUT_RESET_PASSWORD) {
            fingerprtint_view.setVisibility(View.GONE);
            keyboard.setShowFingprintFlag(false);
            tv_info.setText(getResString(R.string.reset_pwd));
        } else if (action == Const.INPUT_ENTER_UNLOCK) {
            if (isUsingFp) {
                fingerprtint_view.setVisibility(View.VISIBLE);
                keyboard.setShowFingprintFlag(true);
                authenticate();
            } else {
                fingerprtint_view.setVisibility(View.GONE);
                keyboard.setShowFingprintFlag(false);
            }
            tv_info.setText(getResString(R.string.unlock));
        }
    }

    @Override
    public void onUsePassword() {
        fingerprtint_view.setVisibility(View.GONE);
        keyboard.setShowFingprintFlag(false);
        showShortToast(reason + getResString(R.string.please_input_pwd));
        count = 0;
    }

    @Override
    public void onSucceeded() {
        goTo(Const.LIBRA_CONNECTED_STATE_ID);
        parentActivity.cancelAlarm();
    }

    @Override
    public void onFailed() {
        //showShortToast(AppContext.getContext().getResources().getString(R.string.verify_err, count++));
    }

    @Override
    public void onError(int code, String reason) {
        this.reason = reason;
    }

    @Override
    public void initViewEvent() {
        keyboard.setOnNumberClick(this);
        passwordTextView4.setOnTextChangedListener(this);
    }

    @Override
    public int getFragmentId() {
        return Const.INPUT_PASSWORD_ID;
    }

    private Runnable clearRunnable = () -> clear();

    private void updatePastWordTextView(String number, boolean isDel) {
        if (isDel) {
            if (!TextUtils.isEmpty(passwordTextView4.getTextContent())) {
                passwordTextView4.setTextContent("");
            } else if (!TextUtils.isEmpty(passwordTextView3.getTextContent())) {
                passwordTextView3.setTextContent("");
            } else if (!TextUtils.isEmpty(passwordTextView2.getTextContent())) {
                passwordTextView2.setTextContent("");
            } else if (!TextUtils.isEmpty(passwordTextView1.getTextContent())) {
                passwordTextView1.setTextContent("");
            }
        } else {
            if (TextUtils.isEmpty(passwordTextView1.getTextContent())) {
                passwordTextView1.setTextContent(number);
            } else if (TextUtils.isEmpty(passwordTextView2.getTextContent())) {
                passwordTextView2.setTextContent(number);
            } else if (TextUtils.isEmpty(passwordTextView3.getTextContent())) {
                passwordTextView3.setTextContent(number);
            } else if (TextUtils.isEmpty(passwordTextView4.getTextContent())) {
                passwordTextView4.setTextContent(number);
            }

        }
    }

    private void verify() {
        String password = passwordTextView1.getTextContent() +
                passwordTextView2.getTextContent() +
                passwordTextView3.getTextContent() +
                passwordTextView4.getTextContent();
        handler.postDelayed(clearRunnable, 100);
        if (action == Const.INPUT_RESET_PASSWORD || action == Const.INPUT_SETTING_PASSWORD) {
            if (verifyPsd == null) {
                verifyPsd = password;
                tv_info.setText(getResString(R.string.please_input_pwd_again));
            } else {
                if (verifyPsd.equals(password)) {
                    MyPrefs.getInstance().put(Const.KEY_PSD, password);
                    if (action == Const.INPUT_SETTING_PASSWORD) {
                        if (isUsingFp) {
                            goTo(Const.AUTHORIZATION_ID);
                        } else {
                            goTo(Const.DEVICE_PAIRING_ID);
                        }
                    } else if (action == Const.INPUT_RESET_PASSWORD) {
                        goTo(Const.LIBRA_CONNECTED_STATE_ID);
                    }
                } else {
                    showShortToast(getString(R.string.not_equals));
                }
            }
        } else if (action == Const.INPUT_ENTER_UNLOCK) {
            if (MyPrefs.getInstance().getString(Const.KEY_PSD).equals(password)) {
                goTo(Const.LIBRA_CONNECTED_STATE_ID);
                parentActivity.cancelAlarm();
            } else {
                showShortToast(getString(R.string.pwd_err));
            }
        }
    }

    private void clear() {
        passwordTextView1.setTextContent("");
        passwordTextView2.setTextContent("");
        passwordTextView3.setTextContent("");
        passwordTextView4.setTextContent("");
    }

    @Override
    public void onNumberReturn(int number) {
        updatePastWordTextView(number + "", false);
    }

    @Override
    public void onDelete() {
        updatePastWordTextView("", true);
    }

    @Override
    public void textChanged(String content) {
        verify();
    }

    /*@Override
    public void onWriteResponse(int code, int event) {
        Log.i(String.format("%s write onResponse code = %d, event = %d", TAG, code, event));
        if (code == Const.BLE_REQUEST_SUCCESS) {
            if (event == Const.BLE_FOUND_ACK_W_EVENT || event == Const.BLE_REMOVE_ALARM_W_EVENT) {
                bleWrite(LibraState.getInstance().getMac(),
                        LibraState.getInstance().getDetailItem(),
                        new String(new byte[20]).getBytes(), Const.BLE_CLEAR_W_EVENT);
            } else if (event == Const.BLE_CLEAR_W_EVENT) {
                if (FragmentStack.getInstance().size() == 1) {
                    goTo(Const.LIBRA_PROTECTED_ID);
                } else {
                    goBack();
                }
                bleReStartRead();
            }
        }
    }*/

    @Override
    public void onBackPressed() {
        if (action == Const.INPUT_RESET_PASSWORD) {
            goBack();
        } else if (action == Const.INPUT_ENTER_UNLOCK) {
            showShortToast(getResString(R.string.unlock));
            return;
        } else {
            parentActivity.finish();
        }
    }

/*    @Override
    public void onConnectStatusChanged(String mac, int status) {
        super.onConnectStatusChanged(mac, status);
        if (mac != null && status == Const.STATUS_DEVICE_DISCONNECTED) {
            goTo(Const.INPUT_PASSWORD_ID);
        }
    }*/
}
