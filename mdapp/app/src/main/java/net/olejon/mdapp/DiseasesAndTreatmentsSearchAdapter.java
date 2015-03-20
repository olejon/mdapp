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

    private final MyTools mTools;

    private final JSONArray mResults;

    private final String mSearch;

    private int lastPosition = -1;

    public DiseasesAndTreatmentsSearchAdapter(Context context, JSONArray results, String search)
    {
        mContext = context;

        mTools = new MyTools(mContext);

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
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.pubmed);
                    viewHolder.text.setText(text);

                    break;
                }
                case "webofscience":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.webofscience);
                    viewHolder.text.setText(text);

                    break;
                }
                case "medlineplus":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.medlineplus);
                    viewHolder.text.setText(text);

                    break;
                }
                case "wikipedia":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.wikipedia);
                    viewHolder.text.setText(text);

                    break;
                }
                case "uptodate":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.uptodate);
                    viewHolder.text.setText(text);

                    break;
                }
                case "bmj":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.bmj);
                    viewHolder.text.setText(text);

                    break;
                }
                case "nhi":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.nhi);
                    viewHolder.text.setText(text);

                    break;
                }
                case "sml":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.sml);
                    viewHolder.text.setText(text);

                    break;
                }
                case "forskning":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.forskning);
                    viewHolder.text.setText(text);

                    break;
                }
                case "helsebiblioteket":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.helsebiblioteket);
                    viewHolder.text.setText(text);

                    break;
                }
                case "oncolex":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.oncolex);
                    viewHolder.text.setText(text);

                    break;
                }
                case "tidsskriftet":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.tidsskriftet);
                    viewHolder.text.setText(text);

                    break;
                }
                case "helsenorge":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.helsenorge);
                    viewHolder.text.setText(text);

                    break;
                }
                case "brukerhandboken":
                {
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.brukerhandboken);
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
        if(position > lastPosition)
        {
            lastPosition = position;

            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.card);
            view.startAnimation(animation);
        }
    }
}
