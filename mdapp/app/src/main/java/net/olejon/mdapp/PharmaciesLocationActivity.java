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
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.melnykov.fab.FloatingActionButton;

public class PharmaciesLocationActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private InputMethodManager mInputMethodManager;

    private LinearLayout mToolbarSearchLayout;
    private EditText mToolbarSearchEditText;
    private FloatingActionButton mFloatingActionButton;
    private RecyclerView mRecyclerView;

    private String mMunicipalityName;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Intent
        final Intent intent = getIntent();

        mMunicipalityName = intent.getStringExtra("name");

        // Input manager
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Layout
        setContentView(R.layout.activity_pharmacies_location);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.pharmacies_location_toolbar);
        toolbar.setTitle(mMunicipalityName);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbarSearchLayout = (LinearLayout) findViewById(R.id.pharmacies_location_toolbar_search_layout);
        mToolbarSearchEditText = (EditText) findViewById(R.id.pharmacies_location_toolbar_search);

        ImageButton imageButton = (ImageButton) findViewById(R.id.pharmacies_location_toolbar_clear_search);

        imageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mToolbarSearchEditText.setText("");
            }
        });

        // Floating action button
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.pharmacies_location_fab);

        mFloatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mToolbarSearchLayout.setVisibility(View.VISIBLE);
                mToolbarSearchEditText.requestFocus();

                mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.pharmacies_location_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new PharmaciesLocationAdapter(mContext, mCursor));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Get pharmacies
        GetPharmaciesTask getPharmaciesTask = new GetPharmaciesTask();
        getPharmaciesTask.execute();
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Get pharmacies
    private class GetPharmaciesTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPostExecute(Void success)
        {
            if(mTools.isTablet())
            {
                int spanCount = (mCursor.getCount() == 1) ? 1 : 2;

                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
            }

            mRecyclerView.setAdapter(new PharmaciesLocationAdapter(mContext, mCursor));

            if(mCursor.getCount() >= 4)
            {
                mToolbarSearchEditText.addTextChangedListener(new TextWatcher()
                {
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
                    {
                        String searchString = charSequence.toString().trim();

                        for(int n = 0; n < mCursor.getCount(); n++)
                        {
                            if(mCursor.moveToPosition(n))
                            {
                                String pharmacyName = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.PHARMACIES_COLUMN_NAME));

                                if(pharmacyName.matches("(?i).*?"+searchString+".*"))
                                {
                                    mRecyclerView.scrollToPosition(n);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                    @Override
                    public void afterTextChanged(Editable editable) { }
                });

                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fab);

                mFloatingActionButton.startAnimation(animation);
                mFloatingActionButton.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Void doInBackground(Void... voids)
        {
            mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();

            String[] queryColumns = {SlDataSQLiteHelper.PHARMACIES_COLUMN_NAME, SlDataSQLiteHelper.PHARMACIES_COLUMN_ADDRESS};
            mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_PHARMACIES, queryColumns, SlDataSQLiteHelper.PHARMACIES_COLUMN_MUNICIPALITY+" = "+mTools.sqe(mMunicipalityName), null, null, null, null);

            return null;
        }
    }
}