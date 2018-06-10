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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InteractionsActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	private InputMethodManager mInputMethodManager;

	private EditText mToolbarSearchEditText;
	private FloatingActionButton mFloatingActionButton;
	private ListView mListView;

	private String mSearchString;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Intent
		Intent intent = getIntent();

		mSearchString = (intent.getStringExtra("search") == null) ? "" : intent.getStringExtra("search").replace(" ", "_");

		// Input manager
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_interactions);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.interactions_toolbar);
		toolbar.setTitle(getString(R.string.interactions_title));

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarSearchEditText = findViewById(R.id.interactions_toolbar_search);

		mToolbarSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				if(i == EditorInfo.IME_ACTION_SEARCH || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
				{
					mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);

					search(mToolbarSearchEditText.getText().toString());

					return true;
				}

				return false;
			}
		});

		if(!mSearchString.equals(""))
		{
			mToolbarSearchEditText.setVisibility(View.VISIBLE);
			mToolbarSearchEditText.setText(mSearchString+" ");
			mToolbarSearchEditText.setSelection(mToolbarSearchEditText.getText().length());

			mTools.showToast(getString(R.string.interactions_search_other_medications_or_substances), 1);
		}

		// List
		mListView = findViewById(R.id.interactions_list);

		View listViewEmpty = findViewById(R.id.interactions_list_empty);
		mListView.setEmptyView(listViewEmpty);

		View listViewHeader = getLayoutInflater().inflate(R.layout.activity_interactions_list_subheader, mListView, false);
		mListView.addHeaderView(listViewHeader, null, false);

		// Floating action button
		mFloatingActionButton = findViewById(R.id.interactions_fab);

		mFloatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(mToolbarSearchEditText.getVisibility() == View.VISIBLE)
				{
					mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);

					search(mToolbarSearchEditText.getText().toString());
				}
				else
				{
					mToolbarSearchEditText.setVisibility(View.VISIBLE);
					mToolbarSearchEditText.requestFocus();

					mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
				}
			}
		});
	}

	// Resume activity
	@Override
	protected void onResume()
	{
		super.onResume();

		getRecentSearches();
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
		getMenuInflater().inflate(R.menu.menu_interactions, menu);
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
			case R.id.interactions_menu_clear_recent_searches:
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
	private void search(String searchString)
	{
		if(searchString.equals("")) return;

		Pattern pattern = Pattern.compile("([A-Za-z][0-9]{2}[A-Za-z]\\s+[A-Za-z][0-9]{2})");
		Matcher matcher = pattern.matcher(searchString);

		while(matcher.find())
		{
			searchString = searchString.replace(matcher.group(0), "");

			searchString += " "+matcher.group(1).replace(" ", "")+" ";
		}

		searchString = searchString.replaceAll("\\s{2,}", " ").trim();

		Intent intent = new Intent(mContext, InteractionsCardsActivity.class);
		intent.putExtra("search", mTools.firstToUpper(searchString));
		startActivity(intent);
	}

	private void getRecentSearches()
	{
		mSqLiteDatabase = new InteractionsSQLiteHelper(mContext).getWritableDatabase();
		mCursor = mSqLiteDatabase.query(InteractionsSQLiteHelper.TABLE, null, null, null, null, null, InteractionsSQLiteHelper.COLUMN_ID+" DESC LIMIT 10");

		String[] fromColumns = {InteractionsSQLiteHelper.COLUMN_STRING};
		int[] toViews = {R.id.interactions_list_item_string};

		mListView.setAdapter(new SimpleCursorAdapter(mContext, R.layout.activity_interactions_list_item, mCursor, fromColumns, toViews, 0));

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				int index = i - 1;

				if(mCursor.moveToPosition(index))
				{
					search(mCursor.getString(mCursor.getColumnIndexOrThrow(InteractionsSQLiteHelper.COLUMN_STRING)));
				}
			}
		});

		mFloatingActionButton.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fab));
		mFloatingActionButton.setVisibility(View.VISIBLE);

		if(!mSearchString.equals(""))
		{
			Handler handler = new Handler();

			handler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					mToolbarSearchEditText.setVisibility(View.VISIBLE);
					mToolbarSearchEditText.requestFocus();

					mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
				}
			}, 500);
		}
	}

	private void clearRecentSearches()
	{
		mInputMethodManager.hideSoftInputFromWindow(mToolbarSearchEditText.getWindowToken(), 0);

		mSqLiteDatabase.delete(InteractionsSQLiteHelper.TABLE, null, null);

		mToolbarSearchEditText.setVisibility(View.GONE);
		mToolbarSearchEditText.setText("");

		mTools.showToast(getString(R.string.interactions_recent_searches_removed), 0);

		getRecentSearches();
	}
}