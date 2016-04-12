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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationsFromSlvActivity extends AppCompatActivity
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

        // Connected?
        if(!mTools.isDeviceConnected())
        {
            mTools.showToast(getString(R.string.device_not_connected), 1);

            finish();

            return;
        }

        // Notification manager
        mNotificationManagerCompat = NotificationManagerCompat.from(mContext);

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
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_purple, R.color.accent_teal);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getNotifications();
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.notifications_from_slv_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new NotificationsFromSlvAdapter(new JSONArray()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Get notifications
        getNotifications();
    }

    // Resume activity
    @Override
    protected void onResume()
    {
        super.onResume();

        mNotificationManagerCompat.cancel(NotificationsFromSlvIntentService.NOTIFICATION_ID);
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

    // Get notifications
    private void getNotifications()
    {
        final Cache cache = new DiskBasedCache(getCacheDir(), 0);

        final Network network = new BasicNetwork(new HurlStack());

        final RequestQueue requestQueue = new RequestQueue(cache, network);

        requestQueue.start();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(getString(R.string.project_website_uri)+"api/1/notifications-from-slv/", new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                requestQueue.stop();

                if(response.length() == 0)
                {
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    mTools.showToast(getString(R.string.notifications_from_slv_could_not_get_notifications), 1);

                    finish();
                }
                else
                {
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    if(mTools.isTablet()) mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

                    mRecyclerView.setAdapter(new NotificationsFromSlvAdapter(response));
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                requestQueue.stop();

                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                mTools.showToast(getString(R.string.notifications_from_slv_could_not_get_notifications), 1);

                finish();
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonArrayRequest);
    }

    // Adapter
    private class NotificationsFromSlvAdapter extends RecyclerView.Adapter<NotificationsFromSlvAdapter.NotificationViewHolder>
    {
        private final JSONArray mNotifications;

        private int mLastPosition = -1;

        private NotificationsFromSlvAdapter(JSONArray jsonArray)
        {
            mNotifications = jsonArray;
        }

        class NotificationViewHolder extends RecyclerView.ViewHolder
        {
            private final CardView card;
            private final TextView title;
            private final TextView date;
            private final TextView type;
            private final TextView message;
            private final View uriSeparator;
            private final TextView uri;

            public NotificationViewHolder(View view)
            {
                super(view);

                card = (CardView) view.findViewById(R.id.notifications_from_slv_card);
                title = (TextView) view.findViewById(R.id.notifications_from_slv_card_title);
                date = (TextView) view.findViewById(R.id.notifications_from_slv_card_date);
                type = (TextView) view.findViewById(R.id.notifications_from_slv_card_type);
                message = (TextView) view.findViewById(R.id.notifications_from_slv_card_message);
                uriSeparator = view.findViewById(R.id.notifications_from_slv_card_uri_separator);
                uri = (TextView) view.findViewById(R.id.notifications_from_slv_card_button);
            }
        }

        @Override
        public NotificationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_notifications_from_slv_card, viewGroup, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NotificationViewHolder viewHolder, int i)
        {
            try
            {
                final JSONObject notificationsJsonObject = mNotifications.getJSONObject(i);

                viewHolder.title.setText(notificationsJsonObject.getString("title"));
                viewHolder.date.setText(notificationsJsonObject.getString("date"));
                viewHolder.type.setText(notificationsJsonObject.getString("type"));
                viewHolder.message.setText(notificationsJsonObject.getString("message"));

                final String title = notificationsJsonObject.getString("title");
                final String uri = notificationsJsonObject.getString("uri");

                if(uri.equals(""))
                {
                    viewHolder.uriSeparator.setVisibility(View.GONE);
                    viewHolder.uri.setVisibility(View.GONE);
                }
                else
                {
                    viewHolder.uriSeparator.setVisibility(View.VISIBLE);
                    viewHolder.uri.setVisibility(View.VISIBLE);

                    viewHolder.title.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            if(uri.matches("^https?://.*?\\.pdf$"))
                            {
                                mTools.downloadFile(title, uri);
                            }
                            else
                            {
                                Intent intent = new Intent(mContext, MainWebViewActivity.class);
                                intent.putExtra("title", title);
                                intent.putExtra("uri", uri);
                                mContext.startActivity(intent);
                            }
                        }
                    });

                    viewHolder.uri.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            if(uri.matches("^https?://.*?\\.pdf$"))
                            {
                                mTools.downloadFile(title, uri);
                            }
                            else
                            {
                                Intent intent = new Intent(mContext, MainWebViewActivity.class);
                                intent.putExtra("title", title);
                                intent.putExtra("uri", uri);
                                mContext.startActivity(intent);
                            }
                        }
                    });
                }

                animateCard(viewHolder.card, i);
            }
            catch(Exception e)
            {
                Log.e("NotificationsFromSlv", Log.getStackTraceString(e));
            }
        }

        @Override
        public int getItemCount()
        {
            return mNotifications.length();
        }

        private void animateCard(View view, int position)
        {
            if(position > mLastPosition)
            {
                mLastPosition = position;

                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.card);
                view.startAnimation(animation);
            }
        }
    }
}