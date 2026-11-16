package com.sendo.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.InputStream;

/** Sirve assets/ en https://appassets.androidplatform.net/ y abre fuera los enlaces externos. */
final class Client extends WebViewClient {
    private final Activity act;
    Client(Activity a) { act = a; }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest req) {
        Uri u = req.getUrl();
        if (!MainActivity.HOST.equals(u.getHost())) return null;
        String path = u.getPath();
        if (path == null || path.contains("..")) return null;
        try {
            InputStream in = act.getAssets().open(path.substring(1));
            return new WebResourceResponse(mime(path), "utf-8", in);
        } catch (Exception e) {
            return new WebResourceResponse("text/plain", "utf-8", 404, "Not Found", null, null);
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
        Uri u = req.getUrl();
        if (MainActivity.HOST.equals(u.getHost())) return false;
        try { act.startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) { }
        return true;
    }

    static String mime(String p) {
        if (p.endsWith(".html")) return "text/html";
        if (p.endsWith(".js")) return "text/javascript";
        if (p.endsWith(".css")) return "text/css";
        if (p.endsWith(".png")) return "image/png";
        if (p.endsWith(".svg")) return "image/svg+xml";
        if (p.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}
