package com.sendo.app;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.JavascriptInterface;

import org.json.JSONArray;

import java.util.HashSet;
import java.util.Set;

/**
 * Avisos de plan nuevo. La página le pasa la cuenta abierta (archivo y clave) y las semanas que ya ve;
 * cada media hora, con conexión, Comprobador mira en GitHub si ha aparecido una semana nueva y avisa.
 */
public class Avisos {
    static final String PREFS = "sendo.avisos";
    static final int JOB = 1;
    static final String CANAL = "planes";
    private final MainActivity act;
    Avisos(MainActivity a) { act = a; }

    @JavascriptInterface
    public void register(String base, String id, String key, String weeksJson, String lang) {
        SharedPreferences p = act.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Set<String> known = new HashSet<String>(p.getStringSet("weeks", new HashSet<String>()));
        boolean same = id.equals(p.getString("id", ""));
        if (!same) known.clear();
        try {
            JSONArray a = new JSONArray(weeksJson);
            for (int i = 0; i < a.length(); i++) known.add(a.getString(i));
        } catch (Exception ignored) { }
        p.edit().putString("base", base).putString("id", id).putString("key", key)
                .putString("lang", lang == null ? "es" : lang).putStringSet("weeks", known).apply();
        programar(act);
        act.runOnUiThread(new Permiso(act));
    }

    @JavascriptInterface
    public void unregister() {
        act.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
        JobScheduler js = (JobScheduler) act.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (js != null) js.cancel(JOB);
    }

    static void programar(Context c) {
        JobScheduler js = (JobScheduler) c.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (js == null || js.getPendingJob(JOB) != null) return;
        JobInfo job = new JobInfo.Builder(JOB, new ComponentName(c, Comprobador.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(30L * 60L * 1000L)
                .setPersisted(true)
                .build();
        js.schedule(job);
    }

    static void avisar(Context c, int nuevas, String lang) {
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        boolean eu = "eu".equals(lang);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CANAL, eu ? "Asteko planak" : "Planes semanales", NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(ch);
        }
        Intent open = new Intent(c, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(c, 0, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        String title = eu ? "Asteko plan berria" : "Nuevo plan semanal";
        String text = nuevas > 1
                ? (eu ? "Plan berriak dituzu SENDOn. Ireki ikusteko." : "Tienes planes nuevos en SENDO. Ábrelo para verlos.")
                : (eu ? "Zure asteko plana prest dago SENDOn." : "Tu plan de la semana ya está en SENDO.");
        Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(c, CANAL) : new Notification.Builder(c);
        b.setSmallIcon(R.drawable.ic_stat).setContentTitle(title).setContentText(text)
                .setColor(0xFF1F6B5A).setContentIntent(pi).setAutoCancel(true);
        try { nm.notify(7, b.build()); } catch (SecurityException ignored) { }
    }
}

class Permiso implements Runnable {
    private final MainActivity act;
    Permiso(MainActivity a) { act = a; }

    @Override
    public void run() {
        if (Build.VERSION.SDK_INT >= 33 && act.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            SharedPreferences p = act.getSharedPreferences(Avisos.PREFS, Context.MODE_PRIVATE);
            if (p.getString("id", "").isEmpty()) return;
            int n = p.getInt("asks", 0);
            if (n >= 2) return;
            p.edit().putInt("asks", n + 1).apply();
            act.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 9);
        }
    }
}
