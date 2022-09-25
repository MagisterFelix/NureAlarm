package com.nure.alarm.core.updater;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.nure.alarm.BuildConfig;
import com.nure.alarm.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Updater {

    private static final String LATEST_RELEASE = "https://api.github.com/repos/MagisterFelix/NureAlarm/releases/latest";

    private String tag;
    private String url;

    private final Activity activity;
    private final Context context;

    public Updater(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }
    
    private boolean noReadOrWritePermissions() {
        boolean noReadPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        boolean noWritePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
        return noReadPermission || noWritePermission;
    }

    private void requestPermissions() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(activity, permissions, 0);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void update() {
        String fileName = context.getString(R.string.app_name) + ".apk";
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;
        final Uri uri = Uri.parse("file://" + destination);

        File apk = new File(destination);
        if (apk.exists()) {
            apk.delete();
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(context.getString(R.string.app_name));
        request.setDescription(context.getString(R.string.downloading));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setVisibleInDownloadsUi(false);
        request.setDestinationUri(uri);

        final DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

        BroadcastReceiver onComplete = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                Uri contentUri = FileProvider.getUriForFile(
                        context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        new File(destination));

                Intent install = new Intent(Intent.ACTION_VIEW);
                install.setDataAndType(contentUri, "application/vnd.android.package-archive");
                install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(install);

                context.unregisterReceiver(this);
            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void checkForUpdates(View root, BottomNavigationView menu) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(LATEST_RELEASE).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
                    tag = jsonObject.getString("tag_name");
                    if (tag.charAt(0) == 'v') {
                        tag = tag.substring(1);
                    }
                    url = new JSONObject(jsonObject.getJSONArray("assets").get(0).toString()).getString("browser_download_url");

                    if (!tag.equals(BuildConfig.VERSION_NAME)) {
                        Snackbar.make(root, R.string.update_available, Snackbar.LENGTH_INDEFINITE).setAnchorView(menu).setAction(R.string.update, view -> {
                            if (noReadOrWritePermissions()) {
                                requestPermissions();
                            } else {
                                update();
                            }
                        }).show();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {}
        });
    }
}