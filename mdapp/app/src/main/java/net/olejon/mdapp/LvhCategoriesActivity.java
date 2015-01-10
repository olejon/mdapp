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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONArray;

public class LvhCategoriesActivity extends ActionBarActivity
{
    private final Context mContext = this;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Transition
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

        // Intent
        Intent intent = getIntent();

        final String categoryColor = intent.getStringExtra("color");
        final String categoryIcon = intent.getStringExtra("icon");
        final String categoryTitle = intent.getStringExtra("title");

        // Layout
        setContentView(R.layout.activity_lvh_categories);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.lvh_categories_toolbar);
        toolbar.setTitle(categoryTitle);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lvh_categories_cards);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new LvhCategoriesAdapter(mContext, new JSONArray(), "", ""));
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Get categories
        try
        {
            recyclerView.setAdapter(new LvhCategoriesAdapter(mContext, new JSONArray(intent.getStringExtra("subcategories")), categoryColor, categoryIcon));
        }
        catch(Exception e)
        {
            Log.e("LvhCategoriesActivity", Log.getStackTraceString(e));
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
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
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
