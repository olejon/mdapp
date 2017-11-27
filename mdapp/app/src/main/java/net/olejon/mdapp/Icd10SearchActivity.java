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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Icd10SearchActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Intent
		Intent intent = getIntent();

		final String searchString = intent.getStringExtra("search");

		// Layout
		setContentView(R.layout.activity_icd10_search);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.icd10_search_toolbar);
		toolbar.setTitle(getString(R.string.icd10_search_search, searchString));

		TextView toolbarTextView = (TextView) toolbar.getChildAt(1);
		toolbarTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Progress bar
		final ProgressBar progressBar = findViewById(R.id.icd10_search_toolbar_progressbar);
		progressBar.setVisibility(View.VISIBLE);

		// List
		final ListView listView = findViewById(R.id.icd10_search_list);

		final View listViewEmpty = findViewById(R.id.icd10_search_list_empty);
		listViewEmpty.setVisibility(View.GONE);

		// Get search
		Thread getSearchDataThread = new Thread(new Runnable()
		{
			final ArrayList<HashMap<String,String>> itemsArrayList = new ArrayList<>();
			final ArrayList<String> codesArrayList = new ArrayList<>();
			final ArrayList<String> namesArrayList = new ArrayList<>();

			@Override
			public void run()
			{
				mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
				String[] queryColumns = {SlDataSQLiteHelper.ICD_10_COLUMN_DATA};
				mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ICD_10, queryColumns, null, null, null, null, null);

				if(mCursor.moveToFirst())
				{
					for(int i = 0; i < mCursor.getCount(); i++)
					{
						if(mCursor.moveToPosition(i))
						{
							try
							{
								JSONArray jsonArray = new JSONArray(mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ICD_10_COLUMN_DATA)));

								for(int n = 0; n < jsonArray.length(); n++)
								{
									HashMap<String,String> item = new HashMap<>();

									JSONObject jsonObject = jsonArray.getJSONObject(n);

									String code = jsonObject.getString("code");
									String name = jsonObject.getString("name");

									if(code.matches("(?i).*?"+searchString+".*") || name.matches("(?i).*?"+searchString+".*"))
									{
										item.put("code", code);
										item.put("name", name);

										itemsArrayList.add(item);
										codesArrayList.add(code);
										namesArrayList.add(name);
									}
								}
							}
							catch(JSONException e)
							{
								Log.e("Icd10SearchActivity", Log.getStackTraceString(e));
							}
						}
					}
				}

				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						progressBar.setVisibility(View.GONE);

						String[] fromColumns = new String[] {"code", "name"};
						int[] toViews = new int[] {R.id.icd10_search_list_item_code, R.id.icd10_search_list_item_name};

						listView.setEmptyView(listViewEmpty);

						listView.setAdapter(new SimpleAdapter(mContext, itemsArrayList, R.layout.activity_icd10_search_list_item, fromColumns, toViews));

						listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
						{
							@Override
							public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
							{
								progressBar.setVisibility(View.VISIBLE);

								try
								{
									final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

									requestQueue.start();

									JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mTools.getApiUri()+"api/1/icd-10/search/?code="+codesArrayList.get(i), null, new Response.Listener<JSONObject>()
									{
										@Override
										public void onResponse(JSONObject response)
										{
											requestQueue.stop();

											try
											{
												progressBar.setVisibility(View.GONE);

												String uri = response.getString("uri");

												Intent intent = new Intent(mContext, MainWebViewActivity.class);
												intent.putExtra("title", namesArrayList.get(i));
												intent.putExtra("uri", uri);
												startActivity(intent);
											}
											catch(Exception e)
											{
												progressBar.setVisibility(View.GONE);

												mTools.showToast(getString(R.string.icd10_search_could_not_find_code), 1);

												Log.e("Icd10SearchActivity", Log.getStackTraceString(e));
											}
										}
									}, new Response.ErrorListener()
									{
										@Override
										public void onErrorResponse(VolleyError error)
										{
											requestQueue.stop();

											progressBar.setVisibility(View.GONE);

											mTools.showToast(getString(R.string.icd10_search_could_not_find_code), 1);

											Log.e("Icd10SearchActivity", error.toString());
										}
									});

									jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

									requestQueue.add(jsonObjectRequest);
								}
								catch(Exception e)
								{
									Log.e("Icd10SearchActivity", Log.getStackTraceString(e));
								}
							}
						});
					}
				});
			}
		});

		getSearchDataThread.start();
	}

	// Destroy activity
	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if(mCursor != null && !mCursor.isClosed()) mCursor.close();
		if(mSqLiteDatabase != null && mSqLiteDatabase.isOpen()) mSqLiteDatabase.close();
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
}