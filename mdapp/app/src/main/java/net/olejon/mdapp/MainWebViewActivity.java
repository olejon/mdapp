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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class MainWebViewActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private InputMethodManager mInputMethodManager;

    private MenuItem goForwardMenuItem;
    private LinearLayout mToolbarSearchLayout;
    private EditText mToolbarSearchEditText;
    private TextView mToolbarSearchCountTextView;
    private ProgressBar mProgressBar;
    private WebView mWebView;

    private String pageTitle;
    private String pageUri;

    private boolean mWebViewAnimationHasBeenShown = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected())
        {
            mTools.showToast(getString(R.string.device_not_connected), 1);

            finish();

            return;
        }

        // Input manager
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

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

        mToolbarSearchLayout = (LinearLayout) findViewById(R.id.main_webview_toolbar_search_layout);
        mToolbarSearchEditText = (EditText) findViewById(R.id.main_webview_toolbar_search);
        mToolbarSearchCountTextView = (TextView) findViewById(R.id.main_webview_toolbar_search_count_textview);

        mToolbarSearchEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {
                String find = mToolbarSearchEditText.getText().toString().trim();

                if(find.equals(""))
                {
                    mToolbarSearchCountTextView.setVisibility(View.GONE);

                    mWebView.clearMatches();
                }
                else
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        mWebView.findAllAsync(find);
                    }
                    else
                    {
                        //noinspection deprecation
                        mWebView.findAll(find);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        mToolbarSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_SEARCH || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);
                    return true;
                }

                return false;
            }
        });

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.main_webview_toolbar_progressbar_horizontal);

        // Web view
        mWebView = (WebView) findViewById(R.id.main_webview_content);

        final WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        if(pageUri.contains("brukerhandboken.no"))
        {
            webSettings.setUseWideViewPort(true);
            webSettings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:41.0) Gecko/20100101 Firefox/41.0");
        }
        else if(pageUri.contains("interaksjoner.no"))
        {
            webSettings.setDefaultTextEncodingName("iso-8859-15");
        }

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
                else if(url.startsWith("mailto:"))
                {
                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(Intent.createChooser(intent, getString(R.string.project_feedback_text)));
                    return true;
                }
                else if(url.startsWith("tel:"))
                {
                    try
                    {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                        startActivity(intent);
                    }
                    catch(Exception e)
                    {
                        new MaterialDialog.Builder(mContext).title(getString(R.string.device_not_supported_dialog_title)).content(getString(R.string.device_not_supported_dialog_message)).positiveText(getString(R.string.device_not_supported_dialog_positive_button)).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
                    }

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
                        if(pageUri.contains("brukerhandboken.no"))
                        {
                            mWebView.loadUrl("javascript:if($('div#nonedit_title').length) { var offset = $('div#nonedit_title').offset(); window.scrollTo(0, offset.top - 8); }");
                        }
                        else if(pageUri.contains("helsedirektoratet.no"))
                        {
                            mWebView.loadUrl("javascript:if($('span.dropdown').length) { var offset = $('span.dropdown').offset(); window.scrollTo(0, offset.top); } else if($('.publication_information').length) { var offset = $('.publication_information').offset(); window.scrollTo(0, offset.top); }");
                        }
                    }
                    else
                    {
                        mWebViewAnimationHasBeenShown = true;

                        if(pageUri.contains("brukerhandboken.no"))
                        {
                            mWebView.loadUrl("javascript:$('div#FirstSearch1 > input:text').focus();");
                        }
                        else if(pageUri.contains("helsebiblioteket.no"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('h1').offset(); window.scrollTo(0, offset.top - 8);");
                        }
                        else if(pageUri.contains("helsedirektoratet.no"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('div.searchfield').offset(); window.scrollTo(0, offset.top + 48);");
                        }
                        else if(pageUri.contains("helsenorge.no"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('h1#sidetittel').offset(); window.scrollTo(0, offset.top);");
                        }
                        else if(pageUri.contains("icd10data.com"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('div.contentBlurb:contains(\"Clinical Information\")').offset(); window.scrollTo(0, offset.top - 8);");
                        }
                        else if(pageUri.contains("lvh.no"))
                        {
                            mWebView.loadUrl("javascript:var offset = $('div#article').offset(); window.scrollTo(0, offset.top);");
                        }

                        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fade_in);

                        mWebView.startAnimation(animation);
                        mWebView.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);

                    mToolbarSearchLayout.setVisibility(View.GONE);
                    mToolbarSearchEditText.setText("");
                }
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            mWebView.setFindListener(new WebView.FindListener()
            {
                @Override
                public void onFindResultReceived(int i, int i2, boolean b)
                {
                    if(i2 == 0)
                    {
                        mToolbarSearchCountTextView.setVisibility(View.GONE);

                        mTools.showToast(getString(R.string.main_webview_find_in_text_no_results), 1);
                    }
                    else
                    {
                        int active = i + 1;

                        mToolbarSearchCountTextView.setText(active+"/"+i2);
                        mToolbarSearchCountTextView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }

        CookieManager cookieManager = CookieManager.getInstance();

        cookieManager.setCookie("http://bestpractice.bmj.com/", "BMJ-cookie-policy=close");
        cookieManager.setCookie("https://helsenorge.no/", "mh-unsupportedbar=");
        cookieManager.setCookie("http://tidsskriftet.no/", "osevencookiepromptclosed=1");
        cookieManager.setCookie("http://www.gulesider.no/", "cookiesAccepted=true");
        cookieManager.setCookie("http://www.helsebiblioteket.no/", "whycookie-visited=1");

        if(pageUri.contains("brukerhandboken.no")) mWebView.setInitialScale(100);

        if(savedInstanceState == null)
        {
            mWebView.loadUrl(pageUri);
        }
        else
        {
            mWebView.restoreState(savedInstanceState);
        }

        // Tip dialog
        if(pageUri.equals("http://m.legemiddelhandboka.no/") || pageUri.equals("http://brukerhandboken.no/") || pageUri.equals("http://www.uptodate.com/contents/search") || pageUri.equals("http://bestpractice.bmj.com/"))
        {
            if(!mTools.getSharedPreferencesBoolean("MAIN_WEBVIEW_HIDE_TIP_DIALOG"))
            {
                new MaterialDialog.Builder(mContext).title(getString(R.string.main_webview_tip_dialog_title)).content(getString(R.string.main_webview_tip_dialog_message)).positiveText(getString(R.string.main_webview_tip_dialog_positive_button)).onPositive(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                    {
                        mTools.setSharedPreferencesBoolean("MAIN_WEBVIEW_HIDE_TIP_DIALOG", true);
                    }
                }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
            }
        }
        else if(pageUri.equals("http://legehandboka.no/"))
        {
            if(!mTools.getSharedPreferencesBoolean("MAIN_WEBVIEW_NEL_DIALOG"))
            {
                new MaterialDialog.Builder(mContext).title(getString(R.string.main_webview_nel_dialog_title)).content(getString(R.string.main_webview_nel_dialog_message)).positiveText(getString(R.string.main_webview_nel_dialog_positive_button)).onPositive(new MaterialDialog.SingleButtonCallback()
                {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                    {
                        mTools.setSharedPreferencesBoolean("MAIN_WEBVIEW_NEL_DIALOG", true);
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
    @Override
    protected void onPause()
    {
        super.onPause();

        mToolbarSearchLayout.setVisibility(View.GONE);
        mToolbarSearchEditText.setText("");

        mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);

        mWebView.pauseTimers();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            //noinspection deprecation
            CookieSyncManager.getInstance().sync();
        }
    }

    // Save activity
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        mWebView.saveState(outState);
    }

    // Back button
    @Override
    public void onBackPressed()
    {
        if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
        {
            mToolbarSearchLayout.setVisibility(View.GONE);
            mToolbarSearchEditText.setText("");

            mWebView.clearMatches();
        }
        else if(mWebView.canGoBack())
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
            case R.id.main_webview_menu_find_in_text:
            {
                if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
                {
                    mWebView.findNext(true);

                    mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);
                }
                else
                {
                    mToolbarSearchLayout.setVisibility(View.VISIBLE);
                    mToolbarSearchEditText.requestFocus();

                    mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
                }

                if(!mTools.getSharedPreferencesBoolean("WEBVIEW_FIND_IN_TEXT_HIDE_TIP_DIALOG"))
                {
                    new MaterialDialog.Builder(mContext).title(getString(R.string.main_webview_find_in_text_tip_dialog_title)).content(getString(R.string.main_webview_find_in_text_tip_dialog_message)).positiveText(getString(R.string.main_webview_find_in_text_tip_dialog_positive_button)).onPositive(new MaterialDialog.SingleButtonCallback()
                    {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                        {
                            mTools.setSharedPreferencesBoolean("WEBVIEW_FIND_IN_TEXT_HIDE_TIP_DIALOG", true);
                        }
                    }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
                }

                return true;
            }
            case R.id.main_webview_menu_print:
            {
                mTools.printDocument(mWebView, pageTitle);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}