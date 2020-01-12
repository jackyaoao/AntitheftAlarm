package com.antitheft.alarm.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.antitheft.alarm.AppContext;
import com.antitheft.alarm.R;
import com.antitheft.alarm.model.LibraState;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.MyPrefs;
import com.antitheft.alarm.view.NumericKeyboard;
import com.antitheft.alarm.view.PasswordTextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlarmHandlerActivity extends BaseActivity implements NumericKeyboard.OnNumberClick,
        PasswordTextView.OnTextChangedListener {

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
    private Handler handler = new Handler(Looper.getMainLooper());
    private String reason;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alarm_handler);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.CENTER);
        setFinishOnTouchOutside(false);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isUsingFp = MyPrefs.getInstance().getBoolean(Const.BIOMETRIC);
        if (isUsingFp) {
            fingerprtint_view.setVisibility(View.VISIBLE);
            keyboard.setShowFingprintFlag(true);
            try {
                antitheftAlarmService.biometricenable();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            fingerprtint_view.setVisibility(View.GONE);
            keyboard.setShowFingprintFlag(false);
        }
        tv_info.setText(getResString(R.string.unlock));
        keyboard.setOnNumberClick(this);
        passwordTextView4.setOnTextChangedListener(this);
    }

    private Runnable clearRunnable = () -> clear();

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        PowerManager powerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        //点亮屏幕
        @SuppressLint("InvalidWakeLockTag")
        PowerManager.WakeLock wl = powerManager.newWakeLock(
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "bright");
        wl.acquire(10000);
        wl.release();
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
        fingerprtint_view.setVisibility(View.GONE);
        keyboard.setShowFingprintFlag(false);
        cancelAlarm();
    }

    @Override
    public void onFailed() {
        showShortToast(AppContext.getContext().getResources().getString(R.string.verify_err, count++));
    }

    @Override
    public void onError(int code, String reason) {
        this.reason = reason;
    }

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
        if (MyPrefs.getInstance().getString(Const.KEY_PSD).equals(password)) {
            cancelAlarm();
        } else {
            showShortToast(getString(R.string.pwd_err));
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

    @Override
    public void onWriteResponse(int code, int event) {
        if (code == Const.BLE_REQUEST_SUCCESS) {
            if (event == Const.BLE_FOUND_ACK_W_EVENT || event == Const.BLE_REMOVE_ALARM_W_EVENT) {
                bleWrite(LibraState.getInstance().getMac(),
                        LibraState.getInstance().getDetailItem(),
                        new String(new byte[20]).getBytes(), Const.BLE_CLEAR_W_EVENT);
            } else if (event == Const.BLE_CLEAR_W_EVENT) {
                bleReStartRead();
                finish();
            }
        }
    }

}
