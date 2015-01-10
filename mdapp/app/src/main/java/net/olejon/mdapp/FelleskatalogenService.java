package net.olejon.mdapp;

import android.app.DownloadManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FelleskatalogenService extends Service
{
    public static final String FELLESKATALOGEN_SERVICE_RESULT_RECEIVER_INTENT_EXTRA = "net.olejon.mdapp.FELLESKATALOGEN_SERVICE_RESULT_RECEIVER_INTENT_EXTRA";

    public static final int NOTIFICATION_ID = 1;

    private Context mContext;

    private MyTools mTools;

    private Intent mIntent;

    private ConnectivityManager mConnectivityManager;

    private DownloadManager mDownloadManager;
    private BroadcastReceiver mDownloadManagerBroadcastReceiver;
    private Handler mDownloadManagerHandler;
    private Runnable mDownloadManagerRunnable;
    private File mDownloadManagerFile;

    private boolean mDownloadOnlyOnWifi;
    private boolean mUpdateManually;
    private boolean mUpdateTesting;

    private int mProjectVersionCode;

    @Override
    public void onCreate()
    {
        mDownloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        mDownloadManagerBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if(intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) updateFelleskatalogen();
            }
        };

        registerReceiver(mDownloadManagerBroadcastReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mContext = this;

        mTools = new MyTools(mContext);

        mIntent = intent;

        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        mDownloadOnlyOnWifi = mTools.getDefaultSharedPreferencesBoolean("SQLITE_DATABASE_FELLESKATALOGEN_UPDATE_ONLY_ON_WIFI");

        mUpdateManually = (mIntent.getAction() != null && mIntent.getAction().equals("manually"));
        mUpdateTesting = (mIntent.getAction() != null && mIntent.getAction().equals("testing"));

        mDownloadManagerFile = new File(mContext.getExternalFilesDir(null), FelleskatalogenSQLiteHelper.DB_ZIPPED_NAME);

        mProjectVersionCode = mTools.getProjectVersionCode();

        if(mTools.isDeviceConnected())
        {
            boolean update = false;

            if(mDownloadOnlyOnWifi)
            {
                NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if(networkInfo.isConnected())
                {
                    update = true;
                }
                else
                {
                    if(mUpdateManually || mUpdateTesting) mTools.showToast(getString(R.string.service_felleskatalogen_only_update_on_wifi), 1);
                }
            }
            else
            {
                update = true;
            }

            if(update)
            {
                RequestQueue requestQueue = Volley.newRequestQueue(mContext);

                String uriAppend = (mUpdateTesting) ? "&testing" : "";

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website)+"api/1/felleskatalogen/db/?version_code="+mProjectVersionCode+uriAppend, null, new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response)
                    {
                        try
                        {
                            final String md5 = response.getString("md5");
                            final String file = response.getString("file");

                            final String lastMd5 = mTools.getSharedPreferencesString("SQLITE_DATABASE_FELLESKATALOGEN_LAST_MD5_"+mProjectVersionCode);

                            if(md5.equals(lastMd5) && !mUpdateTesting)
                            {
                                if(mUpdateManually) mTools.showToast(getString(R.string.service_felleskatalogen_no_update_available), 1);
                            }
                            else
                            {
                                if(lastMd5.equals("") && !mUpdateTesting)
                                {
                                    if(mUpdateManually) mTools.showToast(getString(R.string.service_felleskatalogen_no_update_available), 1);
                                }
                                else
                                {
                                    if(mUpdateManually) mTools.showToast(getString(R.string.service_felleskatalogen_updating), 1);

                                    downloadFelleskatalogen(file);
                                }

                                mTools.setSharedPreferencesString("SQLITE_DATABASE_FELLESKATALOGEN_LAST_MD5_"+mProjectVersionCode, md5);
                            }
                        }
                        catch(Exception e)
                        {
                            Log.e("FelleskatalogenService", Log.getStackTraceString(e));
                        }
                    }
                }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Log.e("FelleskatalogenService", error.toString());
                    }
                });

                requestQueue.add(jsonObjectRequest);
            }
        }
        else
        {
            if(mUpdateManually) mTools.showToast(getString(R.string.device_not_connected), 1);
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onDestroy()
    {
        if(mDownloadManagerHandler != null) mDownloadManagerHandler.removeCallbacks(mDownloadManagerRunnable);
        if(mDownloadManagerBroadcastReceiver != null) unregisterReceiver(mDownloadManagerBroadcastReceiver);
    }

    private void downloadFelleskatalogen(String file)
    {
        if(!mTools.isDeviceConnected()) return;

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(getString(R.string.project_website)+"api/1/felleskatalogen/db/"+file));

        request.setVisibleInDownloadsUi(false);
        request.setAllowedOverRoaming(false);
        request.setDestinationInExternalFilesDir(mContext, null, FelleskatalogenSQLiteHelper.DB_ZIPPED_NAME);
        request.setTitle(getString(R.string.service_felleskatalogen_request_title));

        if(!mUpdateManually && !mUpdateTesting) request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        boolean deleteFile = mDownloadManagerFile.delete();

        if(deleteFile) Log.w("FelleskatalogenService", "mDownloadManagerFile deleted");

        final long downloadManagerRequestId = mDownloadManager.enqueue(request);

        final DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadManagerRequestId);

        mDownloadManagerHandler = new Handler();

        mDownloadManagerRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if(mDownloadOnlyOnWifi && !networkInfo.isConnected())
                {
                    mDownloadManagerHandler.removeCallbacks(mDownloadManagerRunnable);

                    mDownloadManager.remove(downloadManagerRequestId);

                    Log.w("FelleskatalogenService", "mDownloadManagerFile canceled");
                }
                else
                {
                    Cursor cursor = mDownloadManager.query(query);
                    cursor.moveToFirst();

                    try
                    {
                        int size = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        int downloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));

                        float progress = (float) downloaded / size;
                        int percentage = Math.round(progress * 100);

                        Log.w("FelleskatalogenService", "mDownloadManagerFile "+String.valueOf(percentage)+"%");
                    }
                    catch(Exception e)
                    {
                        Log.e("MainActivity", Log.getStackTraceString(e));
                    }

                    cursor.close();

                    mDownloadManagerHandler.postDelayed(this, 250);
                }
            }
        };

        mDownloadManagerHandler.postDelayed(mDownloadManagerRunnable, 250);
    }

    private void updateFelleskatalogen()
    {
        if(mDownloadManagerHandler != null) mDownloadManagerHandler.removeCallbacks(mDownloadManagerRunnable);

        try
        {
            InputStream inputStream = new FileInputStream(mDownloadManagerFile);

            FelleskatalogenSQLiteCopyHelper felleskatalogenSQLiteCopyHelper = new FelleskatalogenSQLiteCopyHelper(mContext);
            felleskatalogenSQLiteCopyHelper.copy(inputStream);

            Log.w("FelleskatalogenService", "mDownloadManagerFile copied");

            boolean deleteFile = mDownloadManagerFile.delete();

            if(deleteFile) Log.w("FelleskatalogenService", "mDownloadManagerFile deleted");

            mTools.setSharedPreferencesBoolean("SQLITE_DATABASE_FELLESKATALOGEN_HAS_BEEN_UPDATED", true);

            if(mUpdateManually)
            {
                ResultReceiver resultReceiver = mIntent.getParcelableExtra(FELLESKATALOGEN_SERVICE_RESULT_RECEIVER_INTENT_EXTRA);
                resultReceiver.send(0, null);
            }

            if(!mUpdateManually)
            {
                Intent launchIntent = new Intent(mContext, MainActivity.class);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

                builder.setWhen(mTools.getCurrentTime())
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setCategory(Notification.CATEGORY_MESSAGE)
                        .setLargeIcon(bitmap)
                        .setSmallIcon(R.drawable.ic_local_library_white_24dp)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setLights(Color.BLUE, 1000, 2000)
                        .setContentIntent(launchPendingIntent)
                        .setTicker(getString(R.string.service_felleskatalogen_notification_has_been_updated))
                        .setContentTitle(getString(R.string.service_felleskatalogen_notification_has_been_updated))
                        .setContentText(getString(R.string.service_felleskatalogen_notification_use_updated_version))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.service_felleskatalogen_notification_tap_to_use_updated_version)))
                        .addAction(R.drawable.ic_local_library_white_24dp, getString(R.string.service_felleskatalogen_notification_use), launchPendingIntent);

                notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
            }
        }
        catch(Exception e)
        {
            Log.e("FelleskatalogenService", Log.getStackTraceString(e));
        }
    }
}