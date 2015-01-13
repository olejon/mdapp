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
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class PoisoningsCardsAdapter extends RecyclerView.Adapter<PoisoningsCardsAdapter.PoisoningsViewHolder>
{
    private final Context mContext;

    private final JSONArray mPoisonings;

    private int lastPosition = -1;

    public PoisoningsCardsAdapter(Context context, JSONArray jsonArray)
    {
        mContext = context;

        mPoisonings = jsonArray;
    }

    static class PoisoningsViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView card;
        private final TextView type;
        private final TextView title;
        private final TextView text;
        private final TextView buttonUri;

        public PoisoningsViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.poisonings_cards_card);
            type = (TextView) view.findViewById(R.id.poisonings_cards_card_type);
            title = (TextView) view.findViewById(R.id.poisonings_cards_card_title);
            text = (TextView) view.findViewById(R.id.poisonings_cards_card_text);
            buttonUri = (TextView) view.findViewById(R.id.poisonings_cards_card_button_uri);
        }
    }

    @Override
    public PoisoningsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_poisonings_card, viewGroup, false);

        return new PoisoningsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PoisoningsViewHolder viewHolder, int i)
    {
        try
        {
            final JSONObject interactionJsonObject = mPoisonings.getJSONObject(i);

            final String type = interactionJsonObject.getString("type");
            final String title = interactionJsonObject.getString("title");
            final String text = interactionJsonObject.getString("text");
            final String uri = interactionJsonObject.getString("uri");

            viewHolder.title.setText(title);
            viewHolder.text.setText(text);

            if(type.equals("helsenorge"))
            {
                viewHolder.type.setText(mContext.getString(R.string.poisonings_cards_source_helsenorge));
            }
            else if(type.equals("helsebiblioteket"))
            {
                viewHolder.type.setText(mContext.getString(R.string.poisonings_cards_source_helsebiblioteket));
            }

            viewHolder.buttonUri.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(mContext, PoisoningsWebViewActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("uri", uri);
                    mContext.startActivity(intent);
                }
            });

            animateView(viewHolder.card, i);
        }
        catch(Exception e)
        {
            Log.e("PoisoningsCardsAdapter", Log.getStackTraceString(e));
        }
    }

    @Override
    public int getItemCount()
    {
        return mPoisonings.length();
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
