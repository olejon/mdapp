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
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

public class AtcCodesActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	private String mAtcCode;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Intent
		Intent intent = getIntent();

		mAtcCode = intent.getStringExtra("code");

		String atcCodes = mAtcCode.substring(0, 5);

		// Layout
		setContentView(R.layout.activity_atc_codes);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.atc_codes_toolbar);
		toolbar.setTitle(atcCodes);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// List
		ListView listView = findViewById(R.id.atc_codes_list);

		View listViewEmpty = findViewById(R.id.atc_codes_list_empty);
		listView.setEmptyView(listViewEmpty);

		// Get substances
		mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
		String[] queryColumns = {SlDataSQLiteHelper.ATC_CODES_COLUMN_ID, SlDataSQLiteHelper.ATC_CODES_COLUMN_CODE, SlDataSQLiteHelper.ATC_CODES_COLUMN_NAME};
		mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ATC_CODES, queryColumns, SlDataSQLiteHelper.ATC_CODES_COLUMN_CODE+" LIKE "+mTools.sqe(atcCodes+"%"), null, null, null, SlDataSQLiteHelper.ATC_CODES_COLUMN_CODE);

		String[] fromColumns = new String[] {SlDataSQLiteHelper.ATC_CODES_COLUMN_CODE, SlDataSQLiteHelper.ATC_CODES_COLUMN_NAME};
		int[] toViews = new int[] {R.id.atc_codes_list_item_code, R.id.atc_codes_list_item_name};

		AtcCodesSimpleCursorAdapter atcCodesSimpleCursorAdapter = new AtcCodesSimpleCursorAdapter(fromColumns, toViews);

		listView.setAdapter(atcCodesSimpleCursorAdapter);

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
			{
				if(mCursor.moveToPosition(i))
				{
					String substanceName = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ATC_CODES_COLUMN_NAME));

					if(substanceName.equals("") || substanceName.equals("Diverse") || substanceName.equals("Kombinasjoner") || substanceName.startsWith("Andre ") || substanceName.contains("kombinasjon"))
					{
						mTools.showToast(getString(R.string.atc_codes_substance_not_a_substance), 1);
					}
					else if(substanceName.contains(" / "))
					{
						final CharSequence[] substancesNamesStringArray = substanceName.split(" / ");

						new MaterialDialog.Builder(mContext).title(R.string.atc_codes_dialog_title).items(substancesNamesStringArray).itemsCallback(new MaterialDialog.ListCallback()
						{
							@Override
							public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence)
							{
								getSubstance(substancesNamesStringArray[i].toString());
							}
						}).itemsColorRes(R.color.dark_blue).show();
					}
					else
					{
						getSubstance(substanceName);
					}
				}
			}
		});

		for(int i = 0; i < mCursor.getCount(); i++)
		{
			if(mCursor.moveToPosition(i))
			{
				String atcCodeSelection = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ATC_CODES_COLUMN_CODE));

				if(atcCodeSelection.equals(mAtcCode))
				{
					listView.setSelection(i);
					break;
				}
			}
		}
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

	// Get substance
	private void getSubstance(String substanceName)
	{
		SQLiteDatabase sqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
		String[] queryColumns = {SlDataSQLiteHelper.SUBSTANCES_COLUMN_ID};
		Cursor cursor = sqLiteDatabase.query(SlDataSQLiteHelper.TABLE_SUBSTANCES, queryColumns, SlDataSQLiteHelper.SUBSTANCES_COLUMN_NAME+" = "+mTools.sqe(substanceName), null, null, null, SlDataSQLiteHelper.SUBSTANCES_COLUMN_ID);

		if(cursor.moveToFirst())
		{
			long id = cursor.getLong(cursor.getColumnIndexOrThrow(SlDataSQLiteHelper.SUBSTANCES_COLUMN_ID));

			Intent intent = new Intent(mContext, SubstanceActivity.class);
			intent.putExtra("id", id);
			startActivity(intent);
		}
		else
		{
			cursor = sqLiteDatabase.query(SlDataSQLiteHelper.TABLE_SUBSTANCES, queryColumns, SlDataSQLiteHelper.SUBSTANCES_COLUMN_NAME+" LIKE "+mTools.sqe("%"+substanceName+"%"), null, null, null, SlDataSQLiteHelper.SUBSTANCES_COLUMN_ID);

			if(cursor.moveToFirst())
			{
				long id = cursor.getLong(cursor.getColumnIndexOrThrow(SlDataSQLiteHelper.SUBSTANCES_COLUMN_ID));

				Intent intent = new Intent(mContext, SubstanceActivity.class);
				intent.putExtra("id", id);
				startActivity(intent);
			}
			else
			{
				mTools.showToast(getString(R.string.atc_codes_could_not_find_substance), 1);
			}
		}

		cursor.close();
		sqLiteDatabase.close();
	}

	// Adapter
	private class AtcCodesSimpleCursorAdapter extends SimpleCursorAdapter
	{
		AtcCodesSimpleCursorAdapter(String[] from, int[] to)
		{
			super(mContext, R.layout.activity_atc_codes_list_item, mCursor, from, to, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = super.getView(position, convertView, parent);

			LinearLayout linearLayout = view.findViewById(R.id.atc_codes_list_item_layout);

			TextView atcCodeTextView = view.findViewById(R.id.atc_codes_list_item_code);
			TextView atcNameTextView = view.findViewById(R.id.atc_codes_list_item_name);

			String atcCode = atcCodeTextView.getText().toString();

			if(atcCode.equals(mAtcCode))
			{
				linearLayout.setBackgroundResource(R.drawable.atc_codes_code);

				atcCodeTextView.setTextColor(ContextCompat.getColor(mContext, R.color.purple));

				atcNameTextView.setTextColor(ContextCompat.getColor(mContext, R.color.purple));
				atcNameTextView.setTypeface(Typeface.DEFAULT_BOLD);
			}
			else
			{
				linearLayout.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.transparent));

				atcCodeTextView.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));

				atcNameTextView.setTextColor(ContextCompat.getColor(mContext, android.R.color.black));
				atcNameTextView.setTypeface(Typeface.DEFAULT);
			}

			return view;
		}
	}
}