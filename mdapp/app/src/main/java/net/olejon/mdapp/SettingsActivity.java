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

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_settings);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        toolbar.setTitle(getString(R.string.settings_title));

        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Fragment
        getFragmentManager().beginTransaction().replace(R.id.settings_container, new SettingsFragment()).commit();
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
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}