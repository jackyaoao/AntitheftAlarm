package com.antitheft.alarm.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.antitheft.alarm.R;
import com.antitheft.alarm.privacy.PrivacyDialog;
import com.antitheft.alarm.privacy.PrivacyPolicyActivity;
import com.antitheft.alarm.utils.Const;
import com.antitheft.alarm.utils.MyPrefs;
import com.antitheft.alarm.utils.SystemUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private long curVersionCode = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        curVersionCode = SystemUtils.getAppVersionCode(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isCheckPrivacy = MyPrefs.getInstance().getBoolean(Const.PRIVACY);
        long versionCode = MyPrefs.getInstance().getLong(Const.VERSION_CODE);
        if (!isCheckPrivacy || versionCode != curVersionCode) {
            showPrivacy();
            return;
        }
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                Intent it = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(it);
            }
        }, 2000);
    }

    /**
     * 显示用户协议和隐私政策
     */
    public void showPrivacy() {
        boolean isCheckPrivacy = MyPrefs.getInstance().getBoolean(Const.PRIVACY);
        long versionCode = MyPrefs.getInstance().getLong(Const.VERSION_CODE);
        if (!isCheckPrivacy || versionCode != curVersionCode) {

            final PrivacyDialog dialog = new PrivacyDialog(this);
            TextView tv_privacy_tips = dialog.findViewById(R.id.tv_privacy_tips);
            TextView btn_exit = dialog.findViewById(R.id.btn_exit);
            TextView btn_enter = dialog.findViewById(R.id.btn_enter);
            dialog.show();

            String string = getResources().getString(R.string.privacy_tips);
            String key1 = getResources().getString(R.string.privacy_tips_key1);
            String key2 = getResources().getString(R.string.privacy_tips_key2);
            int index1 = string.indexOf(key1);
            int index2 = string.indexOf(key2);

            //需要显示的字串
            SpannableString spannedString = new SpannableString(string);
            //设置点击字体颜色
            ForegroundColorSpan colorSpan1 = new ForegroundColorSpan(getResources().getColor(R.color.colorBlue));
            spannedString.setSpan(colorSpan1, index1, index1 + key1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            ForegroundColorSpan colorSpan2 = new ForegroundColorSpan(getResources().getColor(R.color.colorBlue));
            spannedString.setSpan(colorSpan2, index2, index2 + key2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            //设置点击字体大小
            AbsoluteSizeSpan sizeSpan1 = new AbsoluteSizeSpan(16, true);
            spannedString.setSpan(sizeSpan1, index1, index1 + key1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            AbsoluteSizeSpan sizeSpan2 = new AbsoluteSizeSpan(16, true);
            spannedString.setSpan(sizeSpan2, index2, index2 + key2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

            //设置点击事件
            ClickableSpan clickableSpan1 = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Intent intent = new Intent(SplashActivity.this, PrivacyPolicyActivity.class);
                    intent.putExtra(Const.PRIVACY, 1);
                    startActivity(intent);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    //点击事件去掉下划线
                    ds.setUnderlineText(false);
                }
            };
            spannedString.setSpan(clickableSpan1, index1, index1 + key1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spannedString.setSpan(new UnderlineSpan(), index1, index1 + key1.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

            ClickableSpan clickableSpan2 = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    Intent intent = new Intent(SplashActivity.this, PrivacyPolicyActivity.class);
                    intent.putExtra(Const.PRIVACY, 0);
                    startActivity(intent);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    //点击事件去掉下划线
                    ds.setUnderlineText(false);
                }
            };
            spannedString.setSpan(clickableSpan2, index2, index2 + key2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            spannedString.setSpan(new UnderlineSpan(), index2, index2 + key2.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            //设置点击后的颜色为透明，否则会一直出现高亮
            tv_privacy_tips.setHighlightColor(Color.TRANSPARENT);
            //开始响应点击事件
            tv_privacy_tips.setMovementMethod(LinkMovementMethod.getInstance());

            tv_privacy_tips.setText(spannedString);

            //设置弹框宽度占屏幕的80%
            WindowManager m = getWindowManager();
            Display defaultDisplay = m.getDefaultDisplay();
            final WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (defaultDisplay.getWidth() * 0.80);
            dialog.getWindow().setAttributes(params);

            btn_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    MyPrefs.getInstance().put(Const.VERSION_CODE, curVersionCode);
                    MyPrefs.getInstance().put(Const.PRIVACY, false);
                    finish();
                }
            });

            btn_enter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MyPrefs.getInstance().put(Const.VERSION_CODE, curVersionCode);
                    MyPrefs.getInstance().put(Const.PRIVACY, true);
                    dialog.dismiss();
                    finish();
                    Intent it = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(it);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                }else {
                    // SYSTEM_ALERT_WINDOW permission not granted...
                    Toast.makeText(this, "未被授予权限，相关功能不可用", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
