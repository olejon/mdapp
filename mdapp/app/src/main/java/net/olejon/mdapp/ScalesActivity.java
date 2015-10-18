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
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;

public class ScalesActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_scales);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.scales_toolbar);
        toolbar.setTitle(getString(R.string.scales_title));

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Recycler view
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.scales_cards);

        final ArrayList<String> scalesTitlesArrayList = new ArrayList<>();
        final ArrayList<Integer> scalesImagesArrayList = new ArrayList<>();

        scalesTitlesArrayList.add(getString(R.string.scales_medscape));
        scalesTitlesArrayList.add(getString(R.string.scales_vas));
        scalesTitlesArrayList.add(getString(R.string.scales_gcs));
        scalesTitlesArrayList.add(getString(R.string.scales_mews));

        scalesImagesArrayList.add(null);
        scalesImagesArrayList.add(R.drawable.vas);
        scalesImagesArrayList.add(R.drawable.gcs);
        scalesImagesArrayList.add(R.drawable.mews);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new ScalesAdapter(mContext, scalesTitlesArrayList, scalesImagesArrayList));

        if(mTools.isTablet())
        {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        }
        else
        {
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
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
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}