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
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.net.URLEncoder;

public class DiseasesAndTreatmentsSearchActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected())
        {
            mTools.showToast(getString(R.string.device_not_connected), 1);

            finish();

            return;
        }

        // Intent
        Intent intent = getIntent();

        final String searchLanguage = intent.getStringExtra("language");
        final String searchString = intent.getStringExtra("string");

        // Layout
        setContentView(R.layout.activity_diseases_and_treatments_search);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.diseases_and_treatments_search_toolbar);
        toolbar.setTitle(getString(R.string.diseases_and_treatments_search_search)+": \""+searchString+"\"");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.diseases_and_treatments_search_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.diseases_and_treatments_search_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_green, R.color.accent_purple, R.color.accent_orange);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                search(searchLanguage, searchString, false);
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.diseases_and_treatments_search_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new DiseasesAndTreatmentsSearchAdapter(mContext, new JSONArray()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Search
        search(searchLanguage, searchString, true);
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

    // Search
    private void search(final String language, final String string, final boolean cache)
    {
        try
        {
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            String apiUri = getString(R.string.project_website)+"api/1/diseases-and-treatments/"+language+"/?search="+URLEncoder.encode(string.toLowerCase(), "utf-8");

            if(!cache) requestQueue.getCache().remove(apiUri);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(apiUri, new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    if(mTools.isTablet()) mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

                    mRecyclerView.setAdapter(new DiseasesAndTreatmentsSearchAdapter(mContext, response));

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DiseasesAndTreatmentsSQLiteHelper.COLUMN_STRING, string);

                    SQLiteDatabase sqLiteDatabase = new DiseasesAndTreatmentsSQLiteHelper(mContext).getWritableDatabase();

                    sqLiteDatabase.delete(DiseasesAndTreatmentsSQLiteHelper.TABLE, DiseasesAndTreatmentsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(string)+" COLLATE NOCASE", null);
                    sqLiteDatabase.insert(DiseasesAndTreatmentsSQLiteHelper.TABLE, null, contentValues);

                    sqLiteDatabase.close();
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    mTools.showToast(getString(R.string.diseases_and_treatments_search_could_not_search), 1);

                    finish();

                    Log.e("DiseasesAndTreatmentsSearchActivity", error.toString());
                }
            });

            requestQueue.add(jsonArrayRequest);
        }
        catch(Exception e)
        {
            Log.e("DiseasesAndTreatmentsSearchActivity", Log.getStackTraceString(e));
        }
    }
}
