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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

public class MainActivity extends ActionBarActivity
{
    public static SQLiteDatabase SQLITE_DATABASE_FELLESKATALOGEN;

    public static int VIEW_PAGER_POSITION = 0;

    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private NotificationManagerCompat mNotificationManagerCompat;

    private InputMethodManager mInputMethodManager;

    private MenuItem goForwardMenuItem;
    private LinearLayout mDrawer;
    private DrawerLayout mDrawerLayout;
    private ProgressBar mProgressBar;
    private EditText mSearchEditText;
    private ViewPager mViewPager;
    private WebView mWebView;
    private FloatingActionButton mFloatingActionButton;

    private int mDrawerClosed;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Settings
        PreferenceManager.setDefaultValues(mContext, R.xml.settings, false);

        // Installed
        long installed = mTools.getSharedPreferencesLong("INSTALLED");

        if(installed == 0) mTools.setSharedPreferencesLong("INSTALLED", mTools.getCurrentTime());

        // Notification manager
        mNotificationManagerCompat = NotificationManagerCompat.from(mContext);

        // Input manager
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Layout
        setContentView(R.layout.activity_main);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        toolbar.setTitle(getString(R.string.main_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);

        mProgressBar = (ProgressBar) findViewById(R.id.main_toolbar_progressbar);

        // Search
        /*mSearchEditText = (EditText) findViewById(R.id.main_search_edittext);

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_DONE || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    mInputMethodManager.toggleSoftInputFromWindow(mSearchEditText.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    return true;
                }

                return false;
            }
        });*/

        // Drawer
        mDrawer = (LinearLayout) findViewById(R.id.main_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);

        TextView drawerVersionNameTextView = (TextView) findViewById(R.id.drawer_version_name);
        TextView drawerVersionCodeTextView = (TextView) findViewById(R.id.drawer_version_code);

        drawerVersionNameTextView.setText(getString(R.string.drawer_version_name)+": "+mTools.getProjectVersionName());
        drawerVersionCodeTextView.setText(getString(R.string.drawer_version_code)+": "+mTools.getProjectVersionCode());

        mDrawerLayout.setDrawerListener(new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.project_name, R.string.project_name));

        mDrawerLayout.setDrawerListener(new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_content_description, R.string.drawer_content_description)
        {
            @Override
            public void onDrawerOpened(View drawerView)
            {
                mDrawerClosed = 0;
            }

            @Override
            public void onDrawerClosed(View drawerView)
            {
                switch(mDrawerClosed)
                {
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
                    case R.id.drawer_item_nasjonale_retningslinjer:
                    {
                        Intent intent = new Intent(mContext, NasjonaleRetningslinjerActivity.class);
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
                    /*case R.id.drawer_item_atc:
                    {
                        Intent intent = new Intent(mContext, AtcActivity.class);
                        startActivity(intent);
                        break;
                    }*/
                    case R.id.drawer_item_icd10:
                    {
                        Intent intent = new Intent(mContext, Icd10Activity.class);
                        startActivity(intent);
                        break;
                    }
                    /*case R.id.drawer_item_manufacturers:
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
                    }*/
                    case R.id.drawer_item_clinicaltrials:
                    {
                        Intent intent = new Intent(mContext, ClinicalTrialsActivity.class);
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
                        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"+getString(R.string.project_feedback_uri)+"?subject="+getString(R.string.project_name)));
                        startActivity(Intent.createChooser(intent, getString(R.string.project_feedback_text)));
                        break;
                    }
                    case R.id.drawer_item_report_issue:
                    {
                        mTools.openUri(getString(R.string.project_report_issue_uri));
                        break;
                    }
                }
            }
        });

        // View pager
        //mViewPager = (ViewPager) findViewById(R.id.main_pager);

        // Web view
        mWebView = (WebView) findViewById(R.id.main_webview);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        mWebView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                return false;
            }
        });

        mWebView.loadUrl("http://www.felleskatalogen.no/m/medisin/");

        // Floating action button
        /*mFloatingActionButton = (FloatingActionButton) findViewById(R.id.main_fab);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mSearchEditText.requestFocus();

                mInputMethodManager.toggleSoftInputFromWindow(mSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });*/

        // Alarms
        Context applicationContext = getApplicationContext();

        boolean messageAlarmIsStarted = (PendingIntent.getBroadcast(mContext, 1, new Intent(applicationContext, MessageIntentService.class), PendingIntent.FLAG_NO_CREATE) != null);

        if(!messageAlarmIsStarted)
        {
            MessageAlarm messageAlarm = new MessageAlarm();
            messageAlarm.setAlarm(applicationContext);
        }

        boolean notificationsFromSlvAlarmIsStarted = (PendingIntent.getBroadcast(mContext, 2, new Intent(applicationContext, NotificationsFromSlvAlarm.class), PendingIntent.FLAG_NO_CREATE) != null);

        if(!notificationsFromSlvAlarmIsStarted)
        {
            NotificationsFromSlvAlarm notificationsFromSlvAlarm = new NotificationsFromSlvAlarm();
            notificationsFromSlvAlarm.setAlarm(applicationContext);
        }

        // Welcome
        boolean welcomeActivityHasBeenShown = mTools.getSharedPreferencesBoolean("WELCOME_ACTIVITY_HAS_BEEN_SHOWN");

        if(!welcomeActivityHasBeenShown)
        {
            Handler handler = new Handler();

            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    Intent intent = new Intent(mContext, WelcomeActivity.class);
                    startActivity(intent);
                }
            }, 1000);
        }

        // Dialog
        new MaterialDialog.Builder(mContext).title(getString(R.string.main_dialog_title)).content(getString(R.string.main_dialog_message)).positiveText(getString(R.string.main_dialog_positive_button)).neutralText(getString(R.string.main_dialog_neutral_button)).callback(new MaterialDialog.ButtonCallback()
        {
            @Override
            public void onPositive(MaterialDialog dialog)
            {
                dialog.dismiss();
            }

            @Override
            public void onNeutral(MaterialDialog dialog)
            {
                mTools.openUri("http://www.felleskatalogen.no/m/medisin/kontakt-oss");
            }
        }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.dark_blue).autoDismiss(false).show();
    }

    // Resume activity
    @Override
    protected void onResume()
    {
        super.onResume();

        // Connected?
        if(!mTools.isDeviceConnected()) mTools.showToast(getString(R.string.main_not_connected), 1);

        // Rate
        if(!mTools.getSharedPreferencesBoolean("MAIN_HIDE_RATE_DIALOG"))
        {
            long currentTime = mTools.getCurrentTime();
            long installedTime = mTools.getSharedPreferencesLong("INSTALLED");

            if(currentTime - installedTime > 1000 * 3600 * 48)
            {
                mTools.setSharedPreferencesBoolean("MAIN_HIDE_RATE_DIALOG", true);

                new MaterialDialog.Builder(mContext).title(getString(R.string.main_rate_dialog_title)).content(getString(R.string.main_rate_dialog_message)).positiveText(getString(R.string.main_rate_dialog_positive_button)).negativeText(getString(R.string.main_rate_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=net.olejon.mdapp"));
                        startActivity(intent);
                    }
                }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
            }
        }

        // Donate
        if(!mTools.getSharedPreferencesBoolean("MAIN_HIDE_DONATE_DIALOG"))
        {
            long currentTime = mTools.getCurrentTime();
            long installedTime = mTools.getSharedPreferencesLong("INSTALLED");

            if(currentTime - installedTime > 1000 * 3600 * 96)
            {
                mTools.setSharedPreferencesBoolean("MAIN_HIDE_DONATE_DIALOG", true);

                new MaterialDialog.Builder(mContext).title(getString(R.string.main_donate_dialog_title)).content(getString(R.string.main_donate_dialog_message)).positiveText(getString(R.string.main_donate_dialog_positive_button)).negativeText(getString(R.string.main_donate_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        Intent intent = new Intent(mContext, DonateActivity.class);
                        startActivity(intent);
                    }
                }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
            }
        }

        if(!mTools.getSharedPreferencesBoolean("MAIN_HIDE_SECOND_DONATE_DIALOG"))
        {
            long currentTime = mTools.getCurrentTime();
            long installedTime = mTools.getSharedPreferencesLong("INSTALLED");

            if(currentTime - installedTime > 1000 * 3600 * 336)
            {
                mTools.setSharedPreferencesBoolean("MAIN_HIDE_SECOND_DONATE_DIALOG", true);

                new MaterialDialog.Builder(mContext).title(getString(R.string.main_donate_dialog_title)).content(getString(R.string.main_donate_dialog_message)).positiveText(getString(R.string.main_donate_dialog_positive_button)).negativeText(getString(R.string.main_donate_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        Intent intent = new Intent(mContext, DonateActivity.class);
                        startActivity(intent);
                    }
                }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
            }
        }
    }

    // Destroy activity
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(SQLITE_DATABASE_FELLESKATALOGEN != null && SQLITE_DATABASE_FELLESKATALOGEN.isOpen()) SQLITE_DATABASE_FELLESKATALOGEN.close();
    }

    // Back button
    @Override
    public void onBackPressed()
    {
        if(mDrawerLayout.isDrawerOpen(mDrawer))
        {
            mDrawerLayout.closeDrawers();
        }
        /*else if(!mSearchEditText.getText().toString().equals(""))
        {
            mSearchEditText.setText("");
        }*/
        else if(mWebView.canGoBack())
        {
            mWebView.goBack();
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

            mInputMethodManager.toggleSoftInputFromWindow(mSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        goForwardMenuItem = menu.findItem(R.id.main_menu_go_forward);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.main_menu_go_forward:
            {
                mWebView.goForward();
                return true;
            }
            /*case R.id.main_menu_scan_barcode:
            {
                Intent intent = new Intent(mContext, BarcodeScannerActivity.class);
                startActivity(intent);
                return true;
            }*/
            case R.id.main_menu_uri:
            {
                mTools.openUri("http://www.felleskatalogen.no/");
                return true;
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

        mDrawerLayout.closeDrawers();
    }

    // View pager
    private class ViewPagerAdapter extends FragmentStatePagerAdapter
    {
        private final String[] pages = getResources().getStringArray(R.array.main_pages);

        public ViewPagerAdapter(FragmentManager fragmentManager)
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
        public CharSequence getPageTitle(int position)
        {
            return pages[position];
        }

        @Override
        public int getCount()
        {
            return pages.length;
        }
    }

    private class ViewPagerTransformer implements ViewPager.PageTransformer
    {
        public void transformPage(View view, float position)
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
                    view.setTranslationX(-horzizontalMargin + verticalMargin / 2);
                }

                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            }
        }
    }

    // Floating action button
    private void animateFab()
    {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fab);
        mFloatingActionButton.startAnimation(animation);

        mFloatingActionButton.setVisibility(View.VISIBLE);
    }
}