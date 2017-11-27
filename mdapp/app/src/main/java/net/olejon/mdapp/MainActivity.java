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
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity
{
	public static SQLiteDatabase SL_DATA_SQLITE_DATABASE;

	public static int VIEW_PAGER_POSITION = 0;

	private final Activity mActivity = this;

	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private InputMethodManager mInputMethodManager;

	private NavigationView mDrawer;
	private DrawerLayout mDrawerLayout;
	private EditText mSearchEditText;
	private ViewPager mViewPager;
	private TabLayout mTabLayout;
	private FloatingActionButton mFloatingActionButton;

	private TextView mHelsebiblioteketTextView;
	private TextView mTidsskriftetTextView;
	private TextView mOncolexTextView;
	private TextView mBrukerhandbokenTextView;
	private TextView mAnalyseoversiktenTextView;
	private TextView mNhiTextView;
	private TextView mSmlTextView;
	private TextView mWikipediaNorwegianTextView;
	private TextView mForskningTextView;
	private TextView mHelsenorgeTextView;
	private TextView mUpToDateTextView;
	private TextView mBmjTextView;
	private TextView mPubmedTextView;
	private TextView mWebofscienceTextView;
	private TextView mMedlineplusTextView;
	private TextView mAoSurgeryTextView;
	private TextView mWikipediaEnglishTextView;
	private TextView mEncyclopediasTextView;

	private int mDrawerClosed;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Settings
		PreferenceManager.setDefaultValues(mContext, R.xml.settings, false);

		// Installed
		long installed = mTools.getSharedPreferencesLong("INSTALLED_3600");

		if(installed == 0) mTools.setSharedPreferencesLong("INSTALLED_3600", mTools.getCurrentTime());

		// Input manager
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_main);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.main_toolbar);
		toolbar.setTitle(getString(R.string.main_title));

		setSupportActionBar(toolbar);

		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

		// Search
		mSearchEditText = findViewById(R.id.main_search_edittext);

		// Drawer
		mDrawer = findViewById(R.id.main_drawer);
		mDrawerLayout = findViewById(R.id.main_drawer_layout);

		mHelsebiblioteketTextView = findViewById(R.id.drawer_item_helsebiblioteket);
		mTidsskriftetTextView = findViewById(R.id.drawer_item_tidsskriftet);
		mOncolexTextView = findViewById(R.id.drawer_item_oncolex);
		mBrukerhandbokenTextView = findViewById(R.id.drawer_item_brukerhandboken);
		mAnalyseoversiktenTextView = findViewById(R.id.drawer_item_analyseoversikten);
		mNhiTextView = findViewById(R.id.drawer_item_nhi);
		mSmlTextView = findViewById(R.id.drawer_item_sml);
		mWikipediaNorwegianTextView = findViewById(R.id.drawer_item_wikipedia_norwegian);
		mForskningTextView = findViewById(R.id.drawer_item_forskning);
		mHelsenorgeTextView = findViewById(R.id.drawer_item_helsenorge);
		mUpToDateTextView = findViewById(R.id.drawer_item_uptodate);
		mBmjTextView = findViewById(R.id.drawer_item_bmj);
		mPubmedTextView = findViewById(R.id.drawer_item_pubmed);
		mWebofscienceTextView = findViewById(R.id.drawer_item_webofscience);
		mMedlineplusTextView = findViewById(R.id.drawer_item_medlineplus);
		mAoSurgeryTextView = findViewById(R.id.drawer_item_ao_surgery);
		mWikipediaEnglishTextView = findViewById(R.id.drawer_item_wikipedia_english);
		mEncyclopediasTextView = findViewById(R.id.drawer_item_encyclopedias);

		TextView drawerVersionNameTextView = findViewById(R.id.drawer_version_name);
		TextView drawerVersionCodeTextView = findViewById(R.id.drawer_version_code);

		drawerVersionNameTextView.setText(getString(R.string.drawer_version_name, mTools.getProjectVersionName()));
		drawerVersionCodeTextView.setText(getString(R.string.drawer_version_code, mTools.getProjectVersionCode()));

		int drawerContentDescription = R.string.drawer_content_description;

		mDrawerLayout.addDrawerListener(new ActionBarDrawerToggle(mActivity, mDrawerLayout, toolbar, drawerContentDescription, drawerContentDescription)
		{
			@Override
			public void onDrawerOpened(View drawerView)
			{
				mDrawerClosed = 0;
			}

			@Override
			public void onDrawerClosed(View drawerView)
			{
				mHelsebiblioteketTextView.setVisibility(View.GONE);
				mTidsskriftetTextView.setVisibility(View.GONE);
				mOncolexTextView.setVisibility(View.GONE);
				mBrukerhandbokenTextView.setVisibility(View.GONE);
				mAnalyseoversiktenTextView.setVisibility(View.GONE);
				mNhiTextView.setVisibility(View.GONE);
				mSmlTextView.setVisibility(View.GONE);
				mWikipediaNorwegianTextView.setVisibility(View.GONE);
				mForskningTextView.setVisibility(View.GONE);
				mHelsenorgeTextView.setVisibility(View.GONE);
				mUpToDateTextView.setVisibility(View.GONE);
				mBmjTextView.setVisibility(View.GONE);
				mPubmedTextView.setVisibility(View.GONE);
				mWebofscienceTextView.setVisibility(View.GONE);
				mMedlineplusTextView.setVisibility(View.GONE);
				mAoSurgeryTextView.setVisibility(View.GONE);
				mWikipediaEnglishTextView.setVisibility(View.GONE);

				mEncyclopediasTextView.setVisibility(View.VISIBLE);

				switch(mDrawerClosed)
				{
					case R.id.drawer_item_saved_articles:
					{
						Intent intent = new Intent(mContext, SavedArticlesActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_felleskatalogen:
					{
						String uri = (mTools.isTablet()) ? "https://www.felleskatalogen.no/medisin/" : "https://www.felleskatalogen.no/m/medisin/";

						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_felleskatalogen));
						intent.putExtra("uri", uri);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_nlh:
					{
						String uri = (mTools.isTablet()) ? "http://legemiddelhandboka.no/" : "http://m.legemiddelhandboka.no/";

						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_nlh));
						intent.putExtra("uri", uri);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_nel:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_nel));
						intent.putExtra("uri", "https://legehandboka.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_helsebiblioteket:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_helsebiblioteket));
						intent.putExtra("uri", "http://www.helsebiblioteket.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_tidsskriftet:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_tidsskriftet));
						intent.putExtra("uri", "http://tidsskriftet.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_oncolex:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_oncolex));
						intent.putExtra("uri", "http://oncolex.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_brukerhandboken:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_brukerhandboken));
						intent.putExtra("uri", "http://brukerhandboken.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_analyseoversikten:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_analyseoversikten));
						intent.putExtra("uri", "http://analyseoversikten.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_nhi:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_nhi));
						intent.putExtra("uri", "https://nhi.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_sml:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_sml));
						intent.putExtra("uri", "https://sml.snl.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_wikipedia_norwegian:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_wikipedia_norwegian));
						intent.putExtra("uri", "https://no.wikipedia.org/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_forskning:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_forskning));
						intent.putExtra("uri", "http://forskning.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_helsenorge:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_helsenorge));
						intent.putExtra("uri", "https://helsenorge.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_uptodate:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_uptodate));
						intent.putExtra("uri", "https://www.uptodate.com/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_bmj:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_bmj));
						intent.putExtra("uri", "http://bestpractice.bmj.com/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_pubmed:
					{
						String uri = (mTools.isTablet()) ? "https://www.ncbi.nlm.nih.gov/pubmed/" : "https://www.ncbi.nlm.nih.gov/m/pubmed/";

						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_pubmed));
						intent.putExtra("uri", uri);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_webofscience:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_webofscience));
						intent.putExtra("uri", "https://webofknowledge.com/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_medlineplus:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_medlineplus));
						intent.putExtra("uri", "https://medlineplus.gov/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_ao_surgery:
					{
						String uri = (mTools.isTablet()) ? "https://www2.aofoundation.org/wps/portal/surgery" : "https://www2.aofoundation.org/wps/portal/surgerymobile/";

						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_ao_surgery));
						intent.putExtra("uri", uri);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_wikipedia_english:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_wikipedia_english));
						intent.putExtra("uri", "https://en.wikipedia.org/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_diseases_and_treatments:
					{
						Intent intent = new Intent(mContext, DiseasesAndTreatmentsActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_lvh:
					{
						Intent intent = new Intent(mContext, LvhActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_interactions:
					{
						Intent intent = new Intent(mContext, InteractionsActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_poisonings:
					{
						Intent intent = new Intent(mContext, PoisoningsActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_antibiotics_guides:
					{
						Intent intent = new Intent(mContext, AntibioticsGuidesActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_nasjonale_retningslinjer:
					{
						Intent intent = new Intent(mContext, NasjonaleRetningslinjerActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_clinicaltrials:
					{
						Intent intent = new Intent(mContext, ClinicalTrialsActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_atc:
					{
						Intent intent = new Intent(mContext, AtcActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_icd10:
					{
						Intent intent = new Intent(mContext, Icd10Activity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_manufacturers:
					{
						Intent intent = new Intent(mContext, ManufacturersActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_pharmacies:
					{
						Intent intent = new Intent(mContext, PharmaciesActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_scales:
					{
						Intent intent = new Intent(mContext, ScalesActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_notifications_from_slv:
					{
						Intent intent = new Intent(mContext, NotificationsFromSlvActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_calculators:
					{
						Intent intent = new Intent(mContext, CalculatorsActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_notes:
					{
						Intent intent = new Intent(mContext, NotesActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_tasks:
					{
						Intent intent = new Intent(mContext, TasksActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_settings:
					{
						Intent intent = new Intent(mContext, SettingsActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_donate:
					{
						Intent intent = new Intent(mContext, DonateActivity.class);
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_feedback:
					{
						Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(getString(R.string.project_feedback_uri, getString(R.string.project_name))));
						startActivity(Intent.createChooser(intent, getString(R.string.project_feedback_text)));
						break;
					}
					case R.id.drawer_item_report_issue:
					{
						mTools.openChromeCustomTabsUri(getString(R.string.project_report_issue_uri));
						break;
					}
				}
			}
		});

		// View pager
		mViewPager = findViewById(R.id.main_pager);

		// Tab layout
		mTabLayout = findViewById(R.id.main_tabs);

		// Floating action button
		mFloatingActionButton = findViewById(R.id.main_fab);

		mFloatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mSearchEditText.requestFocus();

				mInputMethodManager.showSoftInput(mSearchEditText, 0);
			}
		});

		// Welcome
		if(!mTools.getSharedPreferencesBoolean("WELCOME_ACTIVITY_HAS_BEEN_SHOWN"))
		{
			Handler welcomeActivityHandler = new Handler();

			welcomeActivityHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					Intent intent = new Intent(mContext, WelcomeActivity.class);
					startActivity(intent);
				}
			}, 500);
		}

		// Get data
		if(mTools.getSharedPreferencesBoolean(SlDataSQLiteHelper.DB_CREATED))
		{
			getSlData();
		}
		else
		{
			Thread getSlDataThread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					SQLiteDatabase slDataSqLiteDatabase = new SlDataSQLiteHelper(mContext).getWritableDatabase();

					try
					{
						Log.w("LOG", getString(R.string.main_decompressing_new_database));

						String dbName = SlDataSQLiteHelper.DB_NAME;
						int dbVersion = SlDataSQLiteHelper.DB_VERSION;

						File file = getDatabasePath(SlDataSQLiteHelper.DB_NAME);

						InputStream inputStream = mContext.getAssets().open(SlDataSQLiteHelper.DB_NAME);
						OutputStream outputStream = new FileOutputStream(file);

						byte[] buffer = new byte[1024];
						int length;

						while((length = inputStream.read(buffer)) > 0)
						{
							outputStream.write(buffer, 0, length);
						}

						outputStream.flush();
						outputStream.close();
						inputStream.close();

						Log.w("LOG", getString(R.string.main_new_database_decompressed, dbName, dbVersion));
					}
					catch(Exception e)
					{
						Log.e("MainActivity", Log.getStackTraceString(e));
					}

					slDataSqLiteDatabase.close();

					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							getSlData();
						}
					});
				}
			});

			getSlDataThread.start();
		}
	}

	// Resume activity
	@Override
	protected void onResume()
	{
		super.onResume();

		NotificationManagerCompat.from(mContext).cancel(MyFirebaseMessagingService.NOTIFICATION_MESSAGE_ID);

		// Floating action button
		mFloatingActionButton.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fab));
		mFloatingActionButton.setVisibility(View.VISIBLE);

		// Rate
		if(!mTools.getSharedPreferencesBoolean("MAIN_HIDE_RATE_DIALOG_3600"))
		{
			long currentTime = mTools.getCurrentTime();
			long installedTime = mTools.getSharedPreferencesLong("INSTALLED_3600");

			if(currentTime - installedTime > 1000 * 3600 * 48)
			{
				mTools.setSharedPreferencesBoolean("MAIN_HIDE_RATE_DIALOG_3600", true);

				new MaterialDialog.Builder(mContext).title(R.string.main_rate_dialog_title).content(getString(R.string.main_rate_dialog_message)).positiveText(R.string.main_rate_dialog_positive_button).negativeText(R.string.main_rate_dialog_negative_button).onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
					{
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.olejon.mdapp"));
						startActivity(intent);
					}
				}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
			}
		}

		// Donate
		if(!mTools.getSharedPreferencesBoolean("MAIN_HIDE_DONATE_DIALOG_3600"))
		{
			long currentTime = mTools.getCurrentTime();
			long installedTime = mTools.getSharedPreferencesLong("INSTALLED_3600");

			if(currentTime - installedTime > 1000 * 3600 * 96)
			{
				mTools.setSharedPreferencesBoolean("MAIN_HIDE_DONATE_DIALOG_3600", true);

				new MaterialDialog.Builder(mContext).title(R.string.main_donate_dialog_title).content(getString(R.string.main_donate_dialog_message)).positiveText(R.string.main_donate_dialog_positive_button).negativeText(R.string.main_donate_dialog_negative_button).onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
					{
						Intent intent = new Intent(mContext, DonateActivity.class);
						startActivity(intent);
					}
				}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
			}
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if(SL_DATA_SQLITE_DATABASE != null && SL_DATA_SQLITE_DATABASE.isOpen()) SL_DATA_SQLITE_DATABASE.close();
	}

	// Back button
	@Override
	public void onBackPressed()
	{
		if(mDrawerLayout.isDrawerOpen(mDrawer))
		{
			mDrawerLayout.closeDrawers();
		}
		else if(!mSearchEditText.getText().toString().equals(""))
		{
			mSearchEditText.setText("");
		}
		else if(VIEW_PAGER_POSITION != 0)
		{
			mViewPager.setCurrentItem(0);
		}
		else
		{
			super.onBackPressed();
		}
	}

	// Search button
	@Override
	public boolean onKeyUp(int keyCode, @NonNull KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_SEARCH)
		{
			mSearchEditText.requestFocus();

			mInputMethodManager.showSoftInput(mSearchEditText, 0);

			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.main_menu_scan_barcode:
			{
				Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.main_menu_donate:
			{
				Intent intent = new Intent(mContext, DonateActivity.class);
				startActivity(intent);
				return true;
			}
			case R.id.main_menu_settings:
			{
				Intent intent = new Intent(mContext, SettingsActivity.class);
				startActivity(intent);
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	// Drawer
	public void onDrawerItemClick(View view)
	{
		mDrawerClosed = view.getId();

		if(mDrawerClosed == R.id.drawer_item_encyclopedias)
		{
			mEncyclopediasTextView.setVisibility(View.GONE);

			Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.drawer_item);

			mHelsebiblioteketTextView.startAnimation(animation);
			mTidsskriftetTextView.startAnimation(animation);
			mOncolexTextView.startAnimation(animation);
			mBrukerhandbokenTextView.startAnimation(animation);
			mAnalyseoversiktenTextView.startAnimation(animation);
			mNhiTextView.startAnimation(animation);
			mSmlTextView.startAnimation(animation);
			mWikipediaNorwegianTextView.startAnimation(animation);
			mForskningTextView.startAnimation(animation);
			mHelsenorgeTextView.startAnimation(animation);
			mUpToDateTextView.startAnimation(animation);
			mBmjTextView.startAnimation(animation);
			mPubmedTextView.startAnimation(animation);
			mWebofscienceTextView.startAnimation(animation);
			mMedlineplusTextView.startAnimation(animation);
			mAoSurgeryTextView.startAnimation(animation);
			mWikipediaEnglishTextView.startAnimation(animation);

			mHelsebiblioteketTextView.setVisibility(View.VISIBLE);
			mTidsskriftetTextView.setVisibility(View.VISIBLE);
			mOncolexTextView.setVisibility(View.VISIBLE);
			mBrukerhandbokenTextView.setVisibility(View.VISIBLE);
			mAnalyseoversiktenTextView.setVisibility(View.VISIBLE);
			mNhiTextView.setVisibility(View.VISIBLE);
			mSmlTextView.setVisibility(View.VISIBLE);
			mWikipediaNorwegianTextView.setVisibility(View.VISIBLE);
			mForskningTextView.setVisibility(View.VISIBLE);
			mHelsenorgeTextView.setVisibility(View.VISIBLE);
			mUpToDateTextView.setVisibility(View.VISIBLE);
			mBmjTextView.setVisibility(View.VISIBLE);
			mPubmedTextView.setVisibility(View.VISIBLE);
			mWebofscienceTextView.setVisibility(View.VISIBLE);
			mMedlineplusTextView.setVisibility(View.VISIBLE);
			mAoSurgeryTextView.setVisibility(View.VISIBLE);
			mWikipediaEnglishTextView.setVisibility(View.VISIBLE);
		}
		else
		{
			mDrawerLayout.closeDrawers();
		}
	}

	// View pager
	private class ViewPagerAdapter extends FragmentPagerAdapter
	{
		final String[] pages = getResources().getStringArray(R.array.main_pages);

		ViewPagerAdapter(FragmentManager fragmentManager)
		{
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int position)
		{
			if(position == 1)
			{
				return new SubstancesFragment();
			}
			else if(position == 2)
			{
				return new MedicationsFavoritesFragment();
			}

			return new MedicationsFragment();
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

	// Get SL data
	private void getSlData()
	{
		// Database
		SL_DATA_SQLITE_DATABASE = new SlDataSQLiteHelper(mContext).getReadableDatabase();

		// Layout
		mViewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setPageTransformer(true, new ViewPagerTransformer());

		mTabLayout.setupWithViewPager(mViewPager);

		mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
		{
			@Override
			public void onTabSelected(TabLayout.Tab tab)
			{
				VIEW_PAGER_POSITION = tab.getPosition();

				mSearchEditText.setText("");

				mInputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) { }

			@Override
			public void onTabReselected(TabLayout.Tab tab) { }
		});

		// Firebase
		FirebaseAnalytics.getInstance(mContext);
		FirebaseMessaging.getInstance().subscribeToTopic("message");
		FirebaseMessaging.getInstance().subscribeToTopic("notifications_from_slv");

		if(FirebaseInstanceId.getInstance().getToken() != null)
		{
			String firebaseToken = FirebaseInstanceId.getInstance().getToken();

			mTools.setSharedPreferencesString("FIREBASE_TOKEN", firebaseToken);

			Log.w("FirstFirebaseToken", firebaseToken);
		}
	}
}