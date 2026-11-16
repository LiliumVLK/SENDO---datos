package com.sendo.app;

import android.webkit.ValueCallback;

/** Atrás: primero la web (cerrar hoja, volver a la portada); si no hay nada que cerrar, sale de la app. */
final class Back implements ValueCallback<String> {
    private final MainActivity act;
    Back(MainActivity a) { act = a; }
    @Override public void onReceiveValue(String r) { if (r == null || !r.contains("1")) act.exit(); }
}
