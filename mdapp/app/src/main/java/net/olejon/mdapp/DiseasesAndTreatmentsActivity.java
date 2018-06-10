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
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
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

public class DiseasesAndTreatmentsActivity extends AppCompatActivity
{
	private static final int VOICE_SEARCH_REQUEST_CODE = 1;

	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	private InputMethodManager mInputMethodManager;

	private EditText mToolbarSearchEditText;
	private FloatingActionButton mFloatingActionButton;
	private ListView mListView;

	private String mSearchLanguage = "";

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Input manager
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_diseases_and_treatments);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.diseases_and_treatments_toolbar);
		toolbar.setTitle(getString(R.string.diseases_and_treatments_title));

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarSearchEditText = findViewById(R.id.diseases_and_treatments_toolbar_search);

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

		// List
		mListView = findViewById(R.id.diseases_and_treatments_list);

		View listViewEmpty = findViewById(R.id.diseases_and_treatments_list_empty);
		mListView.setEmptyView(listViewEmpty);

		View listViewHeader = getLayoutInflater().inflate(R.layout.activity_diseases_and_treatments_list_subheader, mListView, false);
		mListView.addHeaderView(listViewHeader, null, false);

		// Floating action buttons
		mFloatingActionButton = findViewById(R.id.diseases_and_treatments_fab);

		mFloatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				if(mToolbarSearchEditText.getVisibility() == View.VISIBLE)
				{
					String searchString = mToolbarSearchEditText.getText().toString().trim();

					if(searchString.equals(""))
					{
						showSearchLanguageDialog();
					}
					else
					{
						search(searchString);
					}
				}
				else
				{
					showSearchLanguageDialog();
				}
			}
		});

		Handler handler = new Handler();

		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				showSearchLanguageDialog();
			}
		}, 500);
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
			showSearchLanguageDialog();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_diseases_and_treatments, menu);
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
			case R.id.diseases_and_treatments_menu_voice_search:
			{
				String language = (mSearchLanguage.equals("")) ? "en-US" : "nb-NO";

				try
				{
					Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
					startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
				}
				catch(Exception e)
				{
					new MaterialDialog.Builder(mContext).title(R.string.device_not_supported_dialog_title).content(getString(R.string.device_not_supported_dialog_message)).positiveText(R.string.device_not_supported_dialog_positive_button).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
				}

				return true;
			}
			case R.id.diseases_and_treatments_menu_clear_recent_searches:
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
	private void getRecentSearches()
	{
		mSqLiteDatabase = new DiseasesAndTreatmentsSQLiteHelper(mContext).getWritableDatabase();
		mCursor = mSqLiteDatabase.query(DiseasesAndTreatmentsSQLiteHelper.TABLE, null, null, null, null, null, DiseasesAndTreatmentsSQLiteHelper.COLUMN_ID+" DESC LIMIT 10");

		String[] fromColumns = {DiseasesAndTreatmentsSQLiteHelper.COLUMN_STRING};
		int[] toViews = {R.id.diseases_and_treatments_list_item_string};

		mListView.setAdapter(new SimpleCursorAdapter(mContext, R.layout.activity_diseases_and_treatments_list_item, mCursor, fromColumns, toViews, 0));

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				int index = i - 1;

				if(mCursor.moveToPosition(index))
				{
					search(mCursor.getString(mCursor.getColumnIndexOrThrow(DiseasesAndTreatmentsSQLiteHelper.COLUMN_STRING)));
				}
			}
		});

		Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fab);
		mFloatingActionButton.startAnimation(animation);
		mFloatingActionButton.setVisibility(View.VISIBLE);
	}

	private void clearRecentSearches()
	{
		mSqLiteDatabase.delete(DiseasesAndTreatmentsSQLiteHelper.TABLE, null, null);

		mTools.showToast(getString(R.string.diseases_and_treatments_recent_searches_removed), 0);

		getRecentSearches();
	}

	private void showSearchLanguageDialog()
	{
		new MaterialDialog.Builder(mContext).title(R.string.diseases_and_treatments_language_dialog_title).items(R.array.diseases_and_treatments_language_dialog_choices).itemsCallback(new MaterialDialog.ListCallback()
		{
			@Override
			public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence)
			{
				if(i == 0)
				{
					mSearchLanguage = "";

					mToolbarSearchEditText.setHint(getString(R.string.diseases_and_treatments_toolbar_search_english_hint));
				}
				else
				{
					mSearchLanguage = "no";

					mToolbarSearchEditText.setHint(getString(R.string.diseases_and_treatments_toolbar_search_norwegian_hint));
				}

				showSearch();
			}
		}).itemsColorRes(R.color.dark_blue).show();
	}

	private void showSearch()
	{
		mToolbarSearchEditText.setVisibility(View.VISIBLE);
		mToolbarSearchEditText.requestFocus();

		Handler handler = new Handler();

		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				mInputMethodManager.showSoftInput(mToolbarSearchEditText, 0);
			}
		}, 125);
	}

	private void search(String searchString)
	{
		if(searchString.equals("")) return;

		Intent intent = new Intent(mContext, DiseasesAndTreatmentsSearchActivity.class);
		intent.putExtra("language", mSearchLanguage);
		intent.putExtra("string", mTools.firstToUpper(searchString));
		startActivity(intent);
	}
}