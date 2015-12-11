package net.olejon.mdapp;

/*

Copyright 2015 Ole Jon Bjørkum

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

public class MedicationFelleskatalogenFragment extends Fragment
{
    public static WebView WEBVIEW;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_medication_felleskatalogen, container, false);

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
        final ProgressBar progressBar = (ProgressBar) viewGroup.findViewById(R.id.medication_felleskatalogen_progressbar);

        // Toolbar
        final LinearLayout toolbarSearchLayout = (LinearLayout) activity.findViewById(R.id.medication_toolbar_search_layout);
        final EditText toolbarSearchEditText = (EditText) activity.findViewById(R.id.medication_toolbar_search);

        // Web view
        WEBVIEW = (WebView) viewGroup.findViewById(R.id.medication_felleskatalogen_content);

        WebSettings webSettings = WEBVIEW.getSettings();
        webSettings.setJavaScriptEnabled(true);

        WEBVIEW.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                if(!mTools.isDeviceConnected())
                {
                    mTools.showToast(getString(R.string.device_not_connected), 0);
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
                if(newProgress > 32)
                {
                    progressBar.setVisibility(View.GONE);
                }
                else
                {
                    progressBar.setVisibility(View.VISIBLE);

                    toolbarSearchLayout.setVisibility(View.GONE);
                    toolbarSearchEditText.setText("");
                }

                if(mTools.isTablet() && newProgress == 100) WEBVIEW.loadUrl("javascript:if($('div#base_content').length) { $('div#base_content').removeClass('base_center_content'); }");
            }
        });

        if(savedInstanceState == null)
        {
            WEBVIEW.loadUrl(pageUri);
        }
        else
        {
            WEBVIEW.restoreState(savedInstanceState);
        }

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
    @Override
    public void onPause()
    {
        super.onPause();

        WEBVIEW.pauseTimers();

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

        WEBVIEW.saveState(outState);
    }
}