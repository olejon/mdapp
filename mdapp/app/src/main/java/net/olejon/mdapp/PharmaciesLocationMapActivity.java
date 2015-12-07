package net.olejon.mdapp;

/*

Copyright 2015 Ole Jon Bjørkum

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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.net.URLEncoder;

public class PharmaciesLocationMapActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 0;

    private final Activity mActivity = this;

    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private String mPharmacyName;
    private String mPharmacyAddress;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected()) mTools.showToast(getString(R.string.device_not_connected), 1);

        // Intent
        final Intent intent = getIntent();

        mPharmacyName = intent.getStringExtra("name");
        mPharmacyAddress = intent.getStringExtra("address");

        // Location
        if(mPharmacyAddress.startsWith("Boks") || mPharmacyAddress.startsWith("Pb.") || mPharmacyAddress.startsWith("Postboks") || mPharmacyAddress.startsWith("Serviceboks"))
        {
            mTools.showToast(getString(R.string.pharmacies_location_map_post_box_location), 1);

            finish();

            return;
        }

        // Layout
        setContentView(R.layout.activity_pharmacies_location_map);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.pharmacies_location_map_toolbar);
        toolbar.setTitle(mPharmacyName);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Permissions
        String [] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkPermissions())
        {
            ActivityCompat.requestPermissions(mActivity, permissions, PERMISSIONS_REQUEST_ACCESS_LOCATION);
        }
        else
        {
            showMap();
        }
    }

    // Menu
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
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Permissions
    private boolean checkPermissions()
    {
        return (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

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
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.pharmacies_location_map_map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap)
    {
        googleMap.setMyLocationEnabled(true);

        try
        {
            mTools.showToast(getString(R.string.pharmacies_location_map_locating), 0);

            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website_uri)+"api/1/geocode/?address="+URLEncoder.encode(mPharmacyAddress, "utf-8"), new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response)
                {
                    try
                    {
                        double latitude = response.getDouble("latitude");
                        double longitude = response.getDouble("longitude");

                        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(mPharmacyName));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
                    }
                    catch(Exception e)
                    {
                        mTools.showToast(getString(R.string.pharmacies_location_map_exact_location_not_found), 1);

                        Log.e("PharmaciesLocationMap", Log.getStackTraceString(e));

                        finish();
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    mTools.showToast(getString(R.string.pharmacies_location_map_exact_location_not_found), 1);

                    Log.e("PharmaciesLocationMap", error.toString());

                    finish();
                }
            });

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsonObjectRequest);
        }
        catch(Exception e)
        {
            Log.e("PharmaciesLocationMap", Log.getStackTraceString(e));
        }
    }
}