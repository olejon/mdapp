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
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

		// Layout
		setContentView(R.layout.activity_pharmacies_location);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.pharmacies_location_toolbar);
		toolbar.setTitle(mPharmacyMunicipality);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Recycler view
		RecyclerView recyclerView = findViewById(R.id.pharmacies_location_cards);

		recyclerView.setHasFixedSize(true);
		recyclerView.setAdapter(new PharmaciesLocationAdapter());
		recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

		// Get pharmacies
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

				mPharmacyNames.add(name);
				mPharmacyAddresses.add(address);
			}
		}

		if(mTools.isTablet())
		{
			int spanCount = (mCursor.getCount() == 1) ? 1 : 2;
			recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
		}

		recyclerView.setAdapter(new PharmaciesLocationAdapter());
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

	// Adapter
	class PharmaciesLocationAdapter extends RecyclerView.Adapter<PharmaciesLocationAdapter.PharmaciesLocationViewHolder>
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

		class PharmaciesLocationViewHolder extends RecyclerView.ViewHolder
		{
			final TextView name;
			final TextView address;
			final Button mapButton;
			final Button contactButton;

			PharmaciesLocationViewHolder(View view)
			{
				super(view);

				name = view.findViewById(R.id.pharmacies_location_card_name);
				address = view.findViewById(R.id.pharmacies_location_card_address);
				mapButton = view.findViewById(R.id.pharmacies_location_card_map_button);
				contactButton = view.findViewById(R.id.pharmacies_location_card_contact_button);
			}
		}

		@NonNull
		@Override
		public PharmaciesLocationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
		{
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_pharmacies_location_card, viewGroup, false);
			return new PharmaciesLocationViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull PharmaciesLocationViewHolder viewHolder, int i)
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