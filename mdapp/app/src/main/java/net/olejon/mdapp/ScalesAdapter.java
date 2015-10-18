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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ScalesAdapter extends RecyclerView.Adapter<ScalesAdapter.ScaleViewHolder>
{
    private final Context mContext;

    private final ArrayList<String> mScalesTitlesArrayList;
    private final ArrayList<Integer> mScalesImagesArrayList;

    private int mLastPosition = -1;

    public ScalesAdapter(Context context, ArrayList<String> scalesTitlesArrayList, ArrayList<Integer> scalesImagesArrayList)
    {
        mContext = context;

        mScalesTitlesArrayList = scalesTitlesArrayList;
        mScalesImagesArrayList = scalesImagesArrayList;
    }

    static class ScaleViewHolder extends RecyclerView.ViewHolder
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

        animateView(viewHolder.card, i);
    }

    @Override
    public int getItemCount()
    {
        return mScalesTitlesArrayList.size();
    }

    private void animateView(View view, int position)
    {
        if(position > mLastPosition)
        {
            mLastPosition = position;

            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.card);
            view.startAnimation(animation);
        }
    }

    private void showScale(int scale)
    {
        Intent intent = new Intent(mContext, ScaleActivity.class);
        intent.putExtra("scale", scale);
        mContext.startActivity(intent);
    }
}