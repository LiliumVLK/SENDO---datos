package com.sendo.app;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;

/** SENDO: app de solo lectura. Muestra la web empaquetada en assets/www y lee los planes cifrados de GitHub. */
public class MainActivity extends Activity {
    static final String HOST = "appassets.androidplatform.net";
    private static final String START = "https://" + HOST + "/www/index.html";
    private WebView web;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setSupportMultipleWindows(false);
        s.setMediaPlaybackRequiresUserGesture(true);
        web.setWebViewClient(new Client(this));
        web.addJavascriptInterface(new Avisos(this), "SendoNative");
        setContentView(web);
        if (state != null) web.restoreState(state);
        else web.loadUrl(START);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // si ya hay una cuenta abierta, pide permiso para avisar (Android 13+) y deja programada la comprobación
        if (!getSharedPreferences(Avisos.PREFS, MODE_PRIVATE).getString("id", "").isEmpty()) {
            Avisos.programar(this);
            web.postDelayed(new Permiso(this), 1200);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        web.saveState(out);
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(window.sendoBack&&window.sendoBack())?'1':'0'", new Back(this));
    }

    void exit() { super.onBackPressed(); }

    @Override
    protected void onDestroy() {
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
