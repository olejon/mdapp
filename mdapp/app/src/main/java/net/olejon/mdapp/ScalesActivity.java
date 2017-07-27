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
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ScalesActivity extends AppCompatActivity
{
    private final Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_scales);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.scales_toolbar);
        toolbar.setTitle(getString(R.string.scales_title));

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Scales
        TextView scalesMedscape = (TextView) findViewById(R.id.scales_medscape);

        scalesMedscape.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(mContext, MainWebViewActivity.class);
                intent.putExtra("title", mContext.getString(R.string.scales_medscape_title));
                intent.putExtra("uri", "http://search.medscape.com/search/?q=Scale");
                mContext.startActivity(intent);
            }
        });

        TextView scalesVasTitle = (TextView) findViewById(R.id.scales_vas_title);
        ImageView scalesVasImage = (ImageView) findViewById(R.id.scales_vas_image);
        TextView scalesVasButton = (TextView) findViewById(R.id.scales_vas_button);

        scalesVasTitle.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showScale(R.drawable.vas);
            }
        });

        scalesVasImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showScale(R.drawable.vas);
            }
        });

        scalesVasButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showScale(R.drawable.vas);
            }
        });

        TextView scalesGcsTitle = (TextView) findViewById(R.id.scales_gcs_title);
        ImageView scalesGcsImage = (ImageView) findViewById(R.id.scales_gcs_image);
        TextView scalesGcsButton = (TextView) findViewById(R.id.scales_gcs_button);

        scalesGcsTitle.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showScale(R.drawable.gcs);
            }
        });

        scalesGcsImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showScale(R.drawable.gcs);
            }
        });

        scalesGcsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showScale(R.drawable.gcs);
            }
        });

        TextView scalesMewsTitle = (TextView) findViewById(R.id.scales_mews_title);
        ImageView scalesMewsImage = (ImageView) findViewById(R.id.scales_mews_image);
        TextView scalesMewsButton = (TextView) findViewById(R.id.scales_mews_button);

        scalesMewsTitle.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showScale(R.drawable.mews);
            }
        });

        scalesMewsImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showScale(R.drawable.mews);
            }
        });

        scalesMewsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showScale(R.drawable.mews);
            }
        });
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

    // Scales
    private void showScale(int scale)
    {
        Intent intent = new Intent(mContext, ScaleActivity.class);
        intent.putExtra("scale", scale);
        startActivity(intent);
    }
}