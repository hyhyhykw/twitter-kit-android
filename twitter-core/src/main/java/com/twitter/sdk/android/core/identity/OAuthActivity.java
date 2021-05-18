/*
 * Copyright (C) 2015 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twitter.sdk.android.core.identity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.blankj.utilcode.util.BarUtils;
import com.orhanobut.hawk.Hawk;
import com.twitter.sdk.android.core.R;
import com.twitter.sdk.android.core.TwitterAuthException;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.internal.TwitterApi;
import com.twitter.sdk.android.core.internal.oauth.OAuth1aService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity for performing OAuth flow when Single Sign On is not available. This activity should not
 * be called directly.
 */
// This activity assumes it will handle configuration changes itself and MUST have the
// following attribute defined in the AndroidManifest.xml
// file: android:configChanges="orientation|screenSize"
public class OAuthActivity extends AppCompatActivity implements OAuthController.Listener {

    static final String EXTRA_AUTH_CONFIG = "auth_config";

    private static final String STATE_PROGRESS = "progress";

    OAuthController oAuthController;

    private ProgressBar spinner;
    private WebView webView;

    public static final String BRIGHTNESS = "brightness";
    public static final String DAY = "day";
    public static final String NIGHT = "night";


    public static final String INVERT_JS =
            "document.body.style.backgroundColor=\"#19191F\";document.body.style.color=\"#CCCCCC\";";


    public static boolean isNightModel() {
        return TextUtils.equals(Hawk.get(BRIGHTNESS, DAY), NIGHT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tw__activity_oauth);

        boolean nightModel = isNightModel();
        BarUtils.setStatusBarLightMode(this, !nightModel);
        spinner = findViewById(R.id.tw__spinner);
        webView = findViewById(R.id.tw__web_view);

        if (nightModel) {
            setBrightness();
            webView.getSettings().setJavaScriptEnabled(true);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    view.post(() -> view.evaluateJavascript(INVERT_JS, null));
                }
            });
        }

        final boolean showProgress;
        if (savedInstanceState != null) {
            showProgress = savedInstanceState.getBoolean(STATE_PROGRESS, false);
        } else {
            showProgress = true;
        }
        spinner.setVisibility(showProgress ? View.VISIBLE : View.GONE);

        final TwitterCore kit = TwitterCore.getInstance();
        oAuthController = new OAuthController(spinner, webView,
                getIntent().getParcelableExtra(EXTRA_AUTH_CONFIG),
                new OAuth1aService(kit, new TwitterApi()), this);
        oAuthController.startAuth();
    }


    protected final void setBrightness() {
        //降低屏幕亮度
        Window localWindow = getWindow();
        WindowManager.LayoutParams localLayoutParams = localWindow.getAttributes();
        localLayoutParams.screenBrightness = 0.1f;
        localWindow.setAttributes(localLayoutParams);
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (spinner.getVisibility() == View.VISIBLE) {
            outState.putBoolean(STATE_PROGRESS, true);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        oAuthController.handleAuthError(RESULT_CANCELED,
                new TwitterAuthException("Authorization failed, request was canceled."));
    }

    @Override
    public void onComplete(int resultCode, Intent data) {
        setResult(resultCode, data);
        finish();
    }
}
