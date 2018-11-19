package net.olejon.mdapp;

/*

Copyright 2018 Ole Jon Bjørkum

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
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
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

		// Layout
		setContentView(R.layout.activity_notifications_from_slv);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.notifications_from_slv_toolbar);
		toolbar.setTitle(getString(R.string.notifications_from_slv_title));

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Progress bar
		mProgressBar = findViewById(R.id.notifications_from_slv_toolbar_progressbar);
		mProgressBar.setVisibility(View.VISIBLE);

		// Refresh
		mSwipeRefreshLayout = findViewById(R.id.notifications_from_slv_swipe_refresh_layout);
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
		mRecyclerView = findViewById(R.id.notifications_from_slv_cards);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(new NotificationsFromSlvAdapter(new JSONArray()));
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

		// Get notifications
		getNotifications();
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
				mTools.navigateUp(this);
				return true;
			}
			case R.id.notifications_from_slv_menu_all:
			{
				Intent intent = new Intent(mContext, MainWebViewActivity.class);
				intent.putExtra("title", getString(R.string.notifications_from_slv_menu_all));
				intent.putExtra("uri", "https://legemiddelverket.no/nyheter/");
				startActivity(intent);
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
		final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

		requestQueue.start();

		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(mTools.getApiUri()+"api/1/notifications-from-slv/", new Response.Listener<JSONArray>()
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

					if(mTools.isTablet())
					{
						int spanCount = (response.length() == 1) ? 1 : 2;
						mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
					}

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

				Log.e("NotificationsFromSlv", error.toString());

				finish();
			}
		});

		jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(jsonArrayRequest);
	}

	// Adapter
	class NotificationsFromSlvAdapter extends RecyclerView.Adapter<NotificationsFromSlvAdapter.NotificationsFromSlvViewHolder>
	{
		final JSONArray mNotificationsFromSlv;

		int mLastPosition = - 1;

		NotificationsFromSlvAdapter(JSONArray jsonArray)
		{
			mNotificationsFromSlv = jsonArray;
		}

		class NotificationsFromSlvViewHolder extends RecyclerView.ViewHolder
		{
			final CardView card;
			final TextView title;
			final TextView type;
			final TextView date;
			final TextView message;
			final Button uri;

			NotificationsFromSlvViewHolder(View view)
			{
				super(view);

				card = view.findViewById(R.id.notifications_from_slv_card);
				title = view.findViewById(R.id.notifications_from_slv_card_title);
				type = view.findViewById(R.id.notifications_from_slv_card_type);
				date = view.findViewById(R.id.notifications_from_slv_card_date);
				message = view.findViewById(R.id.notifications_from_slv_card_message);
				uri = view.findViewById(R.id.notifications_from_slv_card_button);
			}
		}

		@NonNull
		@Override
		public NotificationsFromSlvViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
		{
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_notifications_from_slv_card, viewGroup, false);
			return new NotificationsFromSlvViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull NotificationsFromSlvViewHolder viewHolder, int i)
		{
			try
			{
				JSONObject notificationsFromSlvJsonObject = mNotificationsFromSlv.getJSONObject(i);

				viewHolder.title.setText(notificationsFromSlvJsonObject.getString("title"));
				viewHolder.type.setText(notificationsFromSlvJsonObject.getString("type"));
				viewHolder.date.setText(notificationsFromSlvJsonObject.getString("date"));
				viewHolder.message.setText(notificationsFromSlvJsonObject.getString("message"));

				final String title = notificationsFromSlvJsonObject.getString("title");
				final String uri = notificationsFromSlvJsonObject.getString("uri");

				viewHolder.uri.setVisibility(View.VISIBLE);

				viewHolder.title.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", title);
						intent.putExtra("uri", uri);
						mContext.startActivity(intent);
					}
				});

				viewHolder.uri.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", title);
						intent.putExtra("uri", uri);
						mContext.startActivity(intent);
					}
				});

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
			return mNotificationsFromSlv.length();
		}

		private void animateCard(View view, int position)
		{
			if(position > mLastPosition)
			{
				mLastPosition = position;

				view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.card));
			}
		}
	}
}