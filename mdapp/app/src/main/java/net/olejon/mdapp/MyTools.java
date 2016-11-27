package net.olejon.mdapp;

/*

Copyright 2016 Ole Jon Bjørkum

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see http://www.gnu.org/licenses/.

*/

import android.app.Activity;
import android.app.DownloadManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

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

    public String getProjectVersionName()
    {
        String name = "0.0";

        try
        {
            name = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
        }
        catch(Exception e)
        {
            Log.e("MyTools", Log.getStackTraceString(e));
        }

        return name;
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

    // Download file
    public void downloadFile(String title, String uri)
    {
        DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));

        request.setAllowedOverRoaming(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(title);

        downloadManager.enqueue(request);

        showToast(mContext.getString(R.string.mytools_downloading), 1);
    }

    // Get device
    public String getDevice()
    {
        String device = "";

        try
        {
            device = (Build.MANUFACTURER == null || Build.MODEL == null || Build.VERSION.SDK_INT < 1) ? "" : URLEncoder.encode(Build.MANUFACTURER+" "+Build.MODEL+" "+Build.VERSION.SDK_INT, "utf-8");
        }
        catch(Exception e)
        {
            Log.e("MyTools", Log.getStackTraceString(e));
        }

        return device;
    }

    // Check if tablet
    public boolean isTablet()
    {
        int size = mContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

        return ((size) == Configuration.SCREENLAYOUT_SIZE_LARGE || (size) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
    }

    // Strings
    public String firstToUpper(String string)
    {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    // Statusbar
    public void setStatusbarColor(Activity activity, int color)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) activity.getWindow().setStatusBarColor(ContextCompat.getColor(mContext, color));
    }

    // Toast
    public void showToast(String toast, int length)
    {
        if(mToast != null) mToast.cancel();

        mToast = Toast.makeText(mContext, toast, length);
        mToast.show();
    }

    // Printing
    public void printDocument(WebView webView, String title)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);

            //noinspection deprecation
            PrintDocumentAdapter printDocumentAdapter = webView.createPrintDocumentAdapter();

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) printDocumentAdapter = webView.createPrintDocumentAdapter(title);

            PrintJob printJob = printManager.print(title, printDocumentAdapter, new PrintAttributes.Builder().build());

            List<PrintJob> printJobs = printManager.getPrintJobs();

            printJobs.add(printJob);
        }
        else
        {
            showToast(mContext.getString(R.string.mytools_printing_not_supported), 1);
        }
    }

    // Open URI
    public void openUri(String uri)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        mContext.startActivity(browserIntent);
    }

    // Up navigation
    public void navigateUp(Activity activity)
    {
        Intent navigateUpIntent = NavUtils.getParentActivityIntent(activity);

        if(NavUtils.shouldUpRecreateTask(activity, navigateUpIntent) || activity.isTaskRoot())
        {
            TaskStackBuilder.create(mContext).addNextIntentWithParentStack(navigateUpIntent).startActivities();
        }
        else
        {
            NavUtils.navigateUpFromSameTask(activity);
        }
    }

    // Saved articles
    public void saveArticle(String title, String uri, String webview)
    {
        String domain = uri.replaceAll("https?://", "").replaceAll("[w]{3}\\.", "").replaceAll("/.*", "");

        ContentValues savedArticlesContentValues = new ContentValues();
        savedArticlesContentValues.put(SavedArticlesSQLiteHelper.COLUMN_TITLE, title);
        savedArticlesContentValues.put(SavedArticlesSQLiteHelper.COLUMN_DOMAIN, domain);
        savedArticlesContentValues.put(SavedArticlesSQLiteHelper.COLUMN_URI, uri);
        savedArticlesContentValues.put(SavedArticlesSQLiteHelper.COLUMN_WEBVIEW, webview);

        SQLiteDatabase savedArticlesSqLiteDatabase = new SavedArticlesSQLiteHelper(mContext).getWritableDatabase();

        savedArticlesSqLiteDatabase.insert(SavedArticlesSQLiteHelper.TABLE, null, savedArticlesContentValues);

        savedArticlesSqLiteDatabase.close();

        showToast(mContext.getString(R.string.saved_articles_article_saved), 0);
    }

    // Pharmacies
    public boolean pharmacyAddressIsPostBox(String pharmacyAddress)
    {
        return (pharmacyAddress.startsWith("Boks") || pharmacyAddress.startsWith("Pb.") || pharmacyAddress.startsWith("Postboks") || pharmacyAddress.startsWith("Serviceboks"));
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