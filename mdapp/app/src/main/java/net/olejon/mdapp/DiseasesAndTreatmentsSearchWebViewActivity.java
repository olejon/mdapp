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

public class DiseasesAndTreatmentsSearchWebViewActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private MenuItem goForwardMenuItem;
    private ProgressBar mProgressBar;
    private WebView mWebView;

    private String pageTitle;
    private String pageUri;
    private String mSearch;

    private boolean mWebViewAnimationHasBeenShown = false;

    // Create activity
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

        // Transition
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

        // Intent
        final Intent intent = getIntent();

        pageTitle = intent.getStringExtra("title");
        pageUri = intent.getStringExtra("uri");
        mSearch = intent.getStringExtra("search");

        // Layout
        setContentView(R.layout.activity_diseases_and_treatments_search_webview);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.diseases_and_treatments_search_webview_toolbar);
        toolbar.setTitle(pageTitle);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.diseases_and_treatments_search_webview_toolbar_progressbar_horizontal);

        // Web view
        mWebView = (WebView) findViewById(R.id.diseases_and_treatments_search_webview_content);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        if(pageUri.contains("webofknowledge") || pageUri.contains("brukerhandboken")) webSettings.setUseWideViewPort(true);

        if(pageUri.contains("brukerhandboken")) webSettings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:35.0) Gecko/20100101 Firefox/35.0");

        if(pageUri.contains("webofknowledge")) mTools.showToast(getString(R.string.diseases_and_treatments_search_webview_this_can_take_some_time), 1);

        mWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(url.matches("^https?://.*?\\.pdf$"))
                {
                    mTools.downloadFile(pageTitle, url);
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

                        if(pageUri.contains("uptodate"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('h2').offset(); window.scrollTo(0, offset.top);");
                        }
                        else if(pageUri.contains("bmj"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('small.monograph-title').offset(); window.scrollTo(0, offset.top);");
                        }
                        else if(pageUri.contains("nhi"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('h1').offset(); window.scrollTo(0, offset.top);");
                        }
                        else if(pageUri.contains("sml"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('article.sml_search_result').offset(); window.scrollTo(0, offset.top);");
                        }
                        else if(pageUri.contains("forskning"))
                        {
                            mWebView.loadUrl("javascript:var elements = document.getElementsByTagName('span'); elements[0].scrollIntoView();");
                        }
                        else if(pageUri.contains("helsebiblioteket"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('h1').offset(); window.scrollTo(0, offset.top);");
                        }
                        else if(pageUri.contains("helsenorge"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('h1#sidetittel').offset(); window.scrollTo(0, offset.top);");
                        }
                        else if(pageUri.contains("brukerhandboken"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('p#emnetittel').offset(); window.scrollTo(0, offset.top);");
                        }

                        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);
                        mWebView.startAnimation(animation);

                        mWebView.setVisibility(View.VISIBLE);
                    }

                    if(pageUri.contains("webofknowledge")) mWebView.loadUrl("javascript:if($('input:text.NEWun-pw').length) { $('input:text.NEWun-pw').val('legeappen@olejon.net'); $('input:password.NEWun-pw').val('!cDr4ft23WJq0hIfmEnsJH3vaEGddEAT'); $('input:checkbox.NEWun-pw').prop('checked', true); $('form[name=\"roaming\"]').submit(); } else if($('td.NEWwokErrorContainer > p a').length) { window.location.replace($('td.NEWwokErrorContainer > p a').first().attr('href')); } else { $('div.search-criteria input:text.search-criteria-input').val('"+mSearch+"'); $('form#UA_GeneralSearch_input_form').submit(); }");
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
        cookieManager.setCookie("http://nhi.no/", "userCategory=professional");
        cookieManager.setCookie("http://www.helsebiblioteket.no/", "whycookie-visited=1");
        cookieManager.setCookie("http://tidsskriftet.no/", "osevencookiepromptclosed=1");
        cookieManager.setCookie("https://helsenorge.no/", "mh-unsupportedbar=");

        if(pageUri.contains("webofknowledge") || pageUri.contains("brukerhandboken")) mWebView.setInitialScale(100);

        mWebView.loadUrl(pageUri);
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
        if(mWebView.canGoBack() && !pageUri.contains("webofknowledge"))
        {
            mWebView.goBack();
        }
        else
        {
            super.onBackPressed();

            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
        }
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_diseases_and_treatments_search_webview, menu);

        goForwardMenuItem = menu.findItem(R.id.diseases_and_treatments_search_webview_menu_go_forward);

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
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                return true;
            }
            case R.id.diseases_and_treatments_search_webview_menu_go_forward:
            {
                mWebView.goForward();
                return true;
            }
            case R.id.diseases_and_treatments_search_webview_menu_print:
            {
                mTools.printDocument(mWebView, pageTitle);
                return true;
            }
            case R.id.diseases_and_treatments_search_webview_menu_uri:
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
