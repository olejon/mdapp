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
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.afollestad.materialdialogs.MaterialDialog;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Icd10ChapterActivity extends AppCompatActivity
{
	private static final int VOICE_SEARCH_REQUEST_CODE = 1;

	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	private InputMethodManager mInputMethodManager;

	private EditText mToolbarSearchEditText;
	private ListView mListView;

	private String mTitle;

	private JSONArray mData;

	private ArrayList<String> mCodesArrayList;
	private ArrayList<String> mNamesArrayList;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Intent
		Intent intent = getIntent();

		long chapterId = intent.getLongExtra("chapter", 0);

		// Input manager
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_icd10_chapter);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.icd10_chapter_toolbar);
		toolbar.setTitle("");

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarSearchEditText = findViewById(R.id.icd10_chapter_toolbar_search);

		// Progress bar
		final ProgressBar progressBar = findViewById(R.id.icd10_chapter_toolbar_progressbar);

		FloatingActionButton floatingActionButton = findViewById(R.id.icd10_chapter_fab);

		floatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mToolbarSearchEditText.setVisibility(View.VISIBLE);
				mToolbarSearchEditText.requestFocus();

				mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
			}
		});

		// List
		mListView = findViewById(R.id.icd10_chapter_list);
		View listViewEmpty = findViewById(R.id.icd10_chapter_list_empty);

		// Get data
		mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
		String[] queryColumns = {SlDataSQLiteHelper.ICD_10_COLUMN_NAME, SlDataSQLiteHelper.ICD_10_COLUMN_DATA};
		mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ICD_10, queryColumns, SlDataSQLiteHelper.ICD_10_COLUMN_ID+" = "+chapterId, null, null, null, null);

		if(mCursor.moveToFirst())
		{
			try
			{
				mTitle = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ICD_10_COLUMN_NAME));
				mData = new JSONArray(mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ICD_10_COLUMN_DATA)));
			}
			catch(Exception e)
			{
				Log.e("Icd10ChapterActivity", Log.getStackTraceString(e));
			}
		}

		toolbar.setTitle(mTitle);

		mToolbarSearchEditText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
			{
				populateListView(charSequence.toString().trim());
			}

			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

			@Override
			public void afterTextChanged(Editable editable) { }
		});

		mListView.setEmptyView(listViewEmpty);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
			{
				if(mTools.isDeviceConnected())
				{
					mToolbarSearchEditText.setVisibility(View.GONE);

					progressBar.setVisibility(View.VISIBLE);

					try
					{
						final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

						requestQueue.start();

						JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mTools.getApiUri()+"api/1/icd-10/search/?code="+mCodesArrayList.get(i), null, new Response.Listener<JSONObject>()
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
									intent.putExtra("title", mNamesArrayList.get(i));
									intent.putExtra("uri", uri);
									startActivity(intent);
								}
								catch(Exception e)
								{
									Log.e("Icd10ChapterActivity", Log.getStackTraceString(e));
								}
							}
						}, new Response.ErrorListener()
						{
							@Override
							public void onErrorResponse(VolleyError error)
							{
								requestQueue.stop();

								progressBar.setVisibility(View.GONE);

								mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);

								mTools.showToast(getString(R.string.icd10_chapter_could_not_find_code), 1);

								Log.e("Icd10ChapterActivity", error.toString());
							}
						});

						jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

						requestQueue.add(jsonObjectRequest);
					}
					catch(Exception e)
					{
						Log.e("Icd10ChapterActivity", Log.getStackTraceString(e));
					}
				}
				else
				{
					mTools.showToast(getString(R.string.device_not_connected), 1);
				}
			}
		});

		populateListView(null);

		floatingActionButton.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fab));
		floatingActionButton.setVisibility(View.VISIBLE);
	}

	// Activity result
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == VOICE_SEARCH_REQUEST_CODE && data != null)
		{
			ArrayList<String> voiceSearchArrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			String voiceSearchString = voiceSearchArrayList.get(0);

			populateListView(voiceSearchString);
		}
	}

	// Pause activity
	@Override
	protected void onPause()
	{
		super.onPause();

		mToolbarSearchEditText.setVisibility(View.GONE);
		mToolbarSearchEditText.setText("");
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
		if(mToolbarSearchEditText.getVisibility() == View.VISIBLE)
		{
			mToolbarSearchEditText.setVisibility(View.GONE);
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
			mToolbarSearchEditText.setVisibility(View.VISIBLE);
			mToolbarSearchEditText.requestFocus();

			mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);

			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_icd10_chapter, menu);
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
			case R.id.icd10_chapter_menu_voice_search:
			{
				try
				{
					Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "nb-NO");
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
				}
				catch(Exception e)
				{
					new MaterialDialog.Builder(mContext).title(R.string.device_not_supported_dialog_title).content(getString(R.string.device_not_supported_dialog_message)).positiveText(R.string.device_not_supported_dialog_positive_button).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
				}

				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	// Populate list view
	private void populateListView(String searchString)
	{
		ArrayList<HashMap<String,String>> arrayList = new ArrayList<>();

		mCodesArrayList = new ArrayList<>();
		mNamesArrayList = new ArrayList<>();

		String[] fromColumns = new String[] {"code", "name"};
		int[] toViews = new int[] {R.id.icd10_chapter_list_item_code, R.id.icd10_chapter_list_item_name};

		try
		{
			for(int i = 0; i < mData.length(); i++)
			{
				HashMap<String,String> item = new HashMap<>();

				JSONObject jsonObject = mData.getJSONObject(i);

				String code = jsonObject.getString("code");
				String name = jsonObject.getString("name");

				if(searchString == null)
				{
					item.put("code", code);
					item.put("name", name);

					arrayList.add(item);

					mCodesArrayList.add(code);
					mNamesArrayList.add(name);
				}
				else if(code.matches("(?i).*?"+searchString+".*") || name.matches("(?i).*?"+searchString+".*"))
				{
					item.put("code", code);
					item.put("name", name);

					arrayList.add(item);

					mCodesArrayList.add(code);
					mNamesArrayList.add(name);
				}
			}
		}
		catch(Exception e)
		{
			Log.e("Icd10ChapterActivity", Log.getStackTraceString(e));
		}

		SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, arrayList, R.layout.activity_icd10_chapter_list_item, fromColumns, toViews);

		mListView.setAdapter(simpleAdapter);
	}
}