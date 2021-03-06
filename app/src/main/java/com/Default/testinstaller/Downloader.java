package com.Default.testinstaller; /***
 Copyright (c) 2008-2012 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain	a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 From _The Busy Coder's Guide to Android Development_
 http://commonsware.com/Android
 */

import android.app.Activity;
import android.app.DownloadManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.Objects;

public class Downloader {
    private DownloadManager mgr=null;
    private long lastDownload=-1L;

    private Activity activity;
    private IProgressListener progressListener;

    public void onCreate(Activity activity) {
        this.activity = activity;

        mgr=(DownloadManager)activity.getSystemService(Activity.DOWNLOAD_SERVICE);
        activity.registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        activity.registerReceiver(onNotificationClick,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    }

    public void onDestroy() {
        activity.unregisterReceiver(onComplete);
        activity.unregisterReceiver(onNotificationClick);
    }

    public void startDownload(String path, IProgressListener progressListener) {
        this.progressListener = progressListener;

        Uri uri=Uri.parse(path);

        progressListener.onProgress( "Starting [" + path +  "] download!");

        lastDownload=
                mgr.enqueue(new DownloadManager.Request(uri)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                                DownloadManager.Request.NETWORK_MOBILE)
                        // .setAllowedOverRoaming(false)
                        // .setMimeType("application/pdf")
                        .setTitle("Demo")
                        .setDescription("Something useful. No, really.")
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                "test.apk"));

        progressListener.onProgress( "Download started!");
    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

                progressListener.onProgress( "Download completed! ");

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                Cursor cursor = manager.query(query);
                if (cursor.moveToFirst()) {
                    if (cursor.getCount() > 0) {
                        int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            String fileName = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI));
                            progressListener.onProgress( "Download completed with [" + fileName + "!");

                            Uri fileUri = Uri.parse(fileName);
                            File file = new File(fileUri.getPath());
                            if (Build.VERSION.SDK_INT >= 24) {
                                fileUri = FileProvider.getUriForFile(Objects.requireNonNull(context),
                                        BuildConfig.APPLICATION_ID + ".provider", file);
                            }

                            progressListener.onProgress( "Opening [" + fileUri.getPath() + "]!");


                            Intent openIntent = new Intent(Intent.ACTION_VIEW, fileUri);
                            openIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                            openIntent.setDataAndType(fileUri, "application/vnd.android" + ".package-archive");
                            openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(openIntent);

                            progressListener.onProgress( "Started ACTION_VIEW!");
                        } else {
                            int message = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
                            progressListener.onProgress( "Download failed with [" + message + "]! ");
                        }
                    }
                }
            }
        }
    };

    BroadcastReceiver onNotificationClick=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, "Ummmm...hi!", Toast.LENGTH_LONG).show();
        }
    };
}
