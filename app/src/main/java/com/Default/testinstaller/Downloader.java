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
        // setContentView(R.layout.main);

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

        //Environment
        //        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        //        .mkdirs();

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





        // File directory = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        // File file = new File(directory, "test.apk");

        // progressListener.onProgress( "Starting [" + path +  "] download to [" + file.getAbsolutePath() + "]!");

        // //Delete update file if exists
        // if (file.exists()) {
        //     progressListener.onProgress( "Deleting [" + file.getAbsolutePath() +  "] before download!");
        //     file.delete();
        // }

        // Uri fileUri = Uri.fromFile(file);

        // lastDownload=
        //         mgr.enqueue(new DownloadManager.Request(uri)
        //                 .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
        //                         DownloadManager.Request.NETWORK_MOBILE)
        //                 // .setAllowedOverRoaming(false)
        //                 // .setMimeType("application/pdf")
        //                 .setTitle("Demo")
        //                 .setDescription("Something useful. No, really.")
        //                 .setDestinationUri(fileUri));










        // File dest = Environment.getExternalStorageDirectory();

        progressListener.onProgress( "Download started!");

        // v.setEnabled(false);
        // findViewById(R.id.query).setEnabled(true);
    }

    public void queryStatus() {
        Cursor c = mgr.query(new DownloadManager.Query().setFilterById(lastDownload));

        if (c==null) {
            Toast.makeText(activity, "Download not found!", Toast.LENGTH_LONG).show();
        }
        else {
            c.moveToFirst();

            Log.d(getClass().getName(), "COLUMN_ID: "+
                    c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
            Log.d(getClass().getName(), "COLUMN_BYTES_DOWNLOADED_SO_FAR: "+
                    c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
            Log.d(getClass().getName(), "COLUMN_LAST_MODIFIED_TIMESTAMP: "+
                    c.getLong(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)));
            Log.d(getClass().getName(), "COLUMN_LOCAL_URI: "+
                    c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
            Log.d(getClass().getName(), "COLUMN_STATUS: "+
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)));
            Log.d(getClass().getName(), "COLUMN_REASON: "+
                    c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));

            Toast.makeText(activity, statusMessage(c), Toast.LENGTH_LONG).show();
        }
    }

    public void viewLog() {
        activity.startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
    }

    private String statusMessage(Cursor c) {
        String msg="???";

        switch(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
            case DownloadManager.STATUS_FAILED:
                msg="Download failed!";
                break;

            case DownloadManager.STATUS_PAUSED:
                msg="Download paused!";
                break;

            case DownloadManager.STATUS_PENDING:
                msg="Download pending!";
                break;

            case DownloadManager.STATUS_RUNNING:
                msg="Download in progress!";
                break;

            case DownloadManager.STATUS_SUCCESSFUL:
                msg="Download complete!";
                break;

            default:
                msg="Download is nowhere in sight";
                break;
        }

        return(msg);
    }


    // Request code for selecting a PDF document.
    private static final int PICK_PDF_FILE = 2;


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
                        int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            String fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            progressListener.onProgress( "Download completed with [" + fileName + "!");

                            // Intent openIntent = new Intent(Intent.ACTION_VIEW);
                            // openIntent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
                            // openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            // context.startActivity(openIntent);


                            Uri fileUri = Uri.parse(fileName);
                            File file = new File(fileUri.getPath());
                            if (Build.VERSION.SDK_INT >= 24) {
                                fileUri = FileProvider.getUriForFile(Objects.requireNonNull(context),
                                        BuildConfig.APPLICATION_ID + ".provider", file);

                                // fileUri = FileProvider.getUriForFile(context, context.getPackageName(), file);
                            }

                            progressListener.onProgress( "Opening [" + fileUri.getPath() + "]!");


                            Intent openIntent = new Intent(Intent.ACTION_VIEW, fileUri);
                            openIntent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                            openIntent.setDataAndType(fileUri, "application/vnd.android" + ".package-archive");
                            openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            context.startActivity(openIntent);

                            progressListener.onProgress( "Started ACTION_VIEW!");

                            // So something here on success
                        } else {
                            int message = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                            // So something here on failed.
                            progressListener.onProgress( "Download failed with [" + message + "]! ");
                        }
                    }
                }
            }

            return;

            // Intent newIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            // newIntent.addCategory(Intent.CATEGORY_OPENABLE);
            // newIntent.setType("application/pdf");

            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            // newIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

            // activity.startActivityForResult(newIntent,  PICK_PDF_FILE);


            // findViewById(R.id.start).setEnabled(true);
        }
    };

    BroadcastReceiver onNotificationClick=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            Toast.makeText(ctxt, "Ummmm...hi!", Toast.LENGTH_LONG).show();
        }
    };
}
