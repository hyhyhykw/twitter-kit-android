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

import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.orhanobut.hawk.Hawk;
import com.twitter.sdk.android.core.internal.network.UrlUtils;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

class OAuthWebViewClient extends WebViewClient {

    interface Listener {
        void onPageFinished(WebView webView, String url);
        void onSuccess(Bundle bundle);
        void onError(WebViewException exception);
    }

    private final String completeUrl;
    private final Listener listener;

    private final boolean nightModel;
    OAuthWebViewClient(String completeUrl, Listener listener) {
        this.completeUrl = completeUrl;
        this.listener = listener;
         nightModel = isNightModel();
    }


    public static final String BRIGHTNESS = "brightness";
    public static final String DAY = "day";
    public static final String NIGHT = "night";


    public static final String INVERT_JS =
            "document.body.style.backgroundColor=\"#19191F\";document.body.style.color=\"#CCCCCC\";";


    public static boolean isNightModel() {
        return TextUtils.equals(Hawk.get(BRIGHTNESS, DAY), NIGHT);
    }


    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (nightModel){
            view.post(() -> view.evaluateJavascript(INVERT_JS, null));
        }
        listener.onPageFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith(completeUrl)) {
            final TreeMap<String, String> params =
                    UrlUtils.getQueryParams(URI.create(url), false);
            final Bundle bundle = new Bundle(params.size());
            for (Map.Entry<String, String> entry : params.entrySet()) {
                bundle.putString(entry.getKey(), entry.getValue());
            }
            listener.onSuccess(bundle);
            return true;
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode,
            String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        listener.onError(new WebViewException(errorCode, description, failingUrl));
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        listener.onError(new WebViewException(error.getPrimaryError(), null, null));
    }
}
