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
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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

public class MedicationPicturesActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private String picturesUri;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected()) mTools.showToast(getString(R.string.device_not_connected), 1);

        // Intent
        Intent intent = getIntent();

        final String medicationName = intent.getStringExtra("name");

        picturesUri = intent.getStringExtra("uri");

        // Layout
        setContentView(R.layout.activity_medication_pictures);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.medication_pictures_toolbar);
        toolbar.setTitle(medicationName);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.medication_pictures_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.lvh_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_green, R.color.accent_purple, R.color.accent_orange);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getPictures(false);
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.medication_pictures_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new MedicationPicturesAdapter(mContext, new JSONArray()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Get pictures
        getPictures(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_medication_pictures, menu);
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
            case R.id.medication_pictures_menu_uri:
            {
                mTools.openUri(picturesUri);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void getPictures(boolean cache)
    {
        try
        {
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            String apiUri = getString(R.string.project_website)+"api/1/felleskatalogen/medications/pictures/?uri="+URLEncoder.encode(picturesUri, "utf-8");

            if(!cache) requestQueue.getCache().remove(apiUri);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(apiUri, new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    if(mTools.isTablet())
                    {
                        int spanCount = (response.length() == 1) ? 1 : 2;

                        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
                    }

                    mRecyclerView.setAdapter(new MedicationPicturesAdapter(mContext, response));
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    mTools.showToast(getString(R.string.medication_pictures_could_not_load_pictures), 1);

                    finish();
                }
            });

            requestQueue.add(jsonArrayRequest);
        }
        catch(Exception e)
        {
            Log.e("MedicationPicturesActivity", Log.getStackTraceString(e));
        }
    }
}