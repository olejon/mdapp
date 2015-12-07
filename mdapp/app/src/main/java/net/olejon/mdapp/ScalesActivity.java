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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

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
        recyclerView.setAdapter(new ScalesAdapter(scalesTitlesArrayList, scalesImagesArrayList));

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

    // Adapter
    private class ScalesAdapter extends RecyclerView.Adapter<ScalesAdapter.ScaleViewHolder>
    {
        private final ArrayList<String> mScalesTitlesArrayList;
        private final ArrayList<Integer> mScalesImagesArrayList;

        private int mLastPosition = -1;

        private ScalesAdapter(ArrayList<String> scalesTitlesArrayList, ArrayList<Integer> scalesImagesArrayList)
        {
            mScalesTitlesArrayList = scalesTitlesArrayList;
            mScalesImagesArrayList = scalesImagesArrayList;
        }

        class ScaleViewHolder extends RecyclerView.ViewHolder
        {
            private final CardView card;
            private final TextView title;
            private final ImageView image;
            private final View separator;
            private final TextView button;
            private final TextView medscape;

            public ScaleViewHolder(View view)
            {
                super(view);

                card = (CardView) view.findViewById(R.id.scales_card);
                title = (TextView) view.findViewById(R.id.scales_card_title);
                image = (ImageView) view.findViewById(R.id.scales_card_image);
                separator = view.findViewById(R.id.scales_card_separator);
                button = (TextView) view.findViewById(R.id.scales_card_button);
                medscape = (TextView) view.findViewById(R.id.scales_medscape);
            }
        }

        @Override
        public ScaleViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_scales_card, viewGroup, false);
            return new ScaleViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ScaleViewHolder viewHolder, final int i)
        {
            viewHolder.title.setText(mScalesTitlesArrayList.get(i));

            if(mScalesImagesArrayList.get(i) == null)
            {
                viewHolder.title.setVisibility(View.GONE);
                viewHolder.image.setVisibility(View.GONE);
                viewHolder.separator.setVisibility(View.GONE);
                viewHolder.button.setVisibility(View.GONE);

                viewHolder.medscape.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(mContext, MainWebViewActivity.class);
                        intent.putExtra("title", mContext.getString(R.string.scales_medscape_title));
                        intent.putExtra("uri", "http://search.medscape.com/search/?q=scale");
                        mContext.startActivity(intent);
                    }
                });
            }
            else
            {
                viewHolder.medscape.setVisibility(View.GONE);
                viewHolder.image.setImageResource(mScalesImagesArrayList.get(i));

                viewHolder.title.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        showScale(mScalesImagesArrayList.get(i));
                    }
                });

                viewHolder.image.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        showScale(mScalesImagesArrayList.get(i));
                    }
                });

                viewHolder.button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        showScale(mScalesImagesArrayList.get(i));
                    }
                });
            }

            animateCard(viewHolder.card, i);
        }

        @Override
        public int getItemCount()
        {
            return mScalesTitlesArrayList.size();
        }

        private void showScale(int scale)
        {
            Intent intent = new Intent(mContext, ScaleActivity.class);
            intent.putExtra("scale", scale);
            mContext.startActivity(intent);
        }

        private void animateCard(View view, int position)
        {
            if(position > mLastPosition)
            {
                mLastPosition = position;

                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.card);
                view.startAnimation(animation);
            }
        }
    }
}