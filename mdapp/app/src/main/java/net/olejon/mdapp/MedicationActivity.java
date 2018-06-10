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

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.net.URLEncoder;

public class MedicationActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private InputMethodManager mInputMethodManager;

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	private RelativeLayout mRelativeLayout;
	private Toolbar mToolbar;
	private MenuItem mFavoriteMenuItem;
	private MenuItem mAtcCodeMenuItem;
	private EditText mToolbarSearchEditText;
	private ViewPager mViewPager;
	private WebView mWebView;
	private WebView mFelleskatalogenWebView;
	private WebView mNlhWebView;

	private String medicationPrescriptionGroup;
	private String medicationName;
	private String medicationSubstance;
	private String medicationManufacturer;
	private String medicationAtcCode;

	private int mViewPagerPosition = 0;

	private long medicationId;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Intent
		Intent intent = getIntent();

		medicationId = intent.getLongExtra("id", 0);

		if(medicationId == 0)
		{
			mTools.showToast(getString(R.string.medication_could_not_find_medication), 1);

			finish();

			return;
		}

		// Input manager
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_medication);

		// View
		mRelativeLayout = findViewById(R.id.medication_inner_layout);

		// Toolbar
		mToolbar = findViewById(R.id.medication_toolbar);
		mToolbar.setTitle("");

		setSupportActionBar(mToolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarSearchEditText = findViewById(R.id.medication_toolbar_search);

		// View pager
		mViewPager = findViewById(R.id.medication_pager);
	}

	// Pause activity
	@Override
	protected void onPause()
	{
		super.onPause();

		mToolbarSearchEditText.setVisibility(View.GONE);
		mToolbarSearchEditText.setText("");

		mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);
	}

	// Destroy activity
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if(mCursor != null && !mCursor.isClosed()) mCursor.close();
		if(mSqLiteDatabase != null && mSqLiteDatabase.isOpen()) mSqLiteDatabase.close();
	}

	// Back button
	@Override
	public void onBackPressed()
	{
		if(mToolbarSearchEditText.getVisibility() == View.VISIBLE)
		{
			mToolbarSearchEditText.setVisibility(View.GONE);
			mToolbarSearchEditText.setText("");

			mWebView.clearMatches();
		}
		else
		{
			if(mViewPagerPosition == 0)
			{
				if(mFelleskatalogenWebView.canGoBack())
				{
					mFelleskatalogenWebView.goBack();
				}
				else
				{
					super.onBackPressed();
				}
			}

			if(mViewPagerPosition == 1)
			{
				if(mNlhWebView.canGoBack())
				{
					mNlhWebView.goBack();
				}
				else
				{
					super.onBackPressed();
				}
			}
		}
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_medication, menu);

		mFavoriteMenuItem = menu.findItem(R.id.medication_menu_favorite);
		mAtcCodeMenuItem = menu.findItem(R.id.medication_menu_atc);

		getMedication();

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
			{
				mTools.navigateUp(this);
				return true;
			}
			case R.id.medication_menu_favorite:
			{
				addRemoveFavorite();
				return true;
			}
			case R.id.medication_menu_find_in_text:
			{
				if(mToolbarSearchEditText.getVisibility() == View.VISIBLE)
				{
					mWebView.findNext(true);

					mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);
				}
				else
				{
					mToolbarSearchEditText.setVisibility(View.VISIBLE);
					mToolbarSearchEditText.requestFocus();

					mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
				}

				if(!mTools.getSharedPreferencesBoolean("MEDICATION_WEBVIEW_FIND_IN_TEXT_HIDE_INFORMATION_DIALOG"))
				{
					new MaterialDialog.Builder(mContext).title(R.string.medication_webview_find_in_text_information_dialog_title).content(getString(R.string.medication_webview_find_in_text_information_dialog_message)).positiveText(R.string.medication_webview_find_in_text_information_dialog_positive_button).onPositive(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
						{
							mTools.setSharedPreferencesBoolean("MEDICATION_WEBVIEW_FIND_IN_TEXT_HIDE_INFORMATION_DIALOG", true);
						}
					}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
				}

				return true;
			}
			case R.id.medication_menu_interactions:
			{
				Intent intent = new Intent(mContext, InteractionsActivity.class);
				intent.putExtra("search", medicationName);
				startActivity(intent);
				return true;
			}
			case R.id.medication_menu_poisonings:
			{
				Intent intent = new Intent(mContext, PoisoningsCardsActivity.class);
				intent.putExtra("search", medicationName);
				startActivity(intent);
				return true;
			}
			case R.id.medication_menu_atc:
			{
				Intent intent = new Intent(mContext, AtcCodesActivity.class);
				intent.putExtra("code", medicationAtcCode);
				startActivity(intent);
				return true;
			}
			case R.id.medication_menu_note:
			{
				if(mTools.getSharedPreferencesString("NOTES_PIN_CODE").equals(""))
				{
					new MaterialDialog.Builder(mContext).title(R.string.medication_note_dialog_title).content(getString(R.string.medication_note_dialog_message)).positiveText(R.string.medication_note_dialog_positive_button).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
				}
				else
				{
					Intent intent = new Intent(mContext, NotesEditActivity.class);
					intent.putExtra("title", medicationName);
					startActivity(intent);
				}

				return true;
			}
			case R.id.medication_menu_substance:
			{
				getSubstance();

				return true;
			}
			case R.id.medication_menu_manufacturer:
			{
				getManufacturer();

				return true;
			}
			case R.id.medication_menu_slv:
			{
				try
				{
					Intent intent = new Intent(mContext, MainWebViewActivity.class);
					intent.putExtra("title", getString(R.string.medication_menu_slv));
					intent.putExtra("uri", "https://www.legemiddelsok.no/sider/default.aspx?searchquery="+URLEncoder.encode(medicationName, "utf-8"));
					startActivity(intent);
				}
				catch(Exception e)
				{
					Log.e("MedicationActivity", Log.getStackTraceString(e));
				}

				return true;
			}
			case R.id.medication_menu_print:
			{
				if(mViewPagerPosition == 0)
				{
					mTools.printDocument(mFelleskatalogenWebView, medicationName);
				}
				else
				{
					mTools.printDocument(mNlhWebView, medicationName);
				}

				return true;
			}
			case R.id.medication_menu_open_uri:
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

	// Get URI
	private String getUri(String uri)
	{
		String searchString = "";

		try
		{
			searchString = URLEncoder.encode(medicationName.replaceAll(" .*", ""), "utf-8");
		}
		catch(Exception e)
		{
			Log.e("MedicationActivity", Log.getStackTraceString(e));
		}

		return (uri.equals("felleskatalogen")) ? "https://www.felleskatalogen.no/ir/medisin/sok?sokord="+searchString : "http://m.legemiddelhandboka.no/s%C3%B8keresultat/?q="+searchString;
	}

	// Favorite
	private boolean isFavorite()
	{
		SQLiteDatabase sqLiteDatabase = new MedicationsFavoritesSQLiteHelper(mContext).getReadableDatabase();
		String[] queryColumns = {MedicationsFavoritesSQLiteHelper.COLUMN_ID};
		Cursor cursor = sqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, queryColumns, MedicationsFavoritesSQLiteHelper.COLUMN_NAME+" = "+mTools.sqe(medicationName), null, null, null, null);

		int count = cursor.getCount();

		cursor.close();
		sqLiteDatabase.close();

		return (count != 0);
	}

	private void addRemoveFavorite()
	{
		SQLiteDatabase sqLiteDatabase = new MedicationsFavoritesSQLiteHelper(mContext).getWritableDatabase();

		String snackbarString;

		if(isFavorite())
		{
			sqLiteDatabase.delete(MedicationsFavoritesSQLiteHelper.TABLE, MedicationsFavoritesSQLiteHelper.COLUMN_NAME+" = "+mTools.sqe(medicationName), null);

			mFavoriteMenuItem.setIcon(R.drawable.ic_star_outline_white_24dp).setTitle(getString(R.string.medication_menu_add_favorite));

			snackbarString = getString(R.string.medication_favorite_removed);
		}
		else
		{
			ContentValues contentValues = new ContentValues();
			contentValues.put(MedicationsFavoritesSQLiteHelper.COLUMN_PRESCRIPTION_GROUP, medicationPrescriptionGroup);
			contentValues.put(MedicationsFavoritesSQLiteHelper.COLUMN_NAME, medicationName);
			contentValues.put(MedicationsFavoritesSQLiteHelper.COLUMN_SUBSTANCE, medicationSubstance);
			contentValues.put(MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER, medicationManufacturer);

			sqLiteDatabase.insert(MedicationsFavoritesSQLiteHelper.TABLE, null, contentValues);

			mFavoriteMenuItem.setIcon(R.drawable.ic_star_white_24dp).setTitle(getString(R.string.medication_menu_remove_favorite));

			snackbarString = getString(R.string.medication_favorite_saved);
		}

		Snackbar snackbar = Snackbar.make(mRelativeLayout, snackbarString, Snackbar.LENGTH_LONG).setAction(R.string.snackbar_undo, new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				addRemoveFavorite();
			}
		}).setActionTextColor(ContextCompat.getColor(mContext, R.color.light_teal));

		TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
		textView.setTextColor(ContextCompat.getColor(mContext, R.color.white));

		snackbar.show();

		sqLiteDatabase.close();
	}

	// View pager
	private class ViewPagerAdapter extends FragmentPagerAdapter
	{
		final String[] pages = getResources().getStringArray(R.array.medication_pages);

		ViewPagerAdapter(FragmentManager fragmentManager)
		{
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int position)
		{
			Fragment felleskatalogenFragment = new MedicationFelleskatalogenFragment();
			Bundle felleskatalogenBundle = new Bundle();
			felleskatalogenBundle.putString("uri", getUri("felleskatalogen"));
			felleskatalogenFragment.setArguments(felleskatalogenBundle);

			Fragment nlhFragment = new MedicationNlhFragment();
			Bundle nlhBundle = new Bundle();
			nlhBundle.putString("uri", getUri("nlh"));
			nlhFragment.setArguments(nlhBundle);

			return (position == 0) ? felleskatalogenFragment : nlhFragment;
		}

		@Override
		public int getCount()
		{
			return pages.length;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return pages[position];
		}
	}

	private class ViewPagerTransformer implements ViewPager.PageTransformer
	{
		public void transformPage(@NonNull View view, float position)
		{
			int pageWidth = view.getWidth();
			int pageHeight = view.getHeight();

			if(position <= 1)
			{
				float scaleFactor = Math.max(0.88f, 1 - Math.abs(position));
				float verticalMargin = pageHeight * (1 - scaleFactor) / 2;
				float horzizontalMargin = pageWidth * (1 - scaleFactor) / 2;

				if(position < 0)
				{
					view.setTranslationX(horzizontalMargin - verticalMargin / 2);
				}
				else
				{
					view.setTranslationX(- horzizontalMargin + verticalMargin / 2);
				}

				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);
			}
		}
	}

	// SSL error dialog
	private void showSslErrorDialog(final String uri)
	{
		new MaterialDialog.Builder(mContext).title(R.string.device_not_supported_dialog_title).content(getString(R.string.device_not_supported_dialog_ssl_error_message)).positiveText(R.string.device_not_supported_dialog_positive_button).neutralText(R.string.device_not_supported_dialog_neutral_button).onNeutral(new MaterialDialog.SingleButtonCallback()
		{
			@Override
			public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction which)
			{
				materialDialog.dismiss();

				mTools.openChromeCustomTabsUri(uri);
			}
		}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.dark_blue).show();
	}

	// Get medication
	private void getMedication()
	{
		// Database
		mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
		String[] queryColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME, SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE, SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER, SlDataSQLiteHelper.MEDICATIONS_COLUMN_ATC_CODE};
		mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, queryColumns, SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID+" = "+medicationId, null, null, null, null);

		if(mCursor.moveToFirst())
		{
			// Information
			medicationPrescriptionGroup = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP));
			medicationName = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME));
			medicationSubstance = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE));
			medicationManufacturer = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER));
			medicationAtcCode = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_ATC_CODE));

			// Toolbar
			mToolbar.setTitle(medicationName);

			// Favorite?
			if(isFavorite()) mFavoriteMenuItem.setIcon(R.drawable.ic_star_white_24dp).setTitle(getString(R.string.medication_menu_remove_favorite));

			// ATC
			mAtcCodeMenuItem.setTitle(getString(R.string.medication_menu_atc, medicationAtcCode));

			// Prescription group
			Button prescriptionGroupButton = findViewById(R.id.medication_prescription_group);
			prescriptionGroupButton.setText(medicationPrescriptionGroup);

			switch(medicationPrescriptionGroup)
			{
				case "A":
				{
					prescriptionGroupButton.setBackgroundResource(R.drawable.medication_prescription_group_red);
					break;
				}
				case "B":
				{
					prescriptionGroupButton.setBackgroundResource(R.drawable.medication_prescription_group_orange);
					break;
				}
			}

			prescriptionGroupButton.setVisibility(View.VISIBLE);

			// Substance
			TextView substanceTextView = findViewById(R.id.medication_substance);
			substanceTextView.setText(medicationSubstance);

			// Manufacturer
			TextView manufacturerTextView = findViewById(R.id.medication_manufacturer);
			manufacturerTextView.setText(medicationManufacturer);

			// Connected?
			if(mTools.isDeviceConnected())
			{
				// Firebase
				FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(mContext);

				Bundle bundle = new Bundle();
				bundle.putString(FirebaseAnalytics.Param.ITEM_ID, String.valueOf(medicationId));
				bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, medicationName);
				bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "medication");

				firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

				// Toolbar
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

				// View pager
				PagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

				mViewPager.setAdapter(pagerAdapter);
				mViewPager.setOffscreenPageLimit(2);
				mViewPager.setPageTransformer(true, new ViewPagerTransformer());

				mFelleskatalogenWebView = findViewById(R.id.medication_felleskatalogen_content);
				mNlhWebView = findViewById(R.id.medication_nlh_content);

				mWebView = mFelleskatalogenWebView;

				// Tabs
				TabLayout tabLayout = findViewById(R.id.medication_tabs);
				tabLayout.setupWithViewPager(mViewPager);

				tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
				{
					@Override
					public void onTabSelected(TabLayout.Tab tab)
					{
						mViewPagerPosition = tab.getPosition();

						mToolbarSearchEditText.setVisibility(View.GONE);
						mToolbarSearchEditText.setText("");

						mWebView.clearMatches();

						mWebView = (mViewPagerPosition == 0) ? mFelleskatalogenWebView : mNlhWebView;

						mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);
					}

					@Override
					public void onTabUnselected(TabLayout.Tab tab) { }

					@Override
					public void onTabReselected(TabLayout.Tab tab) { }
				});

				// Prescription group
				prescriptionGroupButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						switch(medicationPrescriptionGroup)
						{
							case "A":
							{
								mTools.showToast(getString(R.string.medication_prescription_group_a), 1);
								break;
							}
							case "B":
							{
								mTools.showToast(getString(R.string.medication_prescription_group_b), 1);
								break;
							}
							case "C":
							{
								mTools.showToast(getString(R.string.medication_prescription_group_c), 1);
								break;
							}
							case "F":
							{
								mTools.showToast(getString(R.string.medication_prescription_group_f), 1);
								break;
							}
						}
					}
				});

				// Substance
				substanceTextView.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						getSubstance();
					}
				});

				// Manufacturer
				manufacturerTextView.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						getManufacturer();
					}
				});

				// SSL error
				Button felleskatalogenSslErrorButton = findViewById(R.id.medication_felleskatalogen_ssl_error_button);
				Button nlhSslErrorButton = findViewById(R.id.medication_nlh_ssl_error_button);

				felleskatalogenSslErrorButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						showSslErrorDialog(getUri("felleskatalogen"));
					}
				});

				nlhSslErrorButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						showSslErrorDialog(getUri("nlh"));
					}
				});

				// Information dialog
				if(!mTools.getSharedPreferencesBoolean("MEDICATION_HIDE_INFORMATION_DIALOG"))
				{
					new MaterialDialog.Builder(mContext).title(R.string.medication_information_dialog_title).content(getString(R.string.medication_information_dialog_message)).positiveText(R.string.medication_information_dialog_positive_button).onPositive(new MaterialDialog.SingleButtonCallback()
					{
						@Override
						public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
						{
							mTools.setSharedPreferencesBoolean("MEDICATION_HIDE_INFORMATION_DIALOG", true);
						}
					}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
				}
			}
			else
			{
				// Information dialog
				new MaterialDialog.Builder(mContext).title(R.string.medication_not_connected_dialog_title).content(getString(R.string.medication_not_connected_dialog_message)).positiveText(R.string.medication_not_connected_dialog_positive_button).negativeText(R.string.medication_not_connected_dialog_negative_button).onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
					{
						getMedication();
					}
				}).onNegative(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
					{
						finish();
					}
				}).cancelListener(new DialogInterface.OnCancelListener()
				{
					@Override
					public void onCancel(DialogInterface dialogInterface)
					{
						finish();
					}
				}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.dark_blue).show();
			}
		}
	}

	// Get substance
	private void getSubstance()
	{
		String[] queryColumns = {SlDataSQLiteHelper.SUBSTANCES_COLUMN_ID};
		Cursor cursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_SUBSTANCES, queryColumns, SlDataSQLiteHelper.SUBSTANCES_COLUMN_NAME+" = "+mTools.sqe(medicationSubstance), null, null, null, null);

		if(cursor.moveToFirst())
		{
			long id = cursor.getLong(cursor.getColumnIndexOrThrow(SlDataSQLiteHelper.SUBSTANCES_COLUMN_ID));

			Intent intent = new Intent(mContext, SubstanceActivity.class);
			intent.putExtra("id", id);
			startActivity(intent);
		}

		cursor.close();
	}

	// Get manufacturer
	private void getManufacturer()
	{
		String[] queryColumns = {SlDataSQLiteHelper.MANUFACTURERS_COLUMN_ID};
		Cursor cursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MANUFACTURERS, queryColumns, SlDataSQLiteHelper.MANUFACTURERS_COLUMN_NAME+" = "+mTools.sqe(medicationManufacturer), null, null, null, null);

		if(cursor.moveToFirst())
		{
			long id = cursor.getLong(cursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MANUFACTURERS_COLUMN_ID));

			Intent intent = new Intent(mContext, ManufacturerActivity.class);
			intent.putExtra("id", id);
			startActivity(intent);
		}

		cursor.close();
	}
}