package net.olejon.mdapp;

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
    private String mPharmacyName;
    private String mPharmacyCoordinates;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

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