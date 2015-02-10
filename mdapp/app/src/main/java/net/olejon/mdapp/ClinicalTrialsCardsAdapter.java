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

public class ClinicalTrialsCardsAdapter extends RecyclerView.Adapter<ClinicalTrialsCardsAdapter.ClinicalTrialsViewHolder>
{
    private final Context mContext;

    private final JSONArray mClinicalTrials;

    private int lastPosition = -1;

    public ClinicalTrialsCardsAdapter(Context context, JSONArray jsonArray)
    {
        mContext = context;

        mClinicalTrials = jsonArray;
    }

    static class ClinicalTrialsViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView card;
        private final TextView title;
        private final TextView status;
        private final TextView conditions;
        private final TextView intervention;
        private final TextView button;

        public ClinicalTrialsViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.clinicaltrials_cards_card);
            title = (TextView) view.findViewById(R.id.clinicaltrials_cards_card_title);
            status = (TextView) view.findViewById(R.id.clinicaltrials_cards_card_status);
            conditions = (TextView) view.findViewById(R.id.clinicaltrials_cards_card_conditions);
            intervention = (TextView) view.findViewById(R.id.clinicaltrials_cards_card_intervention);
            button = (TextView) view.findViewById(R.id.clinicaltrials_cards_card_button_uri);
        }
    }

    @Override
    public ClinicalTrialsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_clinicaltrials_card, viewGroup, false);

        return new ClinicalTrialsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClinicalTrialsViewHolder viewHolder, int i)
    {
        try
        {
            final JSONObject clinicalTrialsJsonObject = mClinicalTrials.getJSONObject(i);

            final String title = clinicalTrialsJsonObject.getString("study");
            final String status = clinicalTrialsJsonObject.getString("status");
            final String conditions = clinicalTrialsJsonObject.getString("conditions");
            final String intervention = clinicalTrialsJsonObject.getString("intervention");
            final String uri = clinicalTrialsJsonObject.getString("uri");

            viewHolder.title.setText(title);
            viewHolder.status.setText(status);
            viewHolder.conditions.setText(conditions);
            viewHolder.intervention.setText(intervention);

            viewHolder.button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(mContext, ClinicalTrialsWebViewActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("uri", uri);
                    mContext.startActivity(intent);
                }
            });

            animateView(viewHolder.card, i);
        }
        catch(Exception e)
        {
            Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
        }
    }

    @Override
    public int getItemCount()
    {
        return mClinicalTrials.length();
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
