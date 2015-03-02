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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.URLEncoder;

public class NasjonaleRetningslinjerWebViewActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private Toolbar mToolbar;
    private MenuItem goForwardMenuItem;
    private ProgressBar mProgressBar;
    private WebView mWebView;

    private String pageUri;

    private boolean mWebViewAnimationHasBeenShown = false;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected())
        {
            mTools.showToast(getString(R.string.device_not_connected), 1);

            finish();

            return;
        }

        // Layout
        setContentView(R.layout.activity_nasjonale_retningslinjer_webview);

        // Intent
        Intent intent = getIntent();

        final String pageSearch = intent.getStringExtra("search");

        pageUri = intent.getStringExtra("uri");

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.nasjonale_retningslinjer_webview_toolbar);
        mToolbar.setTitle(getString(R.string.nasjonale_retningslinjer_webview_search)+": \""+pageSearch+"\"");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.nasjonale_retningslinjer_webview_toolbar_progressbar_horizontal);

        // Web view
        mWebView = (WebView) findViewById(R.id.nasjonale_retningslinjer_webview_content);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        mWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(url.matches("^https?://.*?\\.pdf$"))
                {
                    String fileTitle = view.getTitle();

                    mTools.showToast(getString(R.string.nasjonale_retningslinjer_webview_downloading_pdf), 1);

                    mTools.downloadFile(fileTitle, url);

                    return true;
                }

                return false;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                if(newProgress == 100)
                {
                    mProgressBar.setVisibility(View.INVISIBLE);

                    if(mWebView.canGoForward())
                    {
                        goForwardMenuItem.setVisible(true);
                    }
                    else
                    {
                        goForwardMenuItem.setVisible(false);
                    }

                    if(mWebViewAnimationHasBeenShown)
                    {
                        mWebView.loadUrl("javascript:var offset = $('span.refinesearch').offset(); window.scrollTo(0, offset.top);");
                    }
                    else
                    {
                        mWebViewAnimationHasBeenShown = true;

                        mWebView.loadUrl("javascript:var offset = $('div.searchfield').offset(); window.scrollTo(0, offset.top + 48);");

                        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                        mWebView.startAnimation(animation);

                        mWebView.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
                }
            }
        });

        mWebView.loadUrl(pageUri);

        // Correct
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        try
        {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website_uri)+"api/1/correct/?search="+URLEncoder.encode(pageSearch, "utf-8"), null, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response)
                {
                    try
                    {
                        final String correctSearchString = response.getString("correct");

                        if(!correctSearchString.equals(""))
                        {
                            new MaterialDialog.Builder(mContext).title(getString(R.string.correct_dialog_title)).content(Html.fromHtml(getString(R.string.correct_dialog_message)+":<br><br><b>"+correctSearchString+"</b>")).positiveText(getString(R.string.correct_dialog_positive_button)).negativeText(getString(R.string.correct_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                            {
                                @Override
                                public void onPositive(MaterialDialog dialog)
                                {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(NasjonaleRetningslinjerSQLiteHelper.COLUMN_STRING, correctSearchString);

                                    SQLiteDatabase sqLiteDatabase = new NasjonaleRetningslinjerSQLiteHelper(mContext).getWritableDatabase();

                                    sqLiteDatabase.delete(NasjonaleRetningslinjerSQLiteHelper.TABLE, NasjonaleRetningslinjerSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(pageSearch)+" COLLATE NOCASE", null);
                                    sqLiteDatabase.insert(NasjonaleRetningslinjerSQLiteHelper.TABLE, null, contentValues);

                                    sqLiteDatabase.close();

                                    mToolbar.setTitle(getString(R.string.nasjonale_retningslinjer_webview_search)+": \""+correctSearchString+"\"");

                                    mProgressBar.setVisibility(View.VISIBLE);

                                    try
                                    {
                                        mWebView.loadUrl("https://helsedirektoratet.no/retningslinjer#Default=%7B%22k%22%3A%22"+URLEncoder.encode(correctSearchString.toLowerCase(), "utf-8")+"%22%2C%22r%22%3A%5B%7B%22n%22%3A%22HDDocumentType%22%2C%22t%22%3A%5B%22%5C%22%C7%82%C7%824e61736a6f6e616c65206661676c696765207265746e696e67736c696e6a6572%5C%22%22%2C%22equals(%5C%22Nasjonale%20faglige%20retningslinjer%5C%22)%22%5D%2C%22o%22%3A%22and%22%2C%22k%22%3Afalse%2C%22m%22%3Anull%7D%5D%7D");
                                    }
                                    catch(Exception e)
                                    {
                                        Log.e("NasjonaleRetningslinjer", Log.getStackTraceString(e));
                                    }
                                }
                            }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e("NasjonaleRetningslinjer", Log.getStackTraceString(e));
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("NasjonaleRetningslinjer", error.toString());
                }
            });

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsonObjectRequest);
        }
        catch(Exception e)
        {
            Log.e("NasjonaleRetningslinjer", Log.getStackTraceString(e));
        }
    }

    // Resume activity
    @Override
    protected void onResume()
    {
        super.onResume();

        mWebView.resumeTimers();
    }

    // Pause activity
    @Override
    protected void onPause()
    {
        super.onPause();

        mWebView.pauseTimers();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) CookieSyncManager.getInstance().sync();
    }

    // Back button
    @Override
    public void onBackPressed()
    {
        if(mWebView.canGoBack())
        {
            mWebView.goBack();
        }
        else
        {
            super.onBackPressed();
        }
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_nasjonale_retningslinjer_webview, menu);

        goForwardMenuItem = menu.findItem(R.id.nasjonale_retningslinjer_webview_menu_go_forward);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.nasjonale_retningslinjer_webview_menu_go_forward:
            {
                mWebView.goForward();
                return true;
            }
            case R.id.nasjonale_retningslinjer_webview_menu_uri:
            {
                mTools.openUri(pageUri);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
