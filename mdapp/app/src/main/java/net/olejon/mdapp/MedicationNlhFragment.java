package net.olejon.mdapp;

/*

Copyright 2018 Ole Jon Bjørkum

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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;

public class MedicationNlhFragment extends Fragment
{
	private Context mContext;

	private EditText mToolbarSearchEditText;
	private WebView mWebView;

	private String mPageUri;

	// Create fragment view
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_medication_nlh, container, false);

		// Activity
		Activity activity = getActivity();

		// Context
		if(activity != null) mContext = activity.getApplicationContext();

		// Tools
		final MyTools mTools = new MyTools(mContext);

		// Arguments
		if(getArguments() != null) mPageUri = getArguments().getString("uri");

		// Progress bar
		final ProgressBar progressBar = viewGroup.findViewById(R.id.medication_nlh_progressbar);

		// Toolbar
		if(activity != null) mToolbarSearchEditText = activity.findViewById(R.id.medication_toolbar_search);

		// Web view
		mWebView = viewGroup.findViewById(R.id.medication_nlh_content);

		WebSettings webSettings = mWebView.getSettings();

		webSettings.setJavaScriptEnabled(true);
		webSettings.setAppCacheEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setAppCachePath(mContext.getCacheDir().getAbsolutePath());
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		webSettings.setBuiltInZoomControls(false);
		webSettings.setDisplayZoomControls(false);

		mWebView.setWebViewClient(new WebViewClient()
		{
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
						new MaterialDialog.Builder(mContext).title(R.string.device_not_supported_dialog_title).content(getString(R.string.device_not_supported_dialog_message)).positiveText(R.string.device_not_supported_dialog_positive_button).titleColorRes(R.color.teal).contentColorRes(R.color.dark).positiveColorRes(R.color.teal).negativeColorRes(R.color.dark).neutralColorRes(R.color.teal).buttonRippleColorRes(R.color.light_grey).show();
					}

					return true;
				}

				return false;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				progressBar.setVisibility(View.VISIBLE);

				mToolbarSearchEditText.setVisibility(View.GONE);
				mToolbarSearchEditText.setText("");
			}

			@Override
			public void onPageFinished(WebView view, String url)
			{
				progressBar.setVisibility(View.GONE);
			}
		});

		if(savedInstanceState == null)
		{
			mWebView.loadUrl(mPageUri);
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

		//noinspection deprecation
		CookieSyncManager.getInstance().sync();
	}

	// Save fragment
	@Override
	public void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);

		mWebView.saveState(outState);
	}
}