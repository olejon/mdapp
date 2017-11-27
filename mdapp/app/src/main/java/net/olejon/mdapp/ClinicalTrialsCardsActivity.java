package net.olejon.mdapp;

/*

Copyright 2017 Ole Jon Bjørkum

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

public class ClinicalTrialsCardsActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private ProgressBar mProgressBar;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private RecyclerView mRecyclerView;
	private LinearLayout mNoClinicalTrialsLayout;

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
		setContentView(R.layout.activity_clinicaltrials_cards);

		// Toolbar
		final Toolbar toolbar = findViewById(R.id.clinicaltrials_cards_toolbar);
		toolbar.setTitle(getString(R.string.clinicaltrials_cards_search, searchString));

		TextView toolbarTextView = (TextView) toolbar.getChildAt(1);
		toolbarTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Progress bar
		mProgressBar = findViewById(R.id.clinicaltrials_cards_toolbar_progressbar);
		mProgressBar.setVisibility(View.VISIBLE);

		// Refresh
		mSwipeRefreshLayout = findViewById(R.id.clinicaltrials_cards_swipe_refresh_layout);
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
		mRecyclerView = findViewById(R.id.clinicaltrials_cards_cards);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(new ClinicalTrialsCardsAdapter(new JSONArray()));
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

		// No clinical trials
		mNoClinicalTrialsLayout = findViewById(R.id.clinicaltrials_cards_no_clinicaltrials);

		Button noClinicalTrialsButton = findViewById(R.id.clinicaltrials_cards_no_results_button);

		noClinicalTrialsButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				try
				{
					Intent intent = new Intent(mContext, MainWebViewActivity.class);
					intent.putExtra("title", getString(R.string.clinicaltrials_cards_search, searchString));
					intent.putExtra("uri", "https://clinicaltrials.gov/ct2/results?cond="+URLEncoder.encode(searchString, "utf-8")+"&recrs=b&recrs=a&recrs=f&recrs=d&recrs=e&recrs=c&recrs=l");

					mContext.startActivity(intent);
				}
				catch(Exception e)
				{
					Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
				}
			}
		});

		// Search
		search(searchString);

		// Correct
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
									contentValues.put(ClinicalTrialsSQLiteHelper.COLUMN_STRING, correctSearchString);

									SQLiteDatabase sqLiteDatabase = new ClinicalTrialsSQLiteHelper(mContext).getWritableDatabase();

									sqLiteDatabase.delete(ClinicalTrialsSQLiteHelper.TABLE, ClinicalTrialsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(searchString)+" COLLATE NOCASE", null);
									sqLiteDatabase.insert(ClinicalTrialsSQLiteHelper.TABLE, null, contentValues);

									sqLiteDatabase.close();

									toolbar.setTitle(getString(R.string.clinicaltrials_cards_search, correctSearchString));

									mProgressBar.setVisibility(View.VISIBLE);

									mNoClinicalTrialsLayout.setVisibility(View.GONE);
									mSwipeRefreshLayout.setVisibility(View.VISIBLE);

									search(correctSearchString);
								}
							}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
						}
					}
					catch(Exception e)
					{
						Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
					}
				}
			}, new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{
					requestQueue.stop();

					Log.e("ClinicalTrialsCards", error.toString());
				}
			});

			jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			requestQueue.add(jsonObjectRequest);
		}
		catch(Exception e)
		{
			Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
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

			JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(mTools.getApiUri()+"api/1/clinicaltrials/?search="+URLEncoder.encode(searchString, "utf-8"), new Response.Listener<JSONArray>()
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
						mNoClinicalTrialsLayout.setVisibility(View.VISIBLE);
					}
					else
					{
						if(mTools.isTablet())
						{
							int spanCount = (response.length() == 1) ? 1 : 2;
							mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
						}

						mRecyclerView.setAdapter(new ClinicalTrialsCardsAdapter(response));

						ContentValues contentValues = new ContentValues();
						contentValues.put(ClinicalTrialsSQLiteHelper.COLUMN_STRING, searchString);

						SQLiteDatabase sqLiteDatabase = new ClinicalTrialsSQLiteHelper(mContext).getWritableDatabase();

						sqLiteDatabase.delete(ClinicalTrialsSQLiteHelper.TABLE, ClinicalTrialsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(searchString)+" COLLATE NOCASE", null);
						sqLiteDatabase.insert(ClinicalTrialsSQLiteHelper.TABLE, null, contentValues);

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

					mTools.showToast(getString(R.string.clinicaltrials_cards_something_went_wrong), 1);

					Log.e("ClinicalTrialsCards", error.toString());

					finish();
				}
			});

			jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			requestQueue.add(jsonArrayRequest);
		}
		catch(Exception e)
		{
			Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
		}
	}

	// Adapter
	class ClinicalTrialsCardsAdapter extends RecyclerView.Adapter<ClinicalTrialsCardsAdapter.ClinicalTrialsViewHolder>
	{
		final JSONArray mClinicalTrials;

		int mLastPosition = - 1;

		ClinicalTrialsCardsAdapter(JSONArray jsonArray)
		{
			mClinicalTrials = jsonArray;
		}

		class ClinicalTrialsViewHolder extends RecyclerView.ViewHolder
		{
			final CardView card;
			final TextView title;
			final TextView status;
			final TextView conditions;
			final TextView intervention;
			final Button uri;

			ClinicalTrialsViewHolder(View view)
			{
				super(view);

				card = view.findViewById(R.id.clinicaltrials_cards_card);
				title = view.findViewById(R.id.clinicaltrials_cards_card_title);
				status = view.findViewById(R.id.clinicaltrials_cards_card_status);
				conditions = view.findViewById(R.id.clinicaltrials_cards_card_conditions);
				intervention = view.findViewById(R.id.clinicaltrials_cards_card_intervention);
				uri = view.findViewById(R.id.clinicaltrials_cards_card_button_uri);
			}
		}

		@Override
		public ClinicalTrialsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
		{
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_clinicaltrials_card, viewGroup, false);
			return new ClinicalTrialsViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ClinicalTrialsViewHolder viewHolder, int i)
		{
			try
			{
				JSONObject clinicalTrialsJsonObject = mClinicalTrials.getJSONObject(i);

				final String title = clinicalTrialsJsonObject.getString("study");
				final String uri = clinicalTrialsJsonObject.getString("uri");

				String status = clinicalTrialsJsonObject.getString("status");
				String conditions = clinicalTrialsJsonObject.getString("conditions");
				String intervention = clinicalTrialsJsonObject.getString("intervention");

				viewHolder.title.setText(title);
				viewHolder.status.setText(status);
				viewHolder.conditions.setText(conditions);
				viewHolder.intervention.setText(intervention);

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
				Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
			}
		}

		@Override
		public int getItemCount()
		{
			return mClinicalTrials.length();
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