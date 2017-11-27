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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class PharmaciesLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback
{
	private final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 1;

	private final Activity mActivity = this;

	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private String mPharmacyName;
	private String mPharmacyAddress;

	private ArrayList<String> mPharmacyNames;
	private ArrayList<String> mPharmacyAddresses;

	private int mPharmacyAddressesSize;

	private double mOtherPharmacyLatitude = 0;
	private double mOtherPharmacyLongitude = 0;

	private boolean mPharmacyAddressNotFound = true;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Intent
		Intent intent = getIntent();

		mPharmacyName = intent.getStringExtra("name");
		mPharmacyAddress = intent.getStringExtra("address");
		mPharmacyNames = intent.getStringArrayListExtra("names");
		mPharmacyAddresses = intent.getStringArrayListExtra("addresses");
		mPharmacyAddressesSize = mPharmacyAddresses.size();

		// Layout
		setContentView(R.layout.activity_pharmacies_location_map);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.pharmacies_location_map_toolbar);
		toolbar.setTitle(mPharmacyName);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Permissions
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

			if(ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
			{
				ActivityCompat.requestPermissions(mActivity, permissions, PERMISSIONS_REQUEST_ACCESS_LOCATION);
			}
			else
			{
				showMap();
			}
		}
		else
		{
			showMap();
		}
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_pharmacies_location_map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
			{
				finish();
				return true;
			}
			case R.id.pharmacies_location_map_menu_information:
			{
				showInformationDialog(true);
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	// Information dialog
	private void showInformationDialog(boolean show)
	{
		if(!mTools.getSharedPreferencesBoolean("PHARMACIES_LOCATION_MAP_HIDE_INFORMATION_DIALOG") || show)
		{
			new MaterialDialog.Builder(mContext).title(R.string.pharmacies_location_map_information_dialog_title).content(getString(R.string.pharmacies_location_map_information_dialog_message)).positiveText(R.string.pharmacies_location_map_information_dialog_positive_button).onPositive(new MaterialDialog.SingleButtonCallback()
			{
				@Override
				public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
				{
					mTools.setSharedPreferencesBoolean("PHARMACIES_LOCATION_MAP_HIDE_INFORMATION_DIALOG", true);
				}
			}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
		}
	}

	// Permissions
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if(requestCode == PERMISSIONS_REQUEST_ACCESS_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			showMap();
		}
		else
		{
			mTools.showToast(getString(R.string.device_permissions_not_granted), 1);

			finish();
		}
	}

	// Map
	private void showMap()
	{
		if(mPharmacyAddressesSize == 0)
		{
			mTools.showToast(getString(R.string.pharmacies_location_map_location_not_found), 1);

			finish();
		}
		else
		{
			mTools.showToast(getString(R.string.pharmacies_location_map_locating), 0);

			MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.pharmacies_location_map_map);
			mapFragment.getMapAsync(this);
		}
	}

	@Override
	public void onMapReady(final GoogleMap googleMap)
	{
		if(ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			googleMap.setMyLocationEnabled(true);
		}

		try
		{
			final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

			requestQueue.start();

			JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mTools.getApiUri()+"api/1/geocode/?address="+URLEncoder.encode(mPharmacyAddress, "utf-8"), null, new Response.Listener<JSONObject>()
			{
				@Override
				public void onResponse(JSONObject response)
				{
					requestQueue.stop();

					try
					{
						mPharmacyAddressNotFound = false;

						double latitude = response.getDouble("latitude");
						double longitude = response.getDouble("longitude");

						googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(mPharmacyName)).showInfoWindow();
						googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13.5f));

						showInformationDialog(false);
					}
					catch(Exception e)
					{
						Log.e("PharmaciesLocationMap", Log.getStackTraceString(e));
					}
				}
			}, new Response.ErrorListener()
			{
				@Override
				public void onErrorResponse(VolleyError error)
				{
					requestQueue.stop();

					Log.e("PharmaciesLocationMap", error.toString());
				}
			});

			jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

			requestQueue.add(jsonObjectRequest);

			Handler handler = new Handler();

			handler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						for(int i = 0; i < mPharmacyAddressesSize; i++)
						{
							String pharmacyOtherName = mPharmacyNames.get(i);
							String pharmacyOtherAddress = mPharmacyAddresses.get(i);

							if(pharmacyOtherAddress.equals(mPharmacyAddress)) continue;

							final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

							requestQueue.start();

							JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mTools.getApiUri()+"api/1/geocode/?address="+URLEncoder.encode(pharmacyOtherAddress, "utf-8")+"&name="+URLEncoder.encode(pharmacyOtherName, "utf-8"), null, new Response.Listener<JSONObject>()
							{
								@Override
								public void onResponse(JSONObject response)
								{
									requestQueue.stop();

									try
									{
										String name = response.getString("name");

										double latitude = response.getDouble("latitude");
										double longitude = response.getDouble("longitude");

										googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name));

										if(mPharmacyAddressNotFound && mOtherPharmacyLatitude == 0 && mOtherPharmacyLongitude == 0)
										{
											mOtherPharmacyLatitude = latitude;
											mOtherPharmacyLongitude = longitude;

											googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mOtherPharmacyLatitude, mOtherPharmacyLongitude), 13.5f));
										}
									}
									catch(Exception e)
									{
										Log.e("PharmaciesLocationMap", Log.getStackTraceString(e));
									}
								}
							}, new Response.ErrorListener()
							{
								@Override
								public void onErrorResponse(VolleyError error)
								{
									requestQueue.stop();

									Log.e("PharmaciesLocationMap", error.toString());
								}
							});

							jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

							requestQueue.add(jsonObjectRequest);
						}
					}
					catch(Exception e)
					{
						Log.e("PharmaciesLocationMap", Log.getStackTraceString(e));
					}
				}
			}, 1000);
		}
		catch(Exception e)
		{
			Log.e("PharmaciesLocationMap", Log.getStackTraceString(e));
		}
	}
}