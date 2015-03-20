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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.astuetz.PagerSlidingTabStrip;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.net.URLEncoder;

public class MedicationActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private Toolbar mToolbar;
    private MenuItem mFavoriteMenuItem;
    private MenuItem mAtcCodeMenuItem;
    private ViewPager mViewPager;

    private long medicationId;
    private String medicationPrescriptionGroup;
    private String medicationName;
    private String medicationSubstance;
    private String medicationManufacturer;
    private String medicationAtcCode;

    private int mViewPagerPosition = 0;

    // Create activity
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Intent
        final Intent intent = getIntent();

        medicationId = intent.getLongExtra("id", 0);

        if(medicationId == 0)
        {
            mTools.showToast(getString(R.string.medication_could_not_find_medication), 1);

            finish();

            return;
        }

        // Layout
        setContentView(R.layout.activity_medication);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.medication_toolbar);
        mToolbar.setTitle("");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // View pager
        mViewPager = (ViewPager) findViewById(R.id.medication_pager);
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
        if(MedicationNlhFragment.WEBVIEW == null || MedicationFelleskatalogenFragment.WEBVIEW == null)
        {
            super.onBackPressed();
        }
        else
        {
            if(mViewPagerPosition == 0)
            {
                if(MedicationNlhFragment.WEBVIEW.canGoBack())
                {
                    MedicationNlhFragment.WEBVIEW.goBack();
                }
                else
                {
                    super.onBackPressed();
                }
            }

            if(mViewPagerPosition == 1)
            {
                if(MedicationFelleskatalogenFragment.WEBVIEW.canGoBack())
                {
                    MedicationFelleskatalogenFragment.WEBVIEW.goBack();
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
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.medication_menu_favorite:
            {
                favorite();
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
                Intent intent = new Intent(mContext, PoisoningsActivity.class);
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
                Intent intent = new Intent(mContext, NotesEditActivity.class);
                intent.putExtra("title", medicationName);
                startActivity(intent);
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
                    Intent intent = new Intent(mContext, MedicationWebViewActivity.class);
                    intent.putExtra("title", getString(R.string.medication_menu_slv));
                    intent.putExtra("uri", "http://www.legemiddelverket.no/Legemiddelsoek/Sider/default.aspx?searchquery="+URLEncoder.encode(medicationName, "utf-8"));
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
                    mTools.printDocument(MedicationNlhFragment.WEBVIEW, medicationName);
                }
                else
                {
                    mTools.printDocument(MedicationFelleskatalogenFragment.WEBVIEW, medicationName);
                }

                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Favorite
    private boolean isFavorite()
    {
        SQLiteDatabase sqLiteDatabase = new MedicationsFavoritesSQLiteHelper(mContext).getReadableDatabase();

        String[] queryColumns = {MedicationsFavoritesSQLiteHelper.COLUMN_NAME, MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER};
        Cursor cursor = sqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, queryColumns, MedicationsFavoritesSQLiteHelper.COLUMN_NAME+" = "+mTools.sqe(medicationName)+" AND "+MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER+" = "+mTools.sqe(medicationManufacturer), null, null, null, null);

        int count = cursor.getCount();

        cursor.close();
        sqLiteDatabase.close();

        return (count != 0);
    }

    private void favorite()
    {
        SQLiteDatabase sqLiteDatabase = new MedicationsFavoritesSQLiteHelper(mContext).getWritableDatabase();

        String snackbarString;

        if(isFavorite())
        {
            sqLiteDatabase.delete(MedicationsFavoritesSQLiteHelper.TABLE, MedicationsFavoritesSQLiteHelper.COLUMN_NAME+" = "+mTools.sqe(medicationName)+" AND "+MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER+" = "+mTools.sqe(medicationManufacturer), null);

            mFavoriteMenuItem.setIcon(R.drawable.ic_star_outline_white_24dp);

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

            Intent intent = new Intent();
            intent.setAction("update");
            mContext.sendBroadcast(intent);

            mFavoriteMenuItem.setIcon(R.drawable.ic_star_white_24dp);

            snackbarString = getString(R.string.medication_favorite_saved);
        }

        SnackbarManager.show(Snackbar.with(mContext).text(snackbarString).colorResource(R.color.dark).actionLabel(getString(R.string.snackbar_undo)).actionLabelTypeface(Typeface.DEFAULT_BOLD).actionColorResource(R.color.orange).actionListener(new ActionClickListener()
        {
            @Override
            public void onActionClicked(Snackbar snackbar)
            {
                favorite();
            }
        }));

        mTools.updateWidget();

        sqLiteDatabase.close();
    }

    // Manufacturer
    private void getManufacturer()
    {
        SQLiteDatabase sqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();

        String[] queryColumns = {SlDataSQLiteHelper.MANUFACTURERS_COLUMN_ID};
        Cursor cursor = sqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MANUFACTURERS, queryColumns, SlDataSQLiteHelper.MANUFACTURERS_COLUMN_NAME+" = "+mTools.sqe(medicationManufacturer), null, null, null, null);

        if(cursor.moveToFirst())
        {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MANUFACTURERS_COLUMN_ID));

            Intent intent = new Intent(mContext, ManufacturerActivity.class);
            intent.putExtra("id", id);
            startActivity(intent);
        }

        cursor.close();
        sqLiteDatabase.close();
    }

    // View pager
    private class ViewPagerAdapter extends FragmentStatePagerAdapter
    {
        private final String[] pages = getResources().getStringArray(R.array.medication_pages);

        public ViewPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position)
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

            Fragment nlhFragment = new MedicationNlhFragment();
            Bundle nlhBundle = new Bundle();
            nlhBundle.putString("uri", "http://m.legemiddelhandboka.no/s%C3%B8keresultat/?q="+searchString);
            nlhFragment.setArguments(nlhBundle);

            Fragment felleskatalogenFragment = new MedicationFelleskatalogenFragment();
            Bundle felleskatalogenBundle = new Bundle();
            felleskatalogenBundle.putString("uri", "http://www.felleskatalogen.no/ir/medisin/sok?sokord="+searchString);
            felleskatalogenFragment.setArguments(felleskatalogenBundle);

            if(position == 0) return nlhFragment;

            return felleskatalogenFragment;
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

    // Get medication
    private void getMedication()
    {
        GetMedicationTask getMedicationTask = new GetMedicationTask();
        getMedicationTask.execute();
    }

    private class GetMedicationTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPostExecute(Void success)
        {
            if(mCursor.moveToFirst())
            {
                final String medicationBluePrescription = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_BLUE_PRESCRIPTION));

                medicationPrescriptionGroup = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP));
                medicationName = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME));
                medicationSubstance = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE));
                medicationManufacturer = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER));
                medicationAtcCode = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_ATC_CODE));

                mToolbar.setTitle(medicationName);

                if(isFavorite()) mFavoriteMenuItem.setIcon(R.drawable.ic_star_white_24dp);

                mAtcCodeMenuItem.setTitle(getString(R.string.medication_menu_atc)+" ("+medicationAtcCode+")");

                Button prescriptionGroupButton = (Button) findViewById(R.id.medication_prescription_group);
                prescriptionGroupButton.setText(medicationPrescriptionGroup);

                switch(medicationPrescriptionGroup)
                {
                    case "A":
                    {
                        mTools.setBackgroundDrawable(prescriptionGroupButton, R.drawable.main_medications_list_item_circle_red);
                        break;
                    }
                    case "B":
                    {
                        mTools.setBackgroundDrawable(prescriptionGroupButton, R.drawable.main_medications_list_item_circle_orange);
                        break;
                    }
                    case "C":
                    {
                        mTools.setBackgroundDrawable(prescriptionGroupButton, R.drawable.main_medications_list_item_circle_green);
                        break;
                    }
                    case "F":
                    {
                        mTools.setBackgroundDrawable(prescriptionGroupButton, R.drawable.main_medications_list_item_circle_green);
                        break;
                    }
                }

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

                Button bluePrescriptionButton = (Button) findViewById(R.id.medication_blue_prescription);

                if(medicationBluePrescription.equals("yes"))
                {
                    bluePrescriptionButton.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            mTools.showToast(getString(R.string.medication_blue_prescription), 1);
                        }
                    });

                    bluePrescriptionButton.setVisibility(View.VISIBLE);
                }

                TextView substanceTextView = (TextView) findViewById(R.id.medication_substance);
                substanceTextView.setText(medicationSubstance);

                TextView manufacturerTextView = (TextView) findViewById(R.id.medication_manufacturer);
                manufacturerTextView.setText(medicationManufacturer);

                if(mTools.isDeviceConnected())
                {
                    PagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

                    mViewPager.setAdapter(pagerAdapter);
                    mViewPager.setPageTransformer(true, new ViewPagerTransformer());

                    PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.medication_tabs);
                    pagerSlidingTabStrip.setViewPager(mViewPager);

                    pagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
                    {
                        @Override
                        public void onPageSelected(int position)
                        {
                            mViewPagerPosition = position;
                        }
                    });

                    boolean hideMedicationTipDialog = mTools.getSharedPreferencesBoolean("MEDICATION_HIDE_MEDICATION_TIP_DIALOG_140");

                    if(!hideMedicationTipDialog)
                    {
                        new MaterialDialog.Builder(mContext).title(getString(R.string.medication_tip_dialog_title)).content(getString(R.string.medication_tip_dialog_message)).positiveText(getString(R.string.medication_tip_dialog_positive_button)).callback(new MaterialDialog.ButtonCallback()
                        {
                            @Override
                            public void onPositive(MaterialDialog dialog)
                            {
                                mTools.setSharedPreferencesBoolean("MEDICATION_HIDE_MEDICATION_TIP_DIALOG_140", true);
                            }
                        }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
                    }
                }
                else
                {
                    new MaterialDialog.Builder(mContext).title(getString(R.string.medication_not_connected_dialog_title)).content(getString(R.string.medication_not_connected_dialog_message)).positiveText(getString(R.string.medication_not_connected_dialog_positive_button)).negativeText(getString(R.string.medication_not_connected_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                    {
                        @Override
                        public void onPositive(MaterialDialog dialog)
                        {
                            getMedication();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog)
                        {
                            finish();
                        }
                    }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
                }
            }
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();

            String[] queryColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_BLUE_PRESCRIPTION, SlDataSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME, SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE, SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER, SlDataSQLiteHelper.MEDICATIONS_COLUMN_ATC_CODE};
            mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, queryColumns, SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID+" = "+medicationId, null, null, null, null);

            return null;
        }
    }
}