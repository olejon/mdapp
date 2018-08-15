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
import android.speech.RecognizerIntent;
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

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

public class Icd10Activity extends AppCompatActivity
{
	private static final int VOICE_SEARCH_REQUEST_CODE = 1;

	private final Context mContext = this;

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	private EditText mToolbarSearchEditText;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Input manager
		final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_icd10);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.icd10_toolbar);
		toolbar.setTitle(getString(R.string.icd10_title));

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarSearchEditText = findViewById(R.id.icd10_toolbar_search);

		mToolbarSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
			{
				if(i == EditorInfo.IME_ACTION_SEARCH)
				{
					search(mToolbarSearchEditText.getText().toString().trim());

					return true;
				}

				return false;
			}
		});

		// List
		ListView listView = findViewById(R.id.icd10_list);

		// Floating action button
		FloatingActionButton floatingActionButton = findViewById(R.id.icd10_fab);

		floatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(mToolbarSearchEditText.getVisibility() == View.VISIBLE)
				{
					search(mToolbarSearchEditText.getText().toString().trim());
				}
				else
				{
					mToolbarSearchEditText.setVisibility(View.VISIBLE);
					mToolbarSearchEditText.requestFocus();

					if(inputMethodManager != null) inputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
				}
			}
		});

		// Get chapters
		mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
		String[] queryColumns = {SlDataSQLiteHelper.ICD_10_COLUMN_ID, SlDataSQLiteHelper.ICD_10_COLUMN_CHAPTER, SlDataSQLiteHelper.ICD_10_COLUMN_CODES, SlDataSQLiteHelper.ICD_10_COLUMN_NAME};
		mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ICD_10, queryColumns, null, null, null, null, null);

		String[] fromColumns = {SlDataSQLiteHelper.ICD_10_COLUMN_CHAPTER, SlDataSQLiteHelper.ICD_10_COLUMN_CODES, SlDataSQLiteHelper.ICD_10_COLUMN_NAME};
		int[] toViews = {R.id.icd10_list_item_chapter, R.id.icd10_list_item_codes, R.id.icd10_list_item_name};

		listView.setAdapter(new SimpleCursorAdapter(mContext, R.layout.activity_icd10_list_item, mCursor, fromColumns, toViews, 0));

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long id)
			{
				mToolbarSearchEditText.setVisibility(View.GONE);
				mToolbarSearchEditText.setText("");

				if(mCursor.moveToPosition(i))
				{
					Intent intent = new Intent(mContext, Icd10ChapterActivity.class);
					intent.putExtra("chapter", id);
					startActivity(intent);
				}
			}
		});

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

			search(voiceSearchString);
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

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_icd10, menu);
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
			case R.id.icd10_menu_voice_search:
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
					new MaterialDialog.Builder(mContext).title(R.string.device_not_supported_dialog_title).content(getString(R.string.device_not_supported_dialog_message)).positiveText(R.string.device_not_supported_dialog_positive_button).titleColorRes(R.color.teal).contentColorRes(R.color.dark).positiveColorRes(R.color.teal).negativeColorRes(R.color.dark).neutralColorRes(R.color.teal).buttonRippleColorRes(R.color.light_grey).show();
				}

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

		Intent intent = new Intent(mContext, Icd10SearchActivity.class);
		intent.putExtra("search", searchString);
		startActivity(intent);
	}
}