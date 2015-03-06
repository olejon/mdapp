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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;

import java.util.ArrayList;

public class PharmaciesLocationActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private InputMethodManager mInputMethodManager;

    private Toolbar mToolbar;
    private LinearLayout mToolbarSearchLayout;
    private EditText mToolbarSearchEditText;
    private FloatingActionButton mFloatingActionButton;
    private RecyclerView mRecyclerView;

    private JSONArray mPharmacies = new JSONArray();

    private final ArrayList<String> mPharmaciesNamesArrayList = new ArrayList<>();
    private final ArrayList<String> mPharmaciesCoordinatesArrayList = new ArrayList<>();

    private String mLocationName;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Intent
        Intent intent = getIntent();
        final long locationId = intent.getLongExtra("id", 0);

        // Input manager
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Layout
        setContentView(R.layout.activity_pharmacies_location);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.pharmacies_location_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbarSearchLayout = (LinearLayout) findViewById(R.id.pharmacies_location_toolbar_search_layout);
        mToolbarSearchEditText = (EditText) findViewById(R.id.pharmacies_location_toolbar_search);

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

                mInputMethodManager.toggleSoftInputFromWindow(mToolbarSearchEditText.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.pharmacies_location_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new PharmaciesLocationAdapter(mContext, mPharmacies));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Get pharmacies
        //GetPharmaciesTask getPharmaciesTask = new GetPharmaciesTask();
        //getPharmaciesTask.execute(locationId);
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
        getMenuInflater().inflate(R.menu.menu_pharmacies_location, menu);
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
            case R.id.pharmacies_menu_locations:
            {
                Intent intent = new Intent(mContext, PharmaciesLocationMapActivity.class);
                intent.putExtra("name", mLocationName);
                intent.putExtra("names", mPharmaciesNamesArrayList);
                intent.putExtra("coordinates", mPharmaciesCoordinatesArrayList);
                intent.putExtra("multiple_coordinates", true);
                mContext.startActivity(intent);
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Get pharmacies
    /*private class GetPharmaciesTask extends AsyncTask<Long, Void, Void>
    {
        @Override
        protected void onPostExecute(Void success)
        {
            if(mCursor.moveToFirst())
            {
                try
                {
                    mLocationName = mCursor.getString(mCursor.getColumnIndexOrThrow("location"));

                    mToolbar.setTitle(mLocationName);

                    mPharmacies = new JSONArray(mCursor.getString(mCursor.getColumnIndexOrThrow("details")));

                    int mPharmaciesLength = mPharmacies.length();

                    if(mTools.isTablet())
                    {
                        int spanCount = (mPharmaciesLength == 1) ? 1 : 2;

                        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
                    }

                    mRecyclerView.setAdapter(new PharmaciesLocationAdapter(mContext, mPharmacies));

                    for(int i = 0; i < mPharmaciesLength; i++)
                    {
                        JSONObject pharmacyJsonObject = mPharmacies.getJSONObject(i);

                        String name  = pharmacyJsonObject.getString("name");
                        String coordinates = pharmacyJsonObject.getString("coordinates");

                        mPharmaciesNamesArrayList.add(name);
                        mPharmaciesCoordinatesArrayList.add(coordinates);
                    }

                    mToolbarSearchEditText.addTextChangedListener(new TextWatcher()
                    {
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
                        {
                            String searchString = charSequence.toString().trim();

                            for(int n = 0; n < mPharmaciesNamesArrayList.size(); n++)
                            {
                                String name = mPharmaciesNamesArrayList.get(n);

                                if(name.matches("(?i).*?"+searchString+".*"))
                                {
                                    mRecyclerView.scrollToPosition(n);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                        @Override
                        public void afterTextChanged(Editable editable) { }
                    });

                    if(mPharmaciesLength >= 4)
                    {
                        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fab);
                        mFloatingActionButton.startAnimation(animation);

                        mFloatingActionButton.setVisibility(View.VISIBLE);
                    }
                }
                catch(Exception e)
                {
                    Log.e("PharmaciesLocation", Log.getStackTraceString(e));
                }
            }
        }

        @Override
        protected Void doInBackground(Long... longs)
        {
            mSqLiteDatabase = new FelleskatalogenSQLiteHelper(mContext).getReadableDatabase();

            mCursor = mSqLiteDatabase.query(FelleskatalogenSQLiteHelper.TABLE_PHARMACIES, null, FelleskatalogenSQLiteHelper.PHARMACIES_COLUMN_ID+" = "+longs[0], null, null, null, null);

            return null;
        }
    }*/
}