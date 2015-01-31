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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.astuetz.PagerSlidingTabStrip;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONObject;

import java.io.InputStream;

public class MainActivity extends ActionBarActivity
{
    public static SQLiteDatabase SQLITE_DATABASE_FELLESKATALOGEN;

    public static int VIEW_PAGER_POSITION = 0;

    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private NotificationManagerCompat mNotificationManagerCompat;

    private InputMethodManager mInputMethodManager;

    private LinearLayout mDrawer;
    private DrawerLayout mDrawerLayout;
    private LinearLayout mToolbarSearchLayout;
    private EditText mToolbarSearchEditText;
    private ProgressBar mProgressBar;
    private ViewPager mViewPager;
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

        toolbar.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                new MaterialDialog.Builder(mContext).title(getString(R.string.main_update_felleskatalogen_testing_dialog_title)).content(getString(R.string.main_update_felleskatalogen_testing_dialog_message)).positiveText(getString(R.string.main_update_felleskatalogen_testing_dialog_positive_button)).negativeText(getString(R.string.main_update_felleskatalogen_testing_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        updateFelleskatalogen(true);
                    }
                }).contentColor(getResources().getColor(R.color.black)).negativeColor(getResources().getColor(R.color.black)).show();

                return true;
            }
        });

        mToolbarSearchLayout = (LinearLayout) findViewById(R.id.main_toolbar_search_layout);
        mToolbarSearchEditText = (EditText) findViewById(R.id.main_toolbar_search);

        mToolbarSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_DONE || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    return true;
                }

                return false;
            }
        });

        final ImageButton toolbarSearchClearButton = (ImageButton) findViewById(R.id.main_toolbar_clear_search);

        toolbarSearchClearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mToolbarSearchEditText.setText("");
            }
        });

        mProgressBar = (ProgressBar) findViewById(R.id.main_toolbar_progressbar);

        // Drawer
        mDrawer = (LinearLayout) findViewById(R.id.main_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
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
                    case R.id.drawer_item_notifications_from_slv:
                    {
                        Intent intent = new Intent(mContext, NotificationsFromSlvActivity.class);
                        startActivity(intent);
                        break;
                    }
                    case R.id.drawer_item_clinicaltrials:
                    {
                        Intent intent = new Intent(mContext, ClinicalTrialsActivity.class);
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
                        String[] feedbackAddress = {getString(R.string.project_feedback_uri)};
                        String feedbackSubject = getString(R.string.project_name);

                        Intent intent = new Intent(Intent.ACTION_SEND, Uri.parse("mailto:"));
                        intent.setType("message/rfc822");
                        intent.putExtra(Intent.EXTRA_EMAIL, feedbackAddress);
                        intent.putExtra(Intent.EXTRA_SUBJECT, feedbackSubject);
                        startActivity(intent);
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
        mViewPager = (ViewPager) findViewById(R.id.main_pager);

        // Floating action button
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.main_fab);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mToolbarSearchLayout.setVisibility(View.VISIBLE);
                mToolbarSearchEditText.requestFocus();

                mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });

        // Alarms
        boolean felleskatalogenAlarmIsStarted = (PendingIntent.getBroadcast(mContext, 1, new Intent(getApplicationContext(), FelleskatalogenAlarm.class), PendingIntent.FLAG_NO_CREATE) != null);

        if(!felleskatalogenAlarmIsStarted)
        {
            FelleskatalogenAlarm felleskatalogenAlarm = new FelleskatalogenAlarm();
            felleskatalogenAlarm.setAlarm(getApplicationContext());
        }

        boolean notificationsFromSlvAlarmIsStarted = (PendingIntent.getBroadcast(mContext, 2, new Intent(getApplicationContext(), NotificationsFromSlvAlarm.class), PendingIntent.FLAG_NO_CREATE) != null);

        if(!notificationsFromSlvAlarmIsStarted)
        {
            NotificationsFromSlvAlarm notificationsFromSlvAlarm = new NotificationsFromSlvAlarm();
            notificationsFromSlvAlarm.setAlarm(getApplicationContext());
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

        // Message
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        int projectVersionCode = mTools.getProjectVersionCode();

        String device = mTools.getDevice();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website)+"api/1/message/?version_name="+projectVersionCode+"&device="+device, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    final long id = response.getLong("id");
                    final String title = response.getString("title");
                    final String message = response.getString("message");

                    final long lastId = mTools.getSharedPreferencesLong("MESSAGE_LAST_ID");

                    if(lastId != 0 && id != lastId) new MaterialDialog.Builder(mContext).title(title).content(message).positiveText(getString(R.string.main_message_dialog_positive_button)).contentColor(getResources().getColor(R.color.black)).show();

                    mTools.setSharedPreferencesLong("MESSAGE_LAST_ID", id);
                }
                catch(Exception e)
                {
                    Log.e("MainActivity", Log.getStackTraceString(e));
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("MainActivity", error.toString());
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);

        // Get Felleskatalogen
        if(!mTools.getSharedPreferencesBoolean("SQLITE_DATABASE_FELLESKATALOGEN_HAS_BEEN_UPDATED")) getFelleskatalogen();
    }

    // Resume activity
    @Override
    protected void onPostResume()
    {
        super.onPostResume();

        if(mTools.getSharedPreferencesBoolean("SQLITE_DATABASE_FELLESKATALOGEN_HAS_BEEN_UPDATED"))
        {
            mNotificationManagerCompat.cancel(FelleskatalogenService.NOTIFICATION_ID);

            mTools.showToast(getString(R.string.main_sqlite_database_felleskatalogen_has_been_updated), 1);

            getFelleskatalogen();

            mTools.setSharedPreferencesBoolean("SQLITE_DATABASE_FELLESKATALOGEN_HAS_BEEN_UPDATED", false);
        }

        if(!mTools.getSharedPreferencesBoolean("RATE_DIALOG_HAS_BEEN_SHOWN"))
        {
            long currentTime = mTools.getCurrentTime();
            long installedTime = mTools.getSharedPreferencesLong("INSTALLED");

            if(currentTime - installedTime > 1000 * 3600 * 48)
            {
                new MaterialDialog.Builder(mContext).title(getString(R.string.main_rate_dialog_title)).content(getString(R.string.main_rate_dialog_message)).positiveText(getString(R.string.main_rate_dialog_positive_button)).negativeText(getString(R.string.main_rate_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("market://details?id=net.olejon.mdapp"));
                        startActivity(intent);
                    }
                }).contentColor(getResources().getColor(R.color.black)).negativeColor(getResources().getColor(R.color.black)).show();

                mTools.setSharedPreferencesBoolean("RATE_DIALOG_HAS_BEEN_SHOWN", true);
            }
        }

        if(!mTools.getSharedPreferencesBoolean("DONATE_DIALOG_HAS_BEEN_SHOWN"))
        {
            long currentTime = mTools.getCurrentTime();
            long installedTime = mTools.getSharedPreferencesLong("INSTALLED");

            if(currentTime - installedTime > 1000 * 3600 * 96)
            {
                new MaterialDialog.Builder(mContext).title(getString(R.string.main_donate_dialog_title)).content(getString(R.string.main_donate_dialog_message)).positiveText(getString(R.string.main_donate_dialog_positive_button)).negativeText(getString(R.string.main_donate_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        Intent intent = new Intent(mContext, DonateActivity.class);
                        startActivity(intent);
                    }
                }).contentColor(getResources().getColor(R.color.black)).negativeColor(getResources().getColor(R.color.black)).show();

                mTools.setSharedPreferencesBoolean("DONATE_DIALOG_HAS_BEEN_SHOWN", true);
            }
        }

        if(!mTools.getSharedPreferencesBoolean("SECOND_DONATE_DIALOG_HAS_BEEN_SHOWN"))
        {
            long currentTime = mTools.getCurrentTime();
            long installedTime = mTools.getSharedPreferencesLong("INSTALLED");

            if(currentTime - installedTime > 1000 * 3600 * 336)
            {
                new MaterialDialog.Builder(mContext).title(getString(R.string.main_donate_dialog_title)).content(getString(R.string.main_donate_dialog_message)).positiveText(getString(R.string.main_donate_dialog_positive_button)).negativeText(getString(R.string.main_donate_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                {
                    @Override
                    public void onPositive(MaterialDialog dialog)
                    {
                        Intent intent = new Intent(mContext, DonateActivity.class);
                        startActivity(intent);
                    }
                }).contentColor(getResources().getColor(R.color.black)).negativeColor(getResources().getColor(R.color.black)).show();

                mTools.setSharedPreferencesBoolean("SECOND_DONATE_DIALOG_HAS_BEEN_SHOWN", true);
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
        else if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
        {
            mToolbarSearchLayout.setVisibility(View.GONE);
            mToolbarSearchEditText.setText("");
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
            mToolbarSearchLayout.setVisibility(View.VISIBLE);
            mToolbarSearchEditText.requestFocus();

            mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

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
            case R.id.main_menu_update_felleskatalogen:
            {
                updateFelleskatalogen(false);
                return true;
            }
            case R.id.main_menu_donate:
            {
                Intent intent = new Intent(mContext, DonateActivity.class);
                startActivity(intent);
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

    // Update Felleskatalogen
    private void updateFelleskatalogen(boolean testing)
    {
        String intentAction = (testing) ? "testing" : "manually";

        Intent intent = new Intent(mContext, FelleskatalogenService.class);
        intent.setAction(intentAction);

        intent.putExtra(FelleskatalogenService.FELLESKATALOGEN_SERVICE_RESULT_RECEIVER_INTENT_EXTRA, new ResultReceiver(new Handler())
        {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData)
            {
                mTools.showToast(getString(R.string.main_sqlite_database_felleskatalogen_has_been_updated), 1);

                if(SQLITE_DATABASE_FELLESKATALOGEN.isOpen()) getFelleskatalogen();

                mTools.setSharedPreferencesBoolean("SQLITE_DATABASE_FELLESKATALOGEN_HAS_BEEN_UPDATED", false);
            }
        });

        startService(intent);
    }

    // Get Felleskatalogen
    private void getFelleskatalogen()
    {
        GetFelleskatalogenTask getFelleskatalogenTask = new GetFelleskatalogenTask();
        getFelleskatalogenTask.execute();
    }

    private class GetFelleskatalogenTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            if(!mTools.getSharedPreferencesBoolean(FelleskatalogenSQLiteHelper.DB_HAS_BEEN_CREATED))
            {
                mProgressBar.setVisibility(View.VISIBLE);

                mTools.showToast(getString(R.string.main_loading), 1);
            }
        }

        @Override
        protected void onPostExecute(Void success)
        {
            mProgressBar.setVisibility(View.GONE);

            PagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

            mViewPager.setAdapter(pagerAdapter);
            mViewPager.setOffscreenPageLimit(3);
            mViewPager.setPageTransformer(true, new ViewPagerTransformer());

            PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.main_tabs);
            pagerSlidingTabStrip.setViewPager(mViewPager);

            pagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
            {
                @Override
                public void onPageSelected(int position)
                {
                    VIEW_PAGER_POSITION = position;

                    mToolbarSearchLayout.setVisibility(View.GONE);

                    mToolbarSearchEditText.setText("");

                    if(position == 2)
                    {
                        mFloatingActionButton.setVisibility(View.GONE);
                    }
                    else
                    {
                        animateFab();
                    }
                }
            });

            animateFab();
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            if(!mTools.getSharedPreferencesBoolean(FelleskatalogenSQLiteHelper.DB_HAS_BEEN_CREATED))
            {
                try
                {
                    InputStream inputStream = getAssets().open(FelleskatalogenSQLiteHelper.DB_ZIPPED_NAME);

                    FelleskatalogenSQLiteCopyHelper felleskatalogenSQLiteCopyHelper = new FelleskatalogenSQLiteCopyHelper(mContext);
                    felleskatalogenSQLiteCopyHelper.copy(inputStream);
                }
                catch(Exception e)
                {
                    Log.e("MainActivity", Log.getStackTraceString(e));
                }
            }

            SQLITE_DATABASE_FELLESKATALOGEN = new FelleskatalogenSQLiteHelper(mContext).getReadableDatabase();

            return null;
        }
    }
}