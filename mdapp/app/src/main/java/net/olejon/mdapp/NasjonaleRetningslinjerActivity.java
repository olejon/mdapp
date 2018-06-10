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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
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
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class NasjonaleRetningslinjerActivity extends AppCompatActivity
{
	private static final int VOICE_SEARCH_REQUEST_CODE = 1;

	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	private InputMethodManager mInputMethodManager;

	private ProgressBar mProgressBar;
	private EditText mToolbarSearchEditText;
	private FloatingActionButton mFloatingActionButton;
	private ListView mListView;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Input manager
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_nasjonale_retningslinjer);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.nasjonale_retningslinjer_toolbar);
		toolbar.setTitle(getString(R.string.nasjonale_retningslinjer_title));

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarSearchEditText = findViewById(R.id.nasjonale_retningslinjer_toolbar_search);

		mToolbarSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				if(i == EditorInfo.IME_ACTION_SEARCH || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
				{
					mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);

					search(mToolbarSearchEditText.getText().toString().trim());

					return true;
				}

				return false;
			}
		});

		// Progress bar
		mProgressBar = findViewById(R.id.nasjonale_retningslinjer_toolbar_progressbar);

		// Floating action button
		mFloatingActionButton = findViewById(R.id.nasjonale_retningslinjer_fab);

		mFloatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(mToolbarSearchEditText.getVisibility() == View.VISIBLE)
				{
					mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);

					search(mToolbarSearchEditText.getText().toString().trim());
				}
				else
				{
					mToolbarSearchEditText.setVisibility(View.VISIBLE);
					mToolbarSearchEditText.requestFocus();

					mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
				}
			}
		});

		// List
		mListView = findViewById(R.id.nasjonale_retningslinjer_list);

		View listViewEmpty = findViewById(R.id.nasjonale_retningslinjer_list_empty);
		mListView.setEmptyView(listViewEmpty);

		View listViewHeader = getLayoutInflater().inflate(R.layout.activity_nasjonale_retningslinjer_list_subheader, mListView, false);
		mListView.addHeaderView(listViewHeader, null, false);
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

			search(voiceSearchString);
		}
	}

	// Resume activity
	@Override
	protected void onResume()
	{
		super.onResume();

		getRecentSearches();
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
		getMenuInflater().inflate(R.menu.menu_nasjonale_retningslinjer, menu);
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
			case R.id.nasjonale_retningslinjer_menu_voice_search:
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
			case R.id.nasjonale_retningslinjer_menu_clear_recent_searches:
			{
				clearRecentSearches();
				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	// Search
	private void search(String originalSearchString)
	{
		if(!mTools.isDeviceConnected())
		{
			mTools.showToast(getString(R.string.device_not_connected), 1);
		}

		if(originalSearchString.equals("")) return;

		final String searchString = mTools.firstToUpper(originalSearchString);

		mToolbarSearchEditText.setVisibility(View.GONE);
		mToolbarSearchEditText.setText("");
		mProgressBar.setVisibility(View.VISIBLE);

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

					mProgressBar.setVisibility(View.GONE);

					try
					{
						final String correctSearchString = response.getString("correct");

						if(correctSearchString.equals(""))
						{
							saveRecentSearch(searchString);

							try
							{
								Intent intent = new Intent(mContext, MainWebViewActivity.class);
								intent.putExtra("title", getString(R.string.nasjonale_retningslinjer_title));
								intent.putExtra("uri", "https://helsedirektoratet.no/retningslinjer#k="+URLEncoder.encode(searchString, "utf-8"));
								startActivity(intent);
							}
							catch(Exception e)
							{
								Log.e("NasjonaleRetningslinjer", Log.getStackTraceString(e));
							}
						}
						else
						{
							new MaterialDialog.Builder(mContext).title(R.string.correct_dialog_title).content(getString(R.string.correct_dialog_message, correctSearchString)).positiveText(R.string.correct_dialog_positive_button).negativeText(R.string.correct_dialog_negative_button).onPositive(new MaterialDialog.SingleButtonCallback()
							{
								@Override
								public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
								{
									saveRecentSearch(correctSearchString);

									try
									{
										Intent intent = new Intent(mContext, MainWebViewActivity.class);
										intent.putExtra("title", getString(R.string.nasjonale_retningslinjer_title));
										intent.putExtra("uri", "https://helsedirektoratet.no/retningslinjer#k="+URLEncoder.encode(correctSearchString, "utf-8"));
										startActivity(intent);
									}
									catch(Exception e)
									{
										Log.e("NasjonaleRetningslinjer", Log.getStackTraceString(e));
									}
								}
							}).onNegative(new MaterialDialog.SingleButtonCallback()
							{
								@Override
								public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
								{
									saveRecentSearch(searchString);

									try
									{
										Intent intent = new Intent(mContext, MainWebViewActivity.class);
										intent.putExtra("title", getString(R.string.nasjonale_retningslinjer_title));
										intent.putExtra("uri", "https://helsedirektoratet.no/retningslinjer#k="+URLEncoder.encode(searchString, "utf-8"));
										startActivity(intent);
									}
									catch(Exception e)
									{
										Log.e("NasjonaleRetningslinjer", Log.getStackTraceString(e));
									}
								}
							}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
						}
					}
					catch(Exception e)
					{
						Log.e("NasjonaleRetningslinjer", Log.getStackTraceString(e));
					}
				}
			}, new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{
					requestQueue.stop();

					mProgressBar.setVisibility(View.GONE);

					Log.e("NasjonaleRetningslinjer", error.toString());
				}
			});

			jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			requestQueue.add(jsonObjectRequest);
		}
		catch(Exception e)
		{
			Log.e("NasjonaleRetningslinjer", Log.getStackTraceString(e));
		}
	}

	private void saveRecentSearch(String searchString)
	{
		ContentValues contentValues = new ContentValues();
		contentValues.put(NasjonaleRetningslinjerSQLiteHelper.COLUMN_STRING, searchString);

		mSqLiteDatabase.delete(NasjonaleRetningslinjerSQLiteHelper.TABLE, NasjonaleRetningslinjerSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(searchString)+" COLLATE NOCASE", null);
		mSqLiteDatabase.insert(NasjonaleRetningslinjerSQLiteHelper.TABLE, null, contentValues);
	}

	private void clearRecentSearches()
	{
		mSqLiteDatabase.delete(NasjonaleRetningslinjerSQLiteHelper.TABLE, null, null);

		mTools.showToast(getString(R.string.nasjonale_retningslinjer_recent_searches_removed), 0);

		getRecentSearches();
	}

	private void getRecentSearches()
	{
		mSqLiteDatabase = new NasjonaleRetningslinjerSQLiteHelper(mContext).getWritableDatabase();
		mCursor = mSqLiteDatabase.query(NasjonaleRetningslinjerSQLiteHelper.TABLE, null, null, null, null, null, NasjonaleRetningslinjerSQLiteHelper.COLUMN_ID+" DESC LIMIT 10");

		String[] fromColumns = {NasjonaleRetningslinjerSQLiteHelper.COLUMN_STRING};
		int[] toViews = {R.id.nasjonale_retningslinjer_list_item_string};

		mListView.setAdapter(new SimpleCursorAdapter(mContext, R.layout.activity_nasjonale_retningslinjer_list_item, mCursor, fromColumns, toViews, 0));

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				int index = i - 1;

				if(mCursor.moveToPosition(index))
				{
					search(mCursor.getString(mCursor.getColumnIndexOrThrow(NasjonaleRetningslinjerSQLiteHelper.COLUMN_STRING)));
				}
			}
		});

		mFloatingActionButton.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fab));
		mFloatingActionButton.setVisibility(View.VISIBLE);
	}
}