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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InteractionsActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private InputMethodManager mInputMethodManager;

    private LinearLayout mToolbarSearchLayout;
    private EditText mToolbarSearchEditText;
    private FloatingActionButton mFloatingActionButton;
    private ListView mListView;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Intent
        Intent intent = getIntent();

        final String searchString;

        if(intent.getStringExtra("search") == null)
        {
            searchString = "";
        }
        else
        {
            searchString = intent.getStringExtra("search");
        }

        // Layout
        setContentView(R.layout.activity_interactions);

        // Input manager
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.interactions_toolbar);
        toolbar.setTitle(getString(R.string.interactions_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbarSearchLayout = (LinearLayout) findViewById(R.id.interactions_toolbar_search_layout);
        mToolbarSearchEditText = (EditText) findViewById(R.id.interactions_toolbar_search);

        mToolbarSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_SEARCH || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    search(mToolbarSearchEditText.getText().toString());

                    return true;
                }

                return false;
            }
        });

        ImageButton imageButton = (ImageButton) findViewById(R.id.interactions_toolbar_clear_search);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mToolbarSearchEditText.setText("");
            }
        });

        if(!searchString.equals(""))
        {
            mToolbarSearchLayout.setVisibility(View.VISIBLE);
            mToolbarSearchEditText.setText(searchString+" ");
            mToolbarSearchEditText.setSelection(mToolbarSearchEditText.getText().length());

            mTools.showToast("Skriv eventuelt inn andre preparater å sjekke opp mot", 1);
        }

        // List
        mListView = (ListView) findViewById(R.id.interactions_list);

        View listViewEmpty = findViewById(R.id.interactions_list_empty);
        mListView.setEmptyView(listViewEmpty);

        View listViewHeader = getLayoutInflater().inflate(R.layout.activity_interactions_list_subheader, mListView, false);
        mListView.addHeaderView(listViewHeader, null, false);

        // Floating action button
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.interactions_fab);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
                {
                    mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    search(mToolbarSearchEditText.getText().toString());
                }
                else
                {
                    mToolbarSearchLayout.setVisibility(View.VISIBLE);
                    mToolbarSearchEditText.requestFocus();

                    mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
                }
            }
        });
    }

    // Resume activity
    @Override
    protected void onPostResume()
    {
        super.onPostResume();

        getRecentSearches();
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

            mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_interactions, menu);
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
                return true;
            }
            case R.id.interactions_clear_recent_searches:
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
    private void search(String string)
    {
        if(string.equals("")) return;

        Pattern pattern = Pattern.compile("([A-Za-z][0-9]{2}[A-Za-z]\\s+[A-Za-z][0-9]{2})");
        Matcher matcher = pattern.matcher(string);

        while(matcher.find())
        {
            string = string.replace(matcher.group(0), "");

            string += " "+matcher.group(1).replace(" ", "")+" ";
        }

        string = string.replaceAll("\\s{2,}", " ").trim();

        Intent intent = new Intent(mContext, InteractionsCardsActivity.class);
        intent.putExtra("search", string);
        startActivity(intent);
    }

    private void getRecentSearches()
    {
        GetRecentSearchesTask getRecentSearchesTask = new GetRecentSearchesTask();
        getRecentSearchesTask.execute();
    }

    private void clearRecentSearches()
    {
        mSqLiteDatabase.delete(InteractionsSQLiteHelper.TABLE, null, null);

        mTools.showToast(getString(R.string.interactions_recent_searches_removed), 0);

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

                    if(mCursor.moveToPosition(index)) search(mCursor.getString(mCursor.getColumnIndexOrThrow(InteractionsSQLiteHelper.COLUMN_STRING)));
                }
            });

            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fab);
            mFloatingActionButton.startAnimation(animation);

            mFloatingActionButton.setVisibility(View.VISIBLE);

            if(mCursor.getCount() > 0)
            {
                Handler handler = new Handler();

                handler.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mToolbarSearchLayout.setVisibility(View.VISIBLE);
                        mToolbarSearchEditText.requestFocus();

                        mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
                    }
                }, 1000);
            }

        }

        @Override
        protected SimpleCursorAdapter doInBackground(Void... voids)
        {
            mSqLiteDatabase = new InteractionsSQLiteHelper(mContext).getWritableDatabase();

            mCursor = mSqLiteDatabase.query(InteractionsSQLiteHelper.TABLE, null, null, null, null, null, InteractionsSQLiteHelper.COLUMN_ID+" DESC LIMIT 10");

            String[] fromColumns = {InteractionsSQLiteHelper.COLUMN_STRING};
            int[] toViews = {R.id.interactions_list_item_string};

            return new SimpleCursorAdapter(mContext, R.layout.activity_interactions_list_item, mCursor, fromColumns, toViews, 0);
        }
    }
}
