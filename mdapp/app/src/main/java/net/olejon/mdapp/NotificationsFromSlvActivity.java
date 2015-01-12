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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
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

public class NotificationsFromSlvActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private NotificationManagerCompat mNotificationManagerCompat;

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Settings
        PreferenceManager.setDefaultValues(mContext, R.xml.settings, false);

        // Notification manager
        mNotificationManagerCompat = NotificationManagerCompat.from(mContext);

        // Connected?
        if(!mTools.isDeviceConnected())
        {
            mTools.showToast(getString(R.string.device_not_connected), 1);

            finish();

            return;
        }

        // Layout
        setContentView(R.layout.activity_notifications_from_slv);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.notifications_from_slv_toolbar);
        toolbar.setTitle(getString(R.string.notifications_from_slv_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.notifications_from_slv_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.notifications_from_slv_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_green, R.color.accent_purple, R.color.accent_orange);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getNotifications(false);
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.notifications_from_slv_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new NotificationsFromSlvAdapter(mContext, new JSONArray()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Get notifications
        getNotifications(true);
    }

    @Override
    protected void onPostResume()
    {
        super.onPostResume();

        mNotificationManagerCompat.cancel(NotificationsFromSlvIntentService.NOTIFICATION_ID);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_notifications_from_slv, menu);
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
            case R.id.notifications_from_slv_menu_uri:
            {
                mTools.openUri("http://www.felleskatalogen.no/medisin/varsel");
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Get notifications
    private void getNotifications(boolean cache)
    {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        String apiUri = getString(R.string.project_website)+"api/1/notifications-from-slv/";

        if(!cache) requestQueue.getCache().remove(apiUri);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(apiUri, new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                if(mTools.isTablet()) mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

                mRecyclerView.setAdapter(new NotificationsFromSlvAdapter(mContext, response));
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                mTools.showToast(getString(R.string.notifications_from_slv_could_not_get_notifications), 1);

                finish();
            }
        });

        requestQueue.add(jsonArrayRequest);
    }
}