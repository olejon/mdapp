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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;

public class MainWebViewActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private MenuItem goForwardMenuItem;
    private ProgressBar mProgressBar;
    private WebView mWebView;

    private String pageTitle;
    private String pageUri;

    private boolean mWebViewAnimationHasBeenShown = false;

    @SuppressLint("SetJavaScriptEnabled")
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
        setContentView(R.layout.activity_main_webview);

        // Intent
        final Intent intent = getIntent();

        pageTitle = intent.getStringExtra("title");
        pageUri = intent.getStringExtra("uri");

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.main_webview_toolbar);
        toolbar.setTitle(pageTitle);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.main_webview_toolbar_progressbar_horizontal);

        // Web view
        mWebView = (WebView) findViewById(R.id.main_webview_content);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        mWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(url.matches(".*/[^#]+#[^/]+$"))
                {
                    mWebView.loadUrl(url.replaceAll("#[^/]+$", ""));
                    return true;
                }
                else if(url.matches("^https?://.*?\\.pdf$"))
                {
                    mTools.downloadFile(view.getTitle(), url);
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

                    if(!mWebViewAnimationHasBeenShown)
                    {
                        mWebViewAnimationHasBeenShown = true;

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

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setCookie("http://bestpractice.bmj.com/", "BMJ-cookie-policy=close");

        mWebView.loadUrl(pageUri);

        // Tip dialog
        if(pageUri.equals("http://www.uptodate.com/contents/search") || pageUri.equals("http://bestpractice.bmj.com/"))
        {
            boolean hideTipDialog = mTools.getSharedPreferencesBoolean("MAIN_WEBVIEW_HIDE_TIP_DIALOG");

            if(!hideTipDialog)
            {
                new MaterialDialog.Builder(mContext).title("Tips").content("Merk at du vil få treff fra både UpToDate og BMJ Best Practice dersom du søker på engelsk i seksjonen Sykdommer og behandlinger.").positiveText("OK").callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        mTools.setSharedPreferencesBoolean("MAIN_WEBVIEW_HIDE_TIP_DIALOG", true);
                    }
                }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
            }
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
    @SuppressWarnings("deprecation")
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
        getMenuInflater().inflate(R.menu.menu_main_webview, menu);

        goForwardMenuItem = menu.findItem(R.id.main_webview_menu_go_forward);

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
            case R.id.main_webview_menu_go_forward:
            {
                mWebView.goForward();
                return true;
            }
            case R.id.main_webview_menu_print:
            {
                mTools.printDocument(mWebView, pageTitle);
                return true;
            }
            case R.id.main_webview_menu_uri:
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
