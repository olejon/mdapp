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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

public class ClinicalTrialsActivity extends AppCompatActivity
{
    private static final int VOICE_SEARCH_REQUEST_CODE = 1;

    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private InputMethodManager mInputMethodManager;

    private LinearLayout mToolbarSearchLayout;
    private EditText mToolbarSearchEditText;
    private FloatingActionButton mFloatingActionButton;
    private ListView mListView;

    private boolean mActivityPaused = false;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Input manager
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Layout
        setContentView(R.layout.activity_clinicaltrials);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.clinicaltrials_toolbar);
        toolbar.setTitle(getString(R.string.clinicaltrials_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbarSearchLayout = (LinearLayout) findViewById(R.id.clinicaltrials_toolbar_search_layout);
        mToolbarSearchEditText = (EditText) findViewById(R.id.clinicaltrials_toolbar_search);

        mToolbarSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_SEARCH || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);

                    search(mToolbarSearchEditText.getText().toString().trim());

                    return true;
                }

                return false;
            }
        });

        // List
        mListView = (ListView) findViewById(R.id.clinicaltrials_list);

        View listViewEmpty = findViewById(R.id.clinicaltrials_list_empty);
        mListView.setEmptyView(listViewEmpty);

        View listViewHeader = getLayoutInflater().inflate(R.layout.activity_clinicaltrials_list_subheader, mListView, false);
        mListView.addHeaderView(listViewHeader, null, false);

        // Floating action button
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.clinicaltrials_fab);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
                {
                    mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);

                    search(mToolbarSearchEditText.getText().toString().trim());
                }
                else
                {
                    mToolbarSearchLayout.setVisibility(View.VISIBLE);
                    mToolbarSearchEditText.requestFocus();

                    mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
                }
            }
        });
    }

    // Activity result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == VOICE_SEARCH_REQUEST_CODE && data != null)
        {
            ArrayList<String> voiceSearchArrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            final String voiceSearchString = voiceSearchArrayList.get(0);

            search(voiceSearchString);
        }
    }

    // Resume activity
    @Override
    protected void onResume()
    {
        super.onResume();

        getRecentSearches();
    }

    // Pause activity
    @Override
    protected void onPause()
    {
        super.onPause();

        mActivityPaused = true;
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

            mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_clinicaltrials, menu);
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
            case R.id.clinicaltrials_menu_voice_search:
            {
                try
                {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
                }
                catch(Exception e)
                {
                    new MaterialDialog.Builder(mContext).title(getString(R.string.device_not_supported_dialog_title)).content(getString(R.string.device_not_supported_dialog_message)).positiveText(getString(R.string.device_not_supported_dialog_positive_button)).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
                }

                return true;
            }
            case R.id.clinicaltrials_menu_clear_recent_searches:
            {
                clearRecentSearches();
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Search
    private void search(final String searchString)
    {
        if(searchString.equals("")) return;

        Intent intent = new Intent(mContext, ClinicalTrialsCardsActivity.class);
        intent.putExtra("search", searchString);
        startActivity(intent);
    }

    private void getRecentSearches()
    {
        GetRecentSearchesTask getRecentSearchesTask = new GetRecentSearchesTask();
        getRecentSearchesTask.execute();
    }

    private void clearRecentSearches()
    {
        mSqLiteDatabase.delete(ClinicalTrialsSQLiteHelper.TABLE, null, null);

        mTools.showToast(getString(R.string.clinicaltrials_recent_searches_removed), 0);

        getRecentSearches();
    }

    private class GetRecentSearchesTask extends AsyncTask<Void, Void, SimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(SimpleCursorAdapter simpleCursorAdapter)
        {
            mListView.setAdapter(simpleCursorAdapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    int index = i - 1;

                    if(mCursor.moveToPosition(index)) search(mCursor.getString(mCursor.getColumnIndexOrThrow(ClinicalTrialsSQLiteHelper.COLUMN_STRING)).trim());
                }
            });

            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fab);

            mFloatingActionButton.startAnimation(animation);
            mFloatingActionButton.setVisibility(View.VISIBLE);

            if(!mActivityPaused && mCursor.getCount() > 0)
            {
                Handler handler = new Handler();

                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mToolbarSearchLayout.setVisibility(View.VISIBLE);
                        mToolbarSearchEditText.requestFocus();

                        mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
                    }
                }, 500);
            }
        }

        @Override
        protected SimpleCursorAdapter doInBackground(Void... voids)
        {
            mSqLiteDatabase = new ClinicalTrialsSQLiteHelper(mContext).getWritableDatabase();

            mCursor = mSqLiteDatabase.query(ClinicalTrialsSQLiteHelper.TABLE, null, null, null, null, null, ClinicalTrialsSQLiteHelper.COLUMN_ID+" DESC LIMIT 10");

            String[] fromColumns = {ClinicalTrialsSQLiteHelper.COLUMN_STRING};
            int[] toViews = {R.id.clinicaltrials_list_item_string};

            return new SimpleCursorAdapter(mContext, R.layout.activity_clinicaltrials_list_item, mCursor, fromColumns, toViews, 0);
        }
    }
}