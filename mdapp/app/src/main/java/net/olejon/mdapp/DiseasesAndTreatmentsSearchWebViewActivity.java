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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class DiseasesAndTreatmentsSearchWebViewActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private InputMethodManager mInputMethodManager;

	private MenuItem findInTextMenuItem;
	private MenuItem goForwardMenuItem;
	private EditText mToolbarSearchEditText;
	private WebView mWebView;

	private String pageTitle;

	private boolean mWebViewHasBeenLoaded = false;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Transition
		overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

		// Connected?
		if(!mTools.isDeviceConnected())
		{
			mTools.showToast(getString(R.string.device_not_connected), 1);

			finish();

			return;
		}

		// Intent
		Intent intent = getIntent();

		pageTitle = intent.getStringExtra("title");

		final String pageUri = intent.getStringExtra("uri");
		final String pageSearch = intent.getStringExtra("search");

		// Input manager
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_diseases_and_treatments_search_webview);

		// Toolbar
		final Toolbar toolbar = findViewById(R.id.diseases_and_treatments_search_webview_toolbar);
		toolbar.setTitle(pageTitle);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarSearchEditText = findViewById(R.id.diseases_and_treatments_search_webview_toolbar_search);

		mToolbarSearchEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{
				String find = mToolbarSearchEditText.getText().toString().trim();

				if(find.equals(""))
				{
					mWebView.clearMatches();
				}
				else
				{
					mWebView.findAllAsync(find);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

			@Override
			public void afterTextChanged(Editable editable) { }
		});

		// Progress bar
		final ProgressBar progressBar = findViewById(R.id.diseases_and_treatments_search_webview_toolbar_progressbar_horizontal);

		// Web view
		mWebView = findViewById(R.id.diseases_and_treatments_search_webview_content);

		WebSettings webSettings = mWebView.getSettings();

		webSettings.setJavaScriptEnabled(true);
		webSettings.setAppCacheEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setAppCachePath(getCacheDir().getAbsolutePath());
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setDisplayZoomControls(false);
		webSettings.setSavePassword(false);

		if(pageUri.contains("aofoundation.org"))
		{
			webSettings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:61.0) Gecko/20100101 Firefox/61.0");
		}
		else if(pageUri.contains("felleskatalogen.no"))
		{
			webSettings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:61.0) Gecko/20100101 Firefox/61.0");
		}
		else if(pageUri.contains("interaksjoner.azurewebsites.net"))
		{
			webSettings.setLoadWithOverviewMode(true);
			webSettings.setUseWideViewPort(true);
			webSettings.setUserAgentString("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:61.0) Gecko/20100101 Firefox/61.0");
			webSettings.setDefaultTextEncodingName("iso-8859-15");
		}
		else if(pageUri.contains("legemiddelsok.no"))
		{
			webSettings.setLoadWithOverviewMode(true);
			webSettings.setUseWideViewPort(true);
		}
		else if(pageUri.contains("oncolex.no"))
		{
			webSettings.setLoadWithOverviewMode(true);
			webSettings.setUseWideViewPort(true);
		}
		else if(pageUri.contains("webofknowledge.com"))
		{
			webSettings.setLoadWithOverviewMode(true);
			webSettings.setUseWideViewPort(true);

			mTools.showToast(getString(R.string.device_this_can_take_some_time), 1);
		}

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
				else if(url.matches(".*/[^#]+#[^/]+$"))
				{
					mWebView.loadUrl(url.replaceAll("#[^/]+$", ""));
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
				if(mWebViewHasBeenLoaded)
				{
					findInTextMenuItem.setTitle(getString(R.string.diseases_and_treatments_search_webview_menu_find_in_text));
				}

				progressBar.setVisibility(View.VISIBLE);

				mToolbarSearchEditText.setVisibility(View.GONE);
				mToolbarSearchEditText.setText("");

				if(mInputMethodManager != null) mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);
			}

			@Override
			public void onPageFinished(WebView view, String url)
			{
				String toolbarTitle = (pageUri.contains("antibiotikaiallmennpraksis.no") || pageUri.contains("brukerhandboken.no")) ? pageTitle : mWebView.getTitle();

				toolbar.setTitle(toolbarTitle);

				progressBar.setVisibility(View.INVISIBLE);

				if(mWebView.canGoForward())
				{
					goForwardMenuItem.setVisible(true);
				}
				else
				{
					goForwardMenuItem.setVisible(false);
				}

				if(!mWebViewHasBeenLoaded)
				{
					mWebViewHasBeenLoaded = true;

					if(pageUri.contains("helsenorge.no"))
					{
						mWebView.loadUrl("javascript:var offset = $('h1#sidetittel').offset(); window.scrollTo(0, offset.top - 8);");
					}
					else if(pageUri.contains("lvh.no"))
					{
						mWebView.loadUrl("javascript:var offset = $('div#article').offset(); window.scrollTo(0, offset.top);");
					}
				}

				if(pageUri.contains("antibiotikaiallmennpraksis.no"))
				{
					mWebView.loadUrl("javascript:var element = $('div.footer'); element.hide();");
				}
				else if(pageUri.contains("brukerhandboken.no"))
				{
					mWebView.loadUrl("javascript:var element = $('div.footer'); element.hide();");
				}
				else if(pageUri.contains("webofknowledge.com"))
				{
					mWebView.loadUrl("javascript:if($('input:text.NEWun-pw').length) { $('input:text.NEWun-pw').val(''); $('input:password.NEWun-pw').val(''); $('input:checkbox.NEWun-pw').prop('checked', true); $('form[name=\"roaming\"]').submit(); } else if($('td.NEWwokErrorContainer > p a').length) { window.location.replace($('td.NEWwokErrorContainer > p a').first().attr('href')); } else if($('div.search-criteria input:text.search-criteria-input').length) { $('div.search-criteria input:text.search-criteria-input').val('"+pageSearch+"'); $('form#WOS_GeneralSearch_input_form').submit(); }");
				}
			}
		});

		mWebView.setWebChromeClient(new WebChromeClient()
		{
			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result)
			{
				if(pageUri.contains("webofknowledge.com"))
				{
					result.confirm();
					return true;
				}

				return false;
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress)
			{
				progressBar.setProgress(newProgress);
				progressBar.setSecondaryProgress(newProgress + 10);
			}
		});

		CookieManager cookieManager = CookieManager.getInstance();

		cookieManager.setCookie("https://bestpractice.bmj.com/", "cookieconsent_status=dismiss");
		cookieManager.setCookie("http://legemiddelhandboka.no/", "osevencookiepromptclosed=1");
		cookieManager.setCookie("https://nhi.no/", "user-category=professional");
		cookieManager.setCookie("https://www.uptodate.com/", "cookie-accept=t");

		if(savedInstanceState == null)
		{
			mWebView.loadUrl(pageUri);
		}
		else
		{
			mWebView.restoreState(savedInstanceState);
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

		findInTextMenuItem.setTitle(getString(R.string.diseases_and_treatments_search_webview_menu_find_in_text));

		mToolbarSearchEditText.setVisibility(View.GONE);
		mToolbarSearchEditText.setText("");

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
		if(mToolbarSearchEditText.getVisibility() == View.VISIBLE)
		{
			findInTextMenuItem.setTitle(getString(R.string.diseases_and_treatments_search_webview_menu_find_in_text));

			mToolbarSearchEditText.setVisibility(View.GONE);
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

			overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
		}
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_diseases_and_treatments_search_webview, menu);

		findInTextMenuItem = menu.findItem(R.id.diseases_and_treatments_search_webview_menu_find_in_text);
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
			case R.id.diseases_and_treatments_search_webview_menu_find_in_text:
			{
				if(mToolbarSearchEditText.getVisibility() == View.VISIBLE)
				{
					mWebView.findNext(true);

					if(mInputMethodManager != null) mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);
				}
				else
				{
					findInTextMenuItem.setTitle(getString(R.string.diseases_and_treatments_search_webview_menu_find_in_text_next));

					mToolbarSearchEditText.setVisibility(View.VISIBLE);
					mToolbarSearchEditText.requestFocus();

					if(mInputMethodManager != null) mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
				}

				if(!mTools.getSharedPreferencesBoolean("WEBVIEW_FIND_IN_TEXT_HIDE_INFORMATION_DIALOG"))
				{
					new MaterialDialog.Builder(mContext).title(R.string.main_webview_find_in_text_information_dialog_title).content(getString(R.string.main_webview_find_in_text_information_dialog_message)).positiveText(R.string.main_webview_find_in_text_information_dialog_positive_button).onPositive(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
						{
							mTools.setSharedPreferencesBoolean("WEBVIEW_FIND_IN_TEXT_HIDE_INFORMATION_DIALOG", true);
						}
					}).titleColorRes(R.color.teal).contentColorRes(R.color.dark).positiveColorRes(R.color.teal).negativeColorRes(R.color.dark).neutralColorRes(R.color.teal).buttonRippleColorRes(R.color.light_grey).show();
				}

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
			case R.id.diseases_and_treatments_search_webview_menu_save_article:
			{
				mTools.saveArticle(mWebView.getTitle(), mWebView.getUrl(), "diseases_and_treatments");
				return true;
			}
			case R.id.diseases_and_treatments_search_webview_menu_reload:
			{
				mWebView.reload();
				return true;
			}
			case R.id.diseases_and_treatments_search_webview_menu_open_uri:
			{
				mTools.openChromeCustomTabsUri(mWebView.getUrl());
				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}
}