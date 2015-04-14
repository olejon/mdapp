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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class DiseasesAndTreatmentsSearchAdapter extends RecyclerView.Adapter<DiseasesAndTreatmentsSearchAdapter.DiseasesAndTreatmentsSearchViewHolder>
{
    private final Context mContext;

    private final JSONArray mResults;

    private final String mSearch;

    private int mLastPosition = -1;

    public DiseasesAndTreatmentsSearchAdapter(Context context, JSONArray results, String search)
    {
        mContext = context;

        mResults = results;

        mSearch = search;
    }

    static class DiseasesAndTreatmentsSearchViewHolder extends RecyclerView.ViewHolder
    {
        final CardView card;
        final ImageView icon;
        final TextView title;
        final TextView text;

        public DiseasesAndTreatmentsSearchViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.diseases_and_treatments_search_card);
            icon = (ImageView) view.findViewById(R.id.diseases_and_treatments_search_card_icon);
            title = (TextView) view.findViewById(R.id.diseases_and_treatments_search_card_title);
            text = (TextView) view.findViewById(R.id.diseases_and_treatments_search_card_text);
        }
    }

    @Override
    public DiseasesAndTreatmentsSearchViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_diseases_and_treatments_search_card, viewGroup, false);
        return new DiseasesAndTreatmentsSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DiseasesAndTreatmentsSearchViewHolder viewHolder, final int i)
    {
        try
        {
            final JSONObject result = mResults.getJSONObject(i);

            final String type = result.getString("type");
            final String title = result.getString("title");
            final String text = result.getString("text");
            final String uri = result.getString("uri");

            viewHolder.title.setText(title);

            switch(type)
            {
                case "pubmed":
                {
                    viewHolder.icon.setImageResource(R.drawable.pubmed);
                    viewHolder.text.setText(text);
                    break;
                }
                case "webofscience":
                {
                    viewHolder.icon.setImageResource(R.drawable.webofscience);
                    viewHolder.text.setText(text);
                    break;
                }
                case "medlineplus":
                {
                    viewHolder.icon.setImageResource(R.drawable.medlineplus);
                    viewHolder.text.setText(text);
                    break;
                }
                case "wikipedia":
                {
                    viewHolder.icon.setImageResource(R.drawable.wikipedia);
                    viewHolder.text.setText(text);
                    break;
                }
                case "uptodate":
                {
                    viewHolder.icon.setImageResource(R.drawable.uptodate);
                    viewHolder.text.setText(text);
                    break;
                }
                case "bmj":
                {
                    viewHolder.icon.setImageResource(R.drawable.bmj);
                    viewHolder.text.setText(text);
                    break;
                }
                case "nhi":
                {
                    viewHolder.icon.setImageResource(R.drawable.nhi);
                    viewHolder.text.setText(text);
                    break;
                }
                case "sml":
                {
                    viewHolder.icon.setImageResource(R.drawable.sml);
                    viewHolder.text.setText(text);
                    break;
                }
                case "forskning":
                {
                    viewHolder.icon.setImageResource(R.drawable.forskning);
                    viewHolder.text.setText(text);
                    break;
                }
                case "helsebiblioteket":
                {
                    viewHolder.icon.setImageResource(R.drawable.helsebiblioteket);
                    viewHolder.text.setText(text);
                    break;
                }
                case "tidsskriftet":
                {
                    viewHolder.icon.setImageResource(R.drawable.tidsskriftet);
                    viewHolder.text.setText(text);
                    break;
                }
                case "oncolex":
                {
                    viewHolder.icon.setImageResource(R.drawable.oncolex);
                    viewHolder.text.setText(text);
                    break;
                }
                case "brukerhandboken":
                {
                    viewHolder.icon.setImageResource(R.drawable.brukerhandboken);
                    viewHolder.text.setText(text);
                    break;
                }
                case "helsenorge":
                {
                    viewHolder.icon.setImageResource(R.drawable.helsenorge);
                    viewHolder.text.setText(text);
                    break;
                }
            }

            viewHolder.card.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(mContext, DiseasesAndTreatmentsSearchWebViewActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("uri", uri);
                    intent.putExtra("search", mSearch);
                    mContext.startActivity(intent);
                }
            });

            animateView(viewHolder.card, i);
        }
        catch(Exception e)
        {
            Log.e("DiseasesAndTreatments", Log.getStackTraceString(e));
        }
    }

    @Override
    public int getItemCount()
    {
        return mResults.length();
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
}