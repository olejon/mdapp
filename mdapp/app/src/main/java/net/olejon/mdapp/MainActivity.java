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
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity
{
	public static final String NOTIFICATION_CHANNEL_MESSAGE_ID = "net.olejon.mdapp.NOTIFICATION_CHANNEL_MESSAGE";
	public static final String NOTIFICATION_CHANNEL_SLV_ID = "net.olejon.mdapp.NOTIFICATION_CHANNEL_SLV";

	public static SQLiteDatabase SL_DATA_SQLITE_DATABASE;

	public static int VIEW_PAGER_POSITION = 0;

	private final Activity mActivity = this;

	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private InputMethodManager mInputMethodManager;

	private DrawerLayout mDrawerLayout;
	private NavigationView mDrawer;
	private EditText mSearchEditText;
	private ViewPager mViewPager;
	private TabLayout mTabLayout;

	private int mDrawerClosed;

	private boolean mDrawerEncyclopediasGroupVisible = false;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Settings
		PreferenceManager.setDefaultValues(mContext, R.xml.settings, false);

		// Installed
		long installed = mTools.getSharedPreferencesLong("INSTALLED_4200");

		if(installed == 0) mTools.setSharedPreferencesLong("INSTALLED_4200", mTools.getCurrentTime());

		// Notification manager
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager != null)
		{
			NotificationChannel notificationMessageChannel = new NotificationChannel(NOTIFICATION_CHANNEL_MESSAGE_ID, getString(R.string.notification_channel_message_name), NotificationManager.IMPORTANCE_DEFAULT);
			notificationMessageChannel.setShowBadge(true);
			notificationMessageChannel.setDescription(getString(R.string.notification_channel_message_description));
			notificationManager.createNotificationChannel(notificationMessageChannel);

			NotificationChannel notificationSlvChannel = new NotificationChannel(NOTIFICATION_CHANNEL_SLV_ID, getString(R.string.notification_channel_slv_name), NotificationManager.IMPORTANCE_LOW);
			notificationSlvChannel.setShowBadge(true);
			notificationSlvChannel.setDescription(getString(R.string.notification_channel_slv_description));
			notificationManager.createNotificationChannel(notificationSlvChannel);
		}

		// Input manager
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_main);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.main_toolbar);
		toolbar.setTitle(getString(R.string.main_title));

		setSupportActionBar(toolbar);

		if(getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
		}

		// Drawer
		mDrawerLayout = findViewById(R.id.main_drawer_layout);
		mDrawer = findViewById(R.id.main_drawer);

		final Menu drawerMenu = mDrawer.getMenu();

		mDrawerLayout.addDrawerListener(new ActionBarDrawerToggle(mActivity, mDrawerLayout, toolbar, R.string.drawer_content_description, R.string.drawer_content_description)
		{
			@Override
			public void onDrawerOpened(View drawerView)
			{
				mDrawerClosed = 0;
			}

			@Override
			public void onDrawerClosed(View drawerView)
			{
				mDrawerEncyclopediasGroupVisible = false;

				drawerMenu.setGroupVisible(R.id.drawer_group_encyclopedias, false);
				drawerMenu.findItem(R.id.drawer_item_encyclopedias).setIcon(R.drawable.ic_unfold_more_black_24dp).setTitle(R.string.drawer_item_encyclopedias_show_more);

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
					case R.id.drawer_item_helsenorge:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_helsenorge));
						intent.putExtra("uri", "https://helsenorge.no/");
						startActivity(intent);
						break;
					}
					case R.id.drawer_item_forskning:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_forskning));
						intent.putExtra("uri", "https://forskning.no/");
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
						intent.putExtra("uri", "https://tidsskriftet.no/");
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
						intent.putExtra("uri", "https://brukerhandboken.no/");
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
					case R.id.drawer_item_wikipedia_english:
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", getString(R.string.drawer_item_wikipedia_english));
						intent.putExtra("uri", "https://en.wikipedia.org/");
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
						intent.putExtra("uri", "https://bestpractice.bmj.com/");
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

		mDrawer.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
		{
			@Override
			public boolean onNavigationItemSelected(@NonNull MenuItem item)
			{
				mDrawerClosed = item.getItemId();

				if(mDrawerClosed == R.id.drawer_item_encyclopedias)
				{
					if(mDrawerEncyclopediasGroupVisible)
					{
						mDrawerEncyclopediasGroupVisible = false;

						drawerMenu.setGroupVisible(R.id.drawer_group_encyclopedias, false);
						drawerMenu.findItem(R.id.drawer_item_encyclopedias).setIcon(R.drawable.ic_unfold_more_black_24dp).setTitle(R.string.drawer_item_encyclopedias_show_more);
					}
					else
					{
						mDrawerEncyclopediasGroupVisible = true;

						drawerMenu.setGroupVisible(R.id.drawer_group_encyclopedias, true);
						drawerMenu.findItem(R.id.drawer_item_encyclopedias).setIcon(R.drawable.ic_unfold_less_black_24dp).setTitle(R.string.drawer_item_encyclopedias_hide_more);
					}
				}
				else
				{
					mDrawerLayout.closeDrawers();
				}

				return true;
			}
		});

		// Search
		mSearchEditText = findViewById(R.id.main_search_edittext);

		mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				if(i == EditorInfo.IME_ACTION_SEARCH)
				{
					if(mInputMethodManager != null) mInputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);

					return true;
				}

				return false;
			}
		});

		// View pager
		mViewPager = findViewById(R.id.main_pager);

		// Tab layout
		mTabLayout = findViewById(R.id.main_tabs);

		// Floating action button
		FloatingActionButton floatingActionButton = findViewById(R.id.main_fab);
		floatingActionButton.setVisibility(View.VISIBLE);

		floatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mSearchEditText.requestFocus();

				if(mInputMethodManager != null) mInputMethodManager.showSoftInput(mSearchEditText, 0);
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

						InputStream inputStream = mContext.getAssets().open(SlDataSQLiteHelper.DB_NAME);
						OutputStream outputStream = new FileOutputStream(slDataSqLiteDatabase.getPath());

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

		// Input manager
		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				mInputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
			}
		}, 250);

		// Rate
		if(!mTools.getSharedPreferencesBoolean("MAIN_HIDE_RATE_DIALOG_4200"))
		{
			long currentTime = mTools.getCurrentTime();
			long installedTime = mTools.getSharedPreferencesLong("INSTALLED_4200");

			if(currentTime - installedTime > 1000 * 3600 * 96)
			{
				new MaterialDialog.Builder(mContext).title(R.string.main_rate_dialog_title).content(getString(R.string.main_rate_dialog_message)).positiveText(R.string.main_rate_dialog_positive_button).negativeText(R.string.main_rate_dialog_negative_button).onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
					{
						mTools.setSharedPreferencesBoolean("MAIN_HIDE_RATE_DIALOG_4200", true);

						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=net.olejon.mdapp"));
						startActivity(intent);
					}
				}).onNegative(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which)
					{
						mTools.setSharedPreferencesBoolean("MAIN_HIDE_RATE_DIALOG_4200", true);
					}
				}).titleColorRes(R.color.teal).contentColorRes(R.color.dark).positiveColorRes(R.color.teal).negativeColorRes(R.color.dark).neutralColorRes(R.color.teal).buttonRippleColorRes(R.color.light_grey).show();
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

				if(mInputMethodManager != null) mInputMethodManager.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
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

		FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>()
		{
			@Override
			public void onComplete(@NonNull Task<InstanceIdResult> task)
			{
				if(task.isSuccessful() && task.getResult() != null)
				{
					String firebaseToken = task.getResult().getToken();

					mTools.setSharedPreferencesString("FIREBASE_TOKEN", firebaseToken);

					Log.w("InitialFirebaseToken", firebaseToken);
				}
			}
		});
	}
}