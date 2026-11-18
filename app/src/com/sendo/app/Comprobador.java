package com.sendo.app;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/** Comprueba en segundo plano si el plan del cliente tiene semanas nuevas. */
public class Comprobador extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        new Thread(new Tarea(this, params)).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) { return true; }

    static void comprobar(Context c) throws Exception {
        SharedPreferences p = c.getSharedPreferences(Avisos.PREFS, Context.MODE_PRIVATE);
        String base = p.getString("base", ""), id = p.getString("id", ""), key = p.getString("key", "");
        if (base.isEmpty() || id.isEmpty() || key.isEmpty()) return;
        HttpURLConnection h = (HttpURLConnection) new URL(base + "c/" + id + ".json?t=" + System.currentTimeMillis()).openConnection();
        h.setConnectTimeout(15000);
        h.setReadTimeout(20000);
        h.setUseCaches(false);
        if (h.getResponseCode() != 200) { h.disconnect(); return; }
        InputStream in = h.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        in.close();
        h.disconnect();
        JSONObject box = new JSONObject(out.toString("UTF-8"));
        Cipher ci = Cipher.getInstance("AES/GCM/NoPadding");
        ci.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Base64.decode(key, Base64.DEFAULT), "AES"),
                new GCMParameterSpec(128, Base64.decode(box.getString("iv"), Base64.DEFAULT)));
        JSONObject d = new JSONObject(new String(ci.doFinal(Base64.decode(box.getString("ct"), Base64.DEFAULT)), "UTF-8"));
        JSONObject sem = d.optJSONObject("semanas");
        if (sem == null) return;
        Set<String> known = new HashSet<String>(p.getStringSet("weeks", new HashSet<String>()));
        int nuevas = 0;
        Iterator<String> it = sem.keys();
        while (it.hasNext()) { if (known.add(it.next())) nuevas++; }
        if (nuevas == 0) return;
        if (!id.equals(p.getString("id", ""))) return;
        p.edit().putStringSet("weeks", known).apply();
        Avisos.avisar(c, nuevas, p.getString("lang", "es"));
    }
}

class Tarea implements Runnable {
    private final Comprobador svc;
    private final JobParameters params;
    Tarea(Comprobador s, JobParameters p) { svc = s; params = p; }

    @Override
    public void run() {
        try { Comprobador.comprobar(svc); } catch (Exception ignored) { }
        svc.jobFinished(params, false);
    }
}
