package net.olejon.mdapp;

/*

Copyright 2016 Ole Jon Bjørkum

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

import android.app.assist.AssistContent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONObject;

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
    private LinearLayout mToolbarSearchLayout;
    private EditText mToolbarSearchEditText;
    private ViewPager mViewPager;
    private WebView mWebView;
    private WebView mNlhWebView;
    private WebView mFelleskatalogenWebView;

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

        // Settings
        PreferenceManager.setDefaultValues(mContext, R.xml.settings, false);

        // Intent
        final Intent intent = getIntent();

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
        mRelativeLayout = (RelativeLayout) findViewById(R.id.medication_inner_layout);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.medication_toolbar);
        mToolbar.setTitle("");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbarSearchLayout = (LinearLayout) findViewById(R.id.medication_toolbar_search_layout);
        mToolbarSearchEditText = (EditText) findViewById(R.id.medication_toolbar_search);

        // View pager
        mViewPager = (ViewPager) findViewById(R.id.medication_pager);

        // Find in text
        mToolbarSearchEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
            {
                String find = mToolbarSearchEditText.getText().toString().trim();

                if(find.equals(""))
                {
                    if(mWebView != null) mWebView.clearMatches();
                }
                else
                {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        if(mWebView != null) mWebView.findAllAsync(find);
                    }
                    else
                    {
                        if(mWebView != null)
                        {
                            //noinspection deprecation
                            mWebView.findAll(find);
                        }
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

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
    }

    // Pause activity
    @Override
    protected void onPause()
    {
        super.onPause();

        mToolbarSearchLayout.setVisibility(View.GONE);
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
        if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
        {
            mToolbarSearchLayout.setVisibility(View.GONE);
            mToolbarSearchEditText.setText("");

            mWebView.clearMatches();
        }
        else
        {
            if(mViewPagerPosition == 0)
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

            if(mViewPagerPosition == 1)
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
            case R.id.medication_menu_find_in_text:
            {
                if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
                {
                    mWebView.findNext(true);

                    mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);
                }
                else
                {
                    mToolbarSearchLayout.setVisibility(View.VISIBLE);
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
                    new MaterialDialog.Builder(mContext).title(R.string.medication_note_dialog_title).content(getString(R.string.medication_note_dialog_message)).positiveText(R.string.medication_note_dialog_positive_button).negativeText(R.string.medication_note_dialog_negative_button).onPositive(new MaterialDialog.SingleButtonCallback()
                    {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                        {
                            Intent intent = new Intent(mContext, NotesActivity.class);
                            startActivity(intent);
                        }
                    }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
                }
                else
                {
                    Intent intent = new Intent(mContext, NotesEditActivity.class);
                    intent.putExtra("title", medicationName);
                    startActivity(intent);
                }

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
                    mTools.printDocument(mNlhWebView, medicationName);
                }
                else
                {
                    mTools.printDocument(mFelleskatalogenWebView, medicationName);
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

            Intent intent = new Intent();
            intent.setAction("update");
            mContext.sendBroadcast(intent);

            mFavoriteMenuItem.setIcon(R.drawable.ic_star_white_24dp).setTitle(getString(R.string.medication_menu_remove_favorite));

            snackbarString = getString(R.string.medication_favorite_saved);
        }

        Snackbar snackbar = Snackbar.make(mRelativeLayout, snackbarString, Snackbar.LENGTH_LONG).setAction(R.string.snackbar_undo, new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                favorite();
            }
        }).setActionTextColor(ContextCompat.getColor(mContext,R.color.orange));

        View snackbarView = snackbar.getView();

        TextView snackbarTextView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);

        snackbarTextView.setTextColor(ContextCompat.getColor(mContext, R.color.white));

        snackbar.show();

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
    private class ViewPagerAdapter extends FragmentPagerAdapter
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

            return (position == 0) ? nlhFragment : felleskatalogenFragment;
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

    // Now on tap
    @Override
    public void onProvideAssistContent(AssistContent assistContent)
    {
        super.onProvideAssistContent(assistContent);

        try
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                String structuredJson = new JSONObject().put("@type", "Drug").put("name", medicationName).put("activeIngredient", medicationSubstance).put("manufacturer", medicationManufacturer).toString();

                assistContent.setStructuredData(structuredJson);
            }
        }
        catch(Exception e)
        {
            Log.e("MedicationActivity", Log.getStackTraceString(e));
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
                medicationPrescriptionGroup = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP));
                medicationName = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME));
                medicationSubstance = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE));
                medicationManufacturer = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER));
                medicationAtcCode = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_ATC_CODE));

                mToolbar.setTitle(medicationName);

                if(isFavorite()) mFavoriteMenuItem.setIcon(R.drawable.ic_star_white_24dp).setTitle(getString(R.string.medication_menu_remove_favorite));

                mAtcCodeMenuItem.setTitle(getString(R.string.medication_menu_atc, medicationAtcCode));

                Button prescriptionGroupButton = (Button) findViewById(R.id.medication_prescription_group);
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

                prescriptionGroupButton.setVisibility(View.VISIBLE);

                TextView substanceTextView = (TextView) findViewById(R.id.medication_substance);
                substanceTextView.setText(medicationSubstance);

                TextView manufacturerTextView = (TextView) findViewById(R.id.medication_manufacturer);
                manufacturerTextView.setText(medicationManufacturer);

                if(mTools.isDeviceConnected())
                {
                    PagerAdapter pagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

                    mViewPager.setAdapter(pagerAdapter);
                    mViewPager.setOffscreenPageLimit(2);
                    mViewPager.setPageTransformer(true, new ViewPagerTransformer());

                    mNlhWebView = (WebView) findViewById(R.id.medication_nlh_content);
                    mFelleskatalogenWebView = (WebView) findViewById(R.id.medication_felleskatalogen_content);

                    mWebView = mNlhWebView;

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    {
                        mWebView.setFindListener(new WebView.FindListener()
                        {
                            @Override
                            public void onFindResultReceived(int i, int i2, boolean b)
                            {
                                if(i2 == 0) mTools.showToast(getString(R.string.main_webview_find_in_text_no_results), 1);
                            }
                        });
                    }

                    TabLayout tabLayout = (TabLayout) findViewById(R.id.medication_tabs);
                    tabLayout.setupWithViewPager(mViewPager);

                    tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
                    {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab)
                        {
                            mViewPagerPosition = tab.getPosition();

                            mToolbarSearchLayout.setVisibility(View.GONE);
                            mToolbarSearchEditText.setText("");

                            mWebView.clearMatches();

                            mWebView = (mViewPagerPosition == 0) ? mNlhWebView : mFelleskatalogenWebView;

                            mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) { }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) { }
                    });

                    if(!mTools.getSharedPreferencesBoolean("MEDICATION_HIDE_INFORMATION_DIALOG_300"))
                    {
                        new MaterialDialog.Builder(mContext).title(R.string.medication_information_dialog_title).content(getString(R.string.medication_information_dialog_message)).positiveText(R.string.medication_information_dialog_positive_button).onPositive(new MaterialDialog.SingleButtonCallback()
                        {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                            {
                                mTools.setSharedPreferencesBoolean("MEDICATION_HIDE_INFORMATION_DIALOG_300", true);
                            }
                        }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
                    }
                }
                else
                {
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
                    }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
                }
            }
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();

            String[] queryColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME, SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE, SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER, SlDataSQLiteHelper.MEDICATIONS_COLUMN_ATC_CODE};
            mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, queryColumns, SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID+" = "+medicationId, null, null, null, null);

            return null;
        }
    }
}