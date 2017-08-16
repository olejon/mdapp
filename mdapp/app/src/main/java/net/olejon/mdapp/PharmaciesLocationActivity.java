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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.net.URLEncoder;
import java.util.ArrayList;

public class PharmaciesLocationActivity extends AppCompatActivity
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	private InputMethodManager mInputMethodManager;

	private LinearLayout mToolbarSearchLayout;
	private EditText mToolbarSearchEditText;
	private RecyclerView mRecyclerView;

	private String mPharmacyMunicipality;

	private ArrayList<String> mPharmacyNames;
	private ArrayList<String> mPharmacyAddresses;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Intent
		Intent intent = getIntent();

		mPharmacyMunicipality = intent.getStringExtra("municipality");

		// Input manager
		mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_pharmacies_location);

		// Toolbar
		Toolbar toolbar = (Toolbar) findViewById(R.id.pharmacies_location_toolbar);
		toolbar.setTitle(mPharmacyMunicipality);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mToolbarSearchLayout = (LinearLayout) findViewById(R.id.pharmacies_location_toolbar_search_layout);
		mToolbarSearchEditText = (EditText) findViewById(R.id.pharmacies_location_toolbar_search);

		// Recycler view
		mRecyclerView = (RecyclerView) findViewById(R.id.pharmacies_location_cards);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(new PharmaciesLocationAdapter());
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

		// Get pharmacies
		GetPharmaciesTask getPharmaciesTask = new GetPharmaciesTask();
		getPharmaciesTask.execute();
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
		if(mToolbarSearchLayout.getVisibility() == View.VISIBLE)
		{
			mToolbarSearchLayout.setVisibility(View.GONE);
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
			mToolbarSearchLayout.setVisibility(View.VISIBLE);
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
		getMenuInflater().inflate(R.menu.menu_pharmacies_location, menu);
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
			case R.id.pharmacies_location_menu_contact_information_to_all:
			{
				try
				{
					Intent intent = new Intent(mContext, MainWebViewActivity.class);
					intent.putExtra("title", getString(R.string.pharmacies_location_menu_contact_information_to_all));
					intent.putExtra("uri", "https://www.gulesider.no/finn:Apotek%20"+URLEncoder.encode(mPharmacyMunicipality, "utf-8"));
					startActivity(intent);
					return true;
				}
				catch(Exception e)
				{
					Log.e("PharmaciesLocation", Log.getStackTraceString(e));
				}
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	// Get pharmacies
	private class GetPharmaciesTask extends AsyncTask<Void,Void,Void>
	{
		@Override
		protected void onPostExecute(Void success)
		{
			if(mTools.isTablet())
			{
				int spanCount = (mCursor.getCount() == 1) ? 1 : 2;

				mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
			}

			mRecyclerView.setAdapter(new PharmaciesLocationAdapter());
		}

		@Override
		protected Void doInBackground(Void... voids)
		{
			mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
			String[] queryColumns = {SlDataSQLiteHelper.PHARMACIES_COLUMN_ID, SlDataSQLiteHelper.PHARMACIES_COLUMN_NAME, SlDataSQLiteHelper.PHARMACIES_COLUMN_ADDRESS};
			mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_PHARMACIES, queryColumns, SlDataSQLiteHelper.PHARMACIES_COLUMN_MUNICIPALITY+" = "+mTools.sqe(mPharmacyMunicipality), null, null, null, null);

			int count = mCursor.getCount();

			mPharmacyNames = new ArrayList<>();
			mPharmacyAddresses = new ArrayList<>();

			for(int i = 0; i < count; i++)
			{
				if(mCursor.moveToPosition(i))
				{
					String name = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.PHARMACIES_COLUMN_NAME));
					String address = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.PHARMACIES_COLUMN_ADDRESS));

					if(!mTools.pharmacyAddressIsPostBox(address))
					{
						mPharmacyNames.add(name);
						mPharmacyAddresses.add(address);
					}
				}
			}

			return null;
		}
	}

	// Adapter
	class PharmaciesLocationAdapter extends RecyclerView.Adapter<PharmaciesLocationAdapter.PharmaciesViewHolder>
	{
		boolean isGoogleMapsInstalled = false;

		PharmaciesLocationAdapter()
		{
			try
			{
				mContext.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);

				isGoogleMapsInstalled = true;
			}
			catch(Exception e)
			{
				Log.e("PharmaciesLocation", Log.getStackTraceString(e));
			}
		}

		class PharmaciesViewHolder extends RecyclerView.ViewHolder
		{
			final TextView name;
			final TextView address;
			final TextView mapButton;
			final TextView contactButton;

			PharmaciesViewHolder(View view)
			{
				super(view);

				name = (TextView) view.findViewById(R.id.pharmacies_location_card_name);
				address = (TextView) view.findViewById(R.id.pharmacies_location_card_address);
				mapButton = (TextView) view.findViewById(R.id.pharmacies_location_card_map_button);
				contactButton = (TextView) view.findViewById(R.id.pharmacies_location_card_contact_button);
			}
		}

		@Override
		public PharmaciesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
		{
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_pharmacies_location_card, viewGroup, false);
			return new PharmaciesViewHolder(view);
		}

		@Override
		public void onBindViewHolder(PharmaciesViewHolder viewHolder, int i)
		{
			if(mCursor.moveToPosition(i))
			{
				final String name = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.PHARMACIES_COLUMN_NAME));
				final String address = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.PHARMACIES_COLUMN_ADDRESS));

				viewHolder.name.setText(name);
				viewHolder.address.setText(address);

				viewHolder.mapButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						if(isGoogleMapsInstalled)
						{
							if(mTools.isDeviceConnected())
							{
								Intent intent = new Intent(mContext, PharmaciesLocationMapActivity.class);
								intent.putExtra("name", name);
								intent.putExtra("address", address);
								intent.putExtra("names", mPharmacyNames);
								intent.putExtra("addresses", mPharmacyAddresses);
								mContext.startActivity(intent);
							}
							else
							{
								mTools.showToast(getString(R.string.device_not_connected), 1);
							}
						}
						else
						{
							new MaterialDialog.Builder(mContext).title(R.string.device_not_supported_dialog_title).content(getString(R.string.device_not_supported_dialog_message)).positiveText(R.string.device_not_supported_dialog_positive_button).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
						}
					}
				});

				viewHolder.contactButton.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						try
						{
							Intent intent = new Intent(mContext, MainWebViewActivity.class);
							intent.putExtra("title", name);
							intent.putExtra("uri", "https://www.gulesider.no/finn:"+URLEncoder.encode(name, "utf-8"));
							mContext.startActivity(intent);
						}
						catch(Exception e)
						{
							Log.e("PharmaciesLocation", Log.getStackTraceString(e));
						}
					}
				});
			}
		}

		@Override
		public int getItemCount()
		{
			return (mCursor == null) ? 0 : mCursor.getCount();
		}
	}
}