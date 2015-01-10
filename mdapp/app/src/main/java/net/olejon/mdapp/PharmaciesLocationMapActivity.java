package net.olejon.mdapp;

/*

Copyright 2015 Ole Jon Bjørkum

This file is part of LegeAppen.

LegeAppen is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

LegeAppen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with LegeAppen.  If not, see <http://www.gnu.org/licenses/>.

*/

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class PharmaciesLocationMapActivity extends ActionBarActivity implements OnMapReadyCallback
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private String mPharmacyName;
    private String mPharmacyCoordinates;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected()) mTools.showToast(getString(R.string.device_not_connected), 1);

        // Intent
        Intent intent = getIntent();

        mPharmacyName = intent.getStringExtra("name");
        mPharmacyCoordinates = intent.getStringExtra("coordinates");

        // Layout
        setContentView(R.layout.activity_pharmacies_location_map);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.pharmacies_location_map_toolbar);
        toolbar.setTitle(mPharmacyName);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Map
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.pharmacies_location_map_map);
        mapFragment.getMapAsync(this);
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

    // Map
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        String[] pharmacyCoordinates = mPharmacyCoordinates.split(",");

        double latitude = Double.parseDouble(pharmacyCoordinates[0]);
        double longitude = Double.parseDouble(pharmacyCoordinates[1]);

        googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(mPharmacyName));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 16));
    }
}