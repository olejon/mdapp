package net.olejon.mdapp;

/*

Copyright 2017 Ole Jon Bjørkum

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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;

public class MedicationFelleskatalogenFragment extends Fragment
{
    private WebView mWebView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_medication_felleskatalogen, container, false);

        // Activity
        Activity activity = getActivity();

        // Context
        final Context context = activity.getApplicationContext();

        // Tools
        final MyTools mTools = new MyTools(context);

        // Arguments
        String pageUri = getArguments().getString("uri");

        // Progress bar
        final ProgressBar progressBar = (ProgressBar) viewGroup.findViewById(R.id.medication_felleskatalogen_progressbar);

        // Toolbar
        final LinearLayout toolbarSearchLayout = (LinearLayout) activity.findViewById(R.id.medication_toolbar_search_layout);
        final EditText toolbarSearchEditText = (EditText) activity.findViewById(R.id.medication_toolbar_search);

        // SSL error button
        final Button sslErrorButton = (Button) viewGroup.findViewById(R.id.medication_felleskatalogen_ssl_error_button);

        // Web view
        mWebView = (WebView) viewGroup.findViewById(R.id.medication_felleskatalogen_content);

        WebSettings webSettings = mWebView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.12; rv:54.0) Gecko/20100101 Firefox/54.0");

        mWebView.setWebViewClient(new WebViewClient()
        {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(!mTools.isDeviceConnected())
                {
                    mTools.showToast(getString(R.string.device_not_connected), 0);
                    return true;
                }
                else if(url.matches("^https?://play\\.google\\.com/.*") || url.matches("^https?://itunes\\.apple\\.com/.*"))
                {
                    mTools.showToast(getString(R.string.device_not_supported), 1);
                    return true;
                }
                else if(url.matches("^https?://.*?\\.pdf$") || url.matches("^https?://.*?\\.docx?$") || url.matches("^https?://.*?\\.xlsx?$") || url.matches("^https?://.*?\\.pptx?$"))
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
                        new MaterialDialog.Builder(context).title(R.string.device_not_supported_dialog_title).content(getString(R.string.device_not_supported_dialog_message)).positiveText(R.string.device_not_supported_dialog_positive_button).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
                    }

                    return true;
                }

                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                progressBar.setVisibility(View.VISIBLE);

                toolbarSearchLayout.setVisibility(View.GONE);
                toolbarSearchEditText.setText("");
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                progressBar.setVisibility(View.GONE);

                if(mTools.isTablet()) mWebView.loadUrl("javascript:if($('div#base_content').length) { $('div#base_content').removeClass('base_center_content'); }");
            }

            @Override
            public void onReceivedSslError(WebView view, @NonNull SslErrorHandler handler, SslError error)
            {
                handler.cancel();

                mWebView.stopLoading();

                mWebView.setVisibility(View.GONE);

                progressBar.setVisibility(View.GONE);

                sslErrorButton.setVisibility(View.VISIBLE);
            }
        });

        if(savedInstanceState == null)
        {
            mWebView.loadUrl(pageUri);
        }
        else
        {
            mWebView.restoreState(savedInstanceState);
        }

        return viewGroup;
    }

    // Resume fragment
    @Override
    public void onResume()
    {
        super.onResume();

        mWebView.resumeTimers();
    }

    // Pause fragment
    @Override
    public void onPause()
    {
        super.onPause();

        mWebView.pauseTimers();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            //noinspection deprecation
            CookieSyncManager.getInstance().sync();
        }
    }

    // Save fragment
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        mWebView.saveState(outState);
    }
}