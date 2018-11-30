package com.anjay.dictionary;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Anjay on 01-03-2016.
 */
class browser extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}