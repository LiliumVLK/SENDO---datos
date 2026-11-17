package com.sendo.app;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/** Puente para la página: guarda un PDF del cliente en Descargas y lo abre. */
class Native {
    private final MainActivity act;
    Native(MainActivity a) { act = a; }

    @JavascriptInterface
    public String savePdf(String name, String b64) {
        try {
            byte[] data = Base64.decode(b64, Base64.DEFAULT);
            String safe = name == null || name.trim().isEmpty() ? "SENDO.pdf" : name.replaceAll("[\\\\/:*?\"<>|]+", "_");
            Uri uri;
            if (Build.VERSION.SDK_INT >= 29) {
                ContentResolver cr = act.getContentResolver();
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, safe);
                v.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                v.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/SENDO");
                uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri == null) return "error";
                OutputStream out = cr.openOutputStream(uri);
                if (out == null) return "error";
                try { out.write(data); } finally { out.close(); }
            } else {
                File dir = act.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (dir == null) return "error";
                dir.mkdirs();
                File f = new File(dir, safe);
                FileOutputStream out = new FileOutputStream(f);
                try { out.write(data); } finally { out.close(); }
                DownloadManager dm = (DownloadManager) act.getSystemService(Context.DOWNLOAD_SERVICE);
                long id = dm.addCompletedDownload(safe, "SENDO", true, "application/pdf", f.getAbsolutePath(), f.length(), false);
                uri = dm.getUriForDownloadedFile(id);
                if (uri == null) return "ok";
            }
            act.runOnUiThread(new Opener(act, uri));
            return "ok";
        } catch (Exception e) {
            return "error";
        }
    }
}

class Opener implements Runnable {
    private final MainActivity act;
    private final Uri uri;
    Opener(MainActivity a, Uri u) { act = a; uri = u; }

    @Override
    public void run() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "application/pdf");
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try { act.startActivity(i); } catch (ActivityNotFoundException ignored) { }
    }
}
