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
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class MedicationNlhFragment extends Fragment
{
    public static WebView WEBVIEW;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_medication_nlh, container, false);

        // Activity
        final Activity activity = getActivity();

        // Context
        final Context context = activity.getApplicationContext();

        // Tools
        final MyTools mTools = new MyTools(context);

        // Arguments
        Bundle bundle = getArguments();

        final String pageUri = bundle.getString("uri");

        // Progress bar
        final ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.medication_toolbar_progressbar_horizontal);

        // Web view
        WEBVIEW = (WebView) viewGroup.findViewById(R.id.medication_nlh_content);

        WebSettings webSettings = WEBVIEW.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCachePath(context.getCacheDir().getAbsolutePath());
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        WEBVIEW.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(url.matches(".*/[^#]+#[^/]+$"))
                {
                    WEBVIEW.loadUrl(url.replaceAll("#[^/]+$", ""));
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

        WEBVIEW.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                if(newProgress == 100)
                {
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else
                {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(newProgress);
                }
            }
        });

        WEBVIEW.loadUrl(pageUri);

        return viewGroup;
    }

    // Resume fragment
    @Override
    public void onResume()
    {
        super.onResume();

        WEBVIEW.resumeTimers();
    }

    // Pause fragment
    @SuppressWarnings("deprecation")
    @Override
    public void onPause()
    {
        super.onPause();

        WEBVIEW.pauseTimers();

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) CookieSyncManager.getInstance().sync();
    }
}