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

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class Icd10WebViewActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private MenuItem goForwardMenuItem;
    private ProgressBar mProgressBar;
    private ProgressBar mHorizontalProgressBar;
    private WebView mWebView;

    private String pageSearchUri;

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
        setContentView(R.layout.activity_icd10_webview);

        // Intent
        Intent intent = getIntent();

        final String pageTitle = intent.getStringExtra("title");
        final String pageUri = intent.getStringExtra("uri");

        pageSearchUri = pageUri;

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.icd10_webview_toolbar);
        toolbar.setTitle(getString(R.string.icd10_webview_title)+": "+pageTitle);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.icd10_webview_toolbar_progressbar);
        mHorizontalProgressBar = (ProgressBar) findViewById(R.id.icd10_webview_toolbar_progressbar_horizontal);

        mProgressBar.setVisibility(View.VISIBLE);

        // Web view
        mWebView = (WebView) findViewById(R.id.icd10_webview_content);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        mWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
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
                    mHorizontalProgressBar.setVisibility(View.INVISIBLE);

                    if(mWebView.canGoForward())
                    {
                        goForwardMenuItem.setVisible(true);
                    }
                    else
                    {
                        goForwardMenuItem.setVisible(false);
                    }

                    if(!mWebViewAnimationHasBeenShown)
                    {
                        mWebViewAnimationHasBeenShown = true;

                        mWebView.loadUrl("javascript:var offset = $('div.codeDetail').offset(); window.scrollTo(0, offset.top);");

                        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                        mWebView.startAnimation(animation);

                        mWebView.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    mHorizontalProgressBar.setVisibility(View.VISIBLE);
                    mHorizontalProgressBar.setProgress(newProgress);
                }
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website)+"api/1/icd-10/search/?uri="+pageUri, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    mProgressBar.setVisibility(View.GONE);

                    pageSearchUri = response.getString("uri");

                    mWebView.loadUrl(pageSearchUri);
                }
                catch(Exception e)
                {
                    mProgressBar.setVisibility(View.GONE);

                    mTools.showToast(getString(R.string.icd10_webview_could_not_find_code), 1);

                    Log.e("Icd10WebViewActivity", Log.getStackTraceString(e));

                    finish();
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                mProgressBar.setVisibility(View.GONE);

                mTools.showToast(getString(R.string.icd10_webview_could_not_find_code), 1);

                Log.e("Icd10WebViewActivity", error.toString());

                finish();
            }
        });

        requestQueue.add(jsonObjectRequest);
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
        getMenuInflater().inflate(R.menu.menu_icd10_webview, menu);

        goForwardMenuItem = menu.findItem(R.id.icd10_webview_menu_go_forward);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                finish();
                return true;
            }
            case R.id.icd10_webview_menu_go_forward:
            {
                mWebView.goForward();
                return true;
            }
            case R.id.icd10_webview_menu_uri:
            {
                mTools.openUri(pageSearchUri);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
