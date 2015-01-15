package net.olejon.mdapp;

/*

Copyright 2015 Ole Jon Bjørkum

This file is part of LegeAppen.

LegeAppen is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

LegeAppen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with LegeAppen.  If not, see <http://www.gnu.org/licenses/>.

*/

import android.app.DownloadManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.Calendar;

class MyTools
{
    private final Context mContext;

    private Toast mToast;

    public MyTools(Context context)
    {
        mContext = context;
    }

    // Default shared preferences
    public boolean getDefaultSharedPreferencesBoolean(String preference)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sharedPreferences.getBoolean(preference, false);
    }

    // Shared preferences
    public String getSharedPreferencesString(String preference)
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("SHARED_PREFERENCES", 0);
        return sharedPreferences.getString(preference, "");
    }

    public void setSharedPreferencesString(String preference, String string)
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("SHARED_PREFERENCES", 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putString(preference, string);
        sharedPreferencesEditor.apply();
    }

    public boolean getSharedPreferencesBoolean(String preference)
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("SHARED_PREFERENCES", 0);
        return sharedPreferences.getBoolean(preference, false);
    }

    public void setSharedPreferencesBoolean(String preference, boolean bool)
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("SHARED_PREFERENCES", 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean(preference, bool);
        sharedPreferencesEditor.apply();
    }

    public long getSharedPreferencesLong(String preference)
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("SHARED_PREFERENCES", 0);
        return sharedPreferences.getLong(preference, 0);
    }

    public void setSharedPreferencesLong(String preference, long l)
    {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("SHARED_PREFERENCES", 0);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putLong(preference, l);
        sharedPreferencesEditor.apply();
    }

    // Project version
    public int getProjectVersionCode()
    {
        int code = 0;

        try
        {
            code = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionCode;
        }
        catch(Exception e)
        {
            Log.e("MyTools", Log.getStackTraceString(e));
        }

        return code;
    }

    // Time
    public long getCurrentTime()
    {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTimeInMillis();
    }

    // Database
    public String sqe(String string)
    {
        return DatabaseUtils.sqlEscapeString(string);
    }

    // Network
    public boolean isDeviceConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return (networkInfo != null && networkInfo.isConnected());
    }

    // Open URI
    public void openUri(String uri)
    {
        if(uri.equals(""))
        {
            showToast(mContext.getString(R.string.mytools_open_uri_invalid), 1);
        }
        else
        {
            try
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mContext.getString(R.string.project_website)+"?page=redirect&uri="+URLEncoder.encode(uri, "utf-8")));
                mContext.startActivity(intent);
            }
            catch(Exception e)
            {
                Log.e("MyTools", Log.getStackTraceString(e));
            }
        }
    }

    // Download file
    public void downloadFile(String title, String uri)
    {
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));

        request.setAllowedOverRoaming(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(title);

        downloadManager.enqueue(request);
    }

    // Check if tablet
    public boolean isTablet()
    {
        int size = mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        return ((size) == Configuration.SCREENLAYOUT_SIZE_LARGE || (size) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
    }

    // Toast
    public void showToast(String toast, int length)
    {
        if(mToast != null) mToast.cancel();

        mToast = Toast.makeText(mContext, toast, length);
        mToast.show();
    }

    // Set view background
    public void setBackgroundDrawable(View view, int drawable)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            view.setBackground(mContext.getResources().getDrawable(drawable));
        }
        else
        {
            view.setBackgroundDrawable(mContext.getResources().getDrawable(drawable));
        }
    }

    // Medication
    public long getMedicationIdFromUri(String uri)
    {
        SQLiteDatabase sqLiteDatabase = new FelleskatalogenSQLiteHelper(mContext).getReadableDatabase();

        String[] queryColumns = {FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_ID};

        Cursor cursor = sqLiteDatabase.query(FelleskatalogenSQLiteHelper.TABLE_MEDICATIONS, queryColumns, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_URI+" = "+sqe(uri), null, null, null, null);

        long id = 0;

        if(cursor.moveToFirst())
        {
            try
            {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_ID));
            }
            catch(Exception e)
            {
                Log.e("MyTools", Log.getStackTraceString(e));
            }
        }

        cursor.close();
        sqLiteDatabase.close();

        return id;
    }

    public void getMedicationWithFullContent(String uri)
    {
        GetMedicationWithFullContentTask getMedicationWithFullContentTask = new GetMedicationWithFullContentTask();
        getMedicationWithFullContentTask.execute(uri);
    }

    private class GetMedicationWithFullContentTask extends AsyncTask<String, Void, Long>
    {
        @Override
        protected void onPostExecute(Long id)
        {
            if(id == 0)
            {
                showToast(mContext.getString(R.string.mytools_could_not_find_medication), 1);
            }
            else
            {
                Intent intent = new Intent(mContext, MedicationActivity.class);
                intent.putExtra("id", id);
                mContext.startActivity(intent);
            }
        }

        @Override
        protected Long doInBackground(String... strings)
        {
            return getMedicationIdFromUri(strings[0]);
        }
    }

    // Substance
    public long getSubstanceIdFromUri(String uri)
    {
        SQLiteDatabase sqLiteDatabase = new FelleskatalogenSQLiteHelper(mContext).getReadableDatabase();

        String[] queryColumns = {FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_ID};

        Cursor cursor = sqLiteDatabase.query(FelleskatalogenSQLiteHelper.TABLE_SUBSTANCES, queryColumns, FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_URI+" = "+sqe(uri), null, null, null, null);

        long id = 0;

        if(cursor.moveToFirst())
        {
            try
            {
                id = cursor.getLong(cursor.getColumnIndexOrThrow(FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_ID));
            }
            catch(Exception e)
            {
                Log.e("MyTools", Log.getStackTraceString(e));
            }
        }

        cursor.close();
        sqLiteDatabase.close();

        return id;
    }

    // Widget
    public void updateWidget()
    {
        ComponentName componentName = new ComponentName(mContext, Widget.class);

        int[] appWidgetIds = AppWidgetManager.getInstance(mContext).getAppWidgetIds(componentName);

        Intent intent = new Intent(mContext, Widget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        mContext.sendBroadcast(intent);
    }
}