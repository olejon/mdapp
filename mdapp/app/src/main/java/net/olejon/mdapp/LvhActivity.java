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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class LvhActivity extends AppCompatActivity
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

		// Settings
		PreferenceManager.setDefaultValues(mContext, R.xml.settings, false);

		// Connected?
		if(!mTools.isDeviceConnected())
		{
			mTools.showToast(getString(R.string.device_not_connected), 1);

			finish();

			return;
		}

		// Layout
		setContentView(R.layout.activity_lvh);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.lvh_toolbar);
		toolbar.setTitle(R.string.lvh_title);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Progress bar
		mProgressBar = findViewById(R.id.lvh_toolbar_progressbar);
		mProgressBar.setVisibility(View.VISIBLE);

		// Refresh
		mSwipeRefreshLayout = findViewById(R.id.lvh_swipe_refresh_layout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_purple, R.color.accent_teal);

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				getCategories();
			}
		});

		// Recycler view
		mRecyclerView = findViewById(R.id.lvh_cards);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(new LvhAdapter(new JSONArray()));
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

		// Get categories
		getCategories();
	}

	// Menu
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
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	// Get categories
	private void getCategories()
	{
		final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

		requestQueue.start();

		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(mTools.getApiUri()+"api/1/lvh/", new Response.Listener<JSONArray>()
		{
			@Override
			public void onResponse(JSONArray response)
			{
				requestQueue.stop();

				mProgressBar.setVisibility(View.GONE);
				mSwipeRefreshLayout.setRefreshing(false);

				if(mTools.isTablet())
				{
					int spanCount = (response.length() == 1) ? 1 : 2;
					mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
				}

				mRecyclerView.setAdapter(new LvhAdapter(response));
			}
		}, new Response.ErrorListener()
		{
			@Override
			public void onErrorResponse(VolleyError error)
			{
				requestQueue.stop();

				Log.e("LvhActivity", error.toString());
			}
		});

		jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		requestQueue.add(jsonArrayRequest);
	}

	// Adapter
	class LvhAdapter extends RecyclerView.Adapter<LvhAdapter.LvhCategoriesViewHolder>
	{
		final LayoutInflater mLayoutInflater;

		final JSONArray mLvhCategories;

		int mLastPosition = - 1;

		LvhAdapter(JSONArray jsonArray)
		{
			mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			mLvhCategories = jsonArray;
		}

		class LvhCategoriesViewHolder extends RecyclerView.ViewHolder
		{
			final CardView card;
			final ImageView icon;
			final TextView title;
			final LinearLayout categories;

			LvhCategoriesViewHolder(View view)
			{
				super(view);

				card = view.findViewById(R.id.lvh_card);
				icon = view.findViewById(R.id.lvh_card_icon);
				title = view.findViewById(R.id.lvh_card_title);
				categories = view.findViewById(R.id.lvh_card_categories);
			}
		}

		@NonNull
		@Override
		public LvhCategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
		{
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_lvh_card, viewGroup, false);
			return new LvhCategoriesViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull LvhCategoriesViewHolder viewHolder, int i)
		{
			try
			{
				final String color;
				final String icon;

				JSONObject lvhCategoryJsonObject = mLvhCategories.getJSONObject(i);

				switch(i)
				{
					case 0:
					{
						color = "#F44336";
						icon = "lvh_urgent";

						viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.red));
						viewHolder.icon.setImageResource(R.drawable.ic_favorite_white_24dp);

						break;
					}
					case 1:
					{
						color = "#9C27B0";
						icon = "lvh_symptoms";

						viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.purple));
						viewHolder.icon.setImageResource(R.drawable.stethoscope);

						break;
					}
					case 2:
					{
						color = "#FF9800";
						icon = "lvh_injuries";

						viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
						viewHolder.icon.setImageResource(R.drawable.ic_healing_white_24dp);

						break;
					}
					case 3:
					{
						color = "#009688";
						icon = "lvh_administrative";

						viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.teal));
						viewHolder.icon.setImageResource(R.drawable.ic_library_books_white_24dp);

						break;
					}
					default:
					{
						color = "#009688";
						icon = "lvh_administrative";

						viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.teal));
						viewHolder.icon.setImageResource(R.drawable.ic_library_books_white_24dp);
					}
				}

				viewHolder.title.setText(lvhCategoryJsonObject.getString("title"));

				viewHolder.categories.removeAllViews();

				JSONArray categoryJsonArray = lvhCategoryJsonObject.getJSONArray("categories");

				for(int f = 0; f < categoryJsonArray.length(); f++)
				{
					JSONObject categoryJsonObject = categoryJsonArray.getJSONObject(f);

					final String title = categoryJsonObject.getString("title");
					final String subCategories = categoryJsonObject.getString("subcategories");

					TextView textView = (TextView) mLayoutInflater.inflate(R.layout.activity_lvh_card_categories_item, null);
					textView.setText(categoryJsonObject.getString("title"));

					textView.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View view)
						{
							Intent intent = new Intent(mContext, LvhCategoriesActivity.class);
							intent.putExtra("color", color);
							intent.putExtra("icon", icon);
							intent.putExtra("title", title);
							intent.putExtra("subcategories", subCategories);
							mContext.startActivity(intent);
						}
					});

					viewHolder.categories.addView(textView);
				}

				animateCard(viewHolder.card, i);
			}
			catch(Exception e)
			{
				Log.e("LvhAdapter", Log.getStackTraceString(e));
			}
		}

		@Override
		public int getItemCount()
		{
			return mLvhCategories.length();
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