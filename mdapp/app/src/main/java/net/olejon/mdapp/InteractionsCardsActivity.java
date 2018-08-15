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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class InteractionsCardsActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private ProgressBar mProgressBar;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private RecyclerView mRecyclerView;
	private LinearLayout mNoInteractionsLayout;

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

		final String searchString = intent.getStringExtra("search");

		// Layout
		setContentView(R.layout.activity_interactions_cards);

		// Toolbar
		final Toolbar toolbar = findViewById(R.id.interactions_cards_toolbar);
		toolbar.setTitle(getString(R.string.interactions_cards_search, searchString));

		TextView toolbarTextView = (TextView) toolbar.getChildAt(1);
		toolbarTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Progress bar
		mProgressBar = findViewById(R.id.interactions_cards_toolbar_progressbar);
		mProgressBar.setVisibility(View.VISIBLE);

		// Refresh
		mSwipeRefreshLayout = findViewById(R.id.interactions_cards_swipe_refresh_layout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_purple, R.color.accent_teal);

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
		{
			@Override
			public void onRefresh()
			{
				search(searchString);
			}
		});

		// Recycler view
		mRecyclerView = findViewById(R.id.interactions_cards_cards);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(new InteractionsCardsAdapter(new JSONArray()));
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

		// No interactions
		mNoInteractionsLayout = findViewById(R.id.interactions_cards_no_interactions);

		Button noInteractionsButton = findViewById(R.id.interactions_cards_no_interactions_button);

		noInteractionsButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				try
				{
					Intent intent = new Intent(mContext, MainWebViewActivity.class);
					intent.putExtra("title", getString(R.string.interactions_cards_search, searchString));
					intent.putExtra("uri", "https://interaksjoner.azurewebsites.net/analyser.asp?PreparatNavn="+URLEncoder.encode(searchString, "utf-8"));
					mContext.startActivity(intent);
				}
				catch(Exception e)
				{
					Log.e("InteractionsCards", Log.getStackTraceString(e));
				}
			}
		});

		// Search
		search(searchString);

		// Correct
		if(!searchString.contains("_"))
		{
			try
			{
				final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

				requestQueue.start();

				JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mTools.getApiUri()+"api/1/correct/?search="+URLEncoder.encode(searchString, "utf-8"), null, new Response.Listener<JSONObject>()
				{
					@Override
					public void onResponse(JSONObject response)
					{
						requestQueue.stop();

						try
						{
							final String correctSearchString = response.getString("correct");

							if(!correctSearchString.equals(""))
							{
								mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
								{
									@Override
									public void onRefresh()
									{
										search(correctSearchString);
									}
								});

								new MaterialDialog.Builder(mContext).title(R.string.correct_dialog_title).content(getString(R.string.correct_dialog_message, correctSearchString)).positiveText(R.string.correct_dialog_positive_button).negativeText(R.string.correct_dialog_negative_button).onPositive(new MaterialDialog.SingleButtonCallback()
								{
									@Override
									public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
									{
										ContentValues contentValues = new ContentValues();
										contentValues.put(InteractionsSQLiteHelper.COLUMN_STRING, correctSearchString);

										SQLiteDatabase sqLiteDatabase = new InteractionsSQLiteHelper(mContext).getWritableDatabase();

										sqLiteDatabase.delete(InteractionsSQLiteHelper.TABLE, InteractionsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(searchString)+" COLLATE NOCASE", null);
										sqLiteDatabase.insert(InteractionsSQLiteHelper.TABLE, null, contentValues);

										sqLiteDatabase.close();

										toolbar.setTitle(getString(R.string.interactions_cards_search, correctSearchString));

										mProgressBar.setVisibility(View.VISIBLE);

										mNoInteractionsLayout.setVisibility(View.GONE);
										mSwipeRefreshLayout.setVisibility(View.VISIBLE);

										search(correctSearchString);
									}
								}).titleColorRes(R.color.teal).contentColorRes(R.color.dark).positiveColorRes(R.color.teal).negativeColorRes(R.color.dark).neutralColorRes(R.color.teal).buttonRippleColorRes(R.color.light_grey).show();
							}
						}
						catch(Exception e)
						{
							Log.e("InteractionsCards", Log.getStackTraceString(e));
						}
					}
				}, new Response.ErrorListener()
				{
					@Override
					public void onErrorResponse(VolleyError error)
					{
						requestQueue.stop();

						Log.e("InteractionsCards", error.toString());
					}
				});

				jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

				requestQueue.add(jsonObjectRequest);
			}
			catch(Exception e)
			{
				Log.e("InteractionsCards", Log.getStackTraceString(e));
			}
		}
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
	private void search(final String searchString)
	{
		try
		{
			final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

			requestQueue.start();

			JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(mTools.getApiUri()+"api/1/interactions/?search="+URLEncoder.encode(searchString, "utf-8"), new Response.Listener<JSONArray>()
			{
				@Override
				public void onResponse(JSONArray response)
				{
					requestQueue.stop();

					mProgressBar.setVisibility(View.GONE);
					mSwipeRefreshLayout.setRefreshing(false);

					if(response.length() == 0)
					{
						mSwipeRefreshLayout.setVisibility(View.GONE);
						mNoInteractionsLayout.setVisibility(View.VISIBLE);
					}
					else
					{
						if(mTools.isTablet())
						{
							int spanCount = (response.length() == 1) ? 1 : 2;
							mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
						}

						mRecyclerView.setAdapter(new InteractionsCardsAdapter(response));

						ContentValues contentValues = new ContentValues();
						contentValues.put(InteractionsSQLiteHelper.COLUMN_STRING, searchString);

						SQLiteDatabase sqLiteDatabase = new InteractionsSQLiteHelper(mContext).getWritableDatabase();

						sqLiteDatabase.delete(InteractionsSQLiteHelper.TABLE, InteractionsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(searchString)+" COLLATE NOCASE", null);
						sqLiteDatabase.insert(InteractionsSQLiteHelper.TABLE, null, contentValues);

						sqLiteDatabase.close();
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

					mTools.showToast(getString(R.string.interactions_cards_something_went_wrong), 1);

					Log.e("InteractionsCards", error.toString());

					finish();
				}
			});

			requestQueue.add(jsonArrayRequest);
		}
		catch(Exception e)
		{
			Log.e("InteractionsCards", Log.getStackTraceString(e));
		}
	}

	// Adapter
	class InteractionsCardsAdapter extends RecyclerView.Adapter<InteractionsCardsAdapter.InteractionsViewHolder>
	{
		final JSONArray mInteractions;

		int mLastPosition = - 1;

		InteractionsCardsAdapter(JSONArray jsonArray)
		{
			mInteractions = jsonArray;
		}

		class InteractionsViewHolder extends RecyclerView.ViewHolder
		{
			final CardView card;
			final ImageView icon;
			final TextView title;
			final TextView text;
			final TextView relevance;
			final Button handlingUri;
			final Button uri;
			final Button pubmedSearchUri;

			InteractionsViewHolder(View view)
			{
				super(view);

				card = view.findViewById(R.id.interactions_cards_card);
				icon = view.findViewById(R.id.interactions_cards_card_icon);
				title = view.findViewById(R.id.interactions_cards_card_title);
				text = view.findViewById(R.id.interactions_cards_card_text);
				relevance = view.findViewById(R.id.interactions_cards_card_relevance);
				handlingUri = view.findViewById(R.id.interactions_cards_card_button_handling);
				uri = view.findViewById(R.id.interactions_cards_card_button_uri);
				pubmedSearchUri = view.findViewById(R.id.interactions_cards_card_button_pubmed_search_uri);
			}
		}

		@NonNull
		@Override
		public InteractionsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
		{
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_interactions_card, viewGroup, false);
			return new InteractionsViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull InteractionsViewHolder viewHolder, int i)
		{
			try
			{
				JSONObject interactionJsonObject = mInteractions.getJSONObject(i);

				final String title = interactionJsonObject.getString("title");
				final String uri = interactionJsonObject.getString("uri");
				final String pubmedSearchUri = interactionJsonObject.getString("pubmed_search_uri");

				String color = interactionJsonObject.getString("color");
				String text = interactionJsonObject.getString("text");

				viewHolder.title.setText(title);
				viewHolder.text.setText(text);

				switch(color)
				{
					case "red":
					{
						viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.red));
						viewHolder.relevance.setText(mContext.getString(R.string.interactions_cards_relevance, mContext.getString(R.string.interactions_cards_relevance_red)));
						viewHolder.icon.setImageResource(R.drawable.ic_error_white_24dp);

						break;
					}
					case "orange":
					{
						viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
						viewHolder.relevance.setText(mContext.getString(R.string.interactions_cards_relevance, mContext.getString(R.string.interactions_cards_relevance_orange)));
						viewHolder.icon.setImageResource(R.drawable.ic_warning_white_24dp);

						break;
					}
					case "green":
					{
						viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.green));
						viewHolder.relevance.setText(mContext.getString(R.string.interactions_cards_relevance, mContext.getString(R.string.interactions_cards_relevance_green)));
						viewHolder.icon.setImageResource(R.drawable.ic_check_white_24dp);

						break;
					}
					default:
					{
						viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.red));
						viewHolder.relevance.setText(mContext.getString(R.string.interactions_cards_relevance, mContext.getString(R.string.interactions_cards_relevance_red)));
						viewHolder.icon.setImageResource(R.drawable.ic_error_white_24dp);

						break;
					}
				}

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

				viewHolder.handlingUri.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						if(mTools.isDeviceConnected())
						{
							try
							{
								mProgressBar.setVisibility(View.VISIBLE);

								final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

								requestQueue.start();

								JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mTools.getApiUri()+"api/1/interactions/handling/?uri="+URLEncoder.encode(uri, "utf-8"), null, new Response.Listener<JSONObject>()
								{
									@Override
									public void onResponse(JSONObject response)
									{
										requestQueue.stop();

										mProgressBar.setVisibility(View.GONE);

										try
										{
											String handling = response.getString("handling");

											new MaterialDialog.Builder(mContext).title(R.string.interactions_cards_handling_dialog_title).content(handling).positiveText(R.string.interactions_cards_handling_dialog_positive_button).titleColorRes(R.color.teal).contentColorRes(R.color.dark).positiveColorRes(R.color.teal).negativeColorRes(R.color.dark).neutralColorRes(R.color.teal).buttonRippleColorRes(R.color.light_grey).show();
										}
										catch(Exception e)
										{
											mTools.showToast(mContext.getString(R.string.interactions_cards_no_handling_information_available), 1);

											Log.e("InteractionsCards", Log.getStackTraceString(e));
										}
									}
								}, new Response.ErrorListener()
								{
									@Override
									public void onErrorResponse(VolleyError error)
									{
										requestQueue.stop();

										mProgressBar.setVisibility(View.GONE);

										mTools.showToast(mContext.getString(R.string.interactions_cards_no_handling_information_available), 1);

										Log.e("InteractionsCards", error.toString());
									}
								});

								jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

								requestQueue.add(jsonObjectRequest);
							}
							catch(Exception e)
							{
								Log.e("InteractionsCards", Log.getStackTraceString(e));
							}
						}
						else
						{
							mTools.showToast(mContext.getString(R.string.device_not_connected), 1);
						}
					}
				});

				viewHolder.pubmedSearchUri.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						Intent intent = new Intent(mContext, MainWebViewActivity.class);
						intent.putExtra("title", "PubMed");
						intent.putExtra("uri", pubmedSearchUri);
						mContext.startActivity(intent);
					}
				});

				animateCard(viewHolder.card, i);
			}
			catch(Exception e)
			{
				Log.e("InteractionsCards", Log.getStackTraceString(e));
			}
		}

		@Override
		public int getItemCount()
		{
			return mInteractions.length();
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