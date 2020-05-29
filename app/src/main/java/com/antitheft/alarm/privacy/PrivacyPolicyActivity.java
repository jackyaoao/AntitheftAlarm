package com.antitheft.alarm.privacy;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.antitheft.alarm.R;
import com.antitheft.alarm.utils.Const;

public class PrivacyPolicyActivity extends Activity {

    private static final String TAG = PrivacyPolicyActivity.class.getSimpleName();

    private FrameLayout web_view_container;
    private WebView web_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        initView();
    }

    private void initView() {

        web_view_container = findViewById(R.id.web_view_container);
        web_view = new WebView(getApplicationContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        web_view.setLayoutParams(params);
        web_view.setWebViewClient(new WebViewClient());

        //动态添加WebView，解决在xml引用WebView持有Activity的Context对象，导致内存泄露
        web_view_container.addView(web_view);
        int action = getIntent().getIntExtra(Const.PRIVACY, 0);
        if (action == 0) {
            web_view.loadUrl("file:///android_asset/Privacy_Protection_Disclaimer.html");
        } else if (action == 1) {
            web_view.loadUrl("file:///android_asset/User_Agreement.html");
        } else if (action == 2) {
            web_view.loadUrl("file:///android_asset/My007_User_Manual.html");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        web_view_container.removeAllViews();
        web_view.destroy();
    }
}
