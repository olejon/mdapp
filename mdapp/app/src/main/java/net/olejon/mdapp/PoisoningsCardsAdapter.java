package net.olejon.mdapp;

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
        private final TextView title;
        private final TextView text;
        private final TextView buttonHelsebiblioteketUri;
        private final TextView buttonUri;

        public PoisoningsViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.poisonings_cards_card);
            title = (TextView) view.findViewById(R.id.poisonings_cards_card_title);
            text = (TextView) view.findViewById(R.id.poisonings_cards_card_text);
            buttonHelsebiblioteketUri = (TextView) view.findViewById(R.id.poisonings_cards_card_button_helsebiblioteket_uri);
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

            final String title = interactionJsonObject.getString("title");
            final String text = interactionJsonObject.getString("text");
            final String uri = interactionJsonObject.getString("uri");

            viewHolder.title.setText(title);
            viewHolder.text.setText(text);

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

            viewHolder.buttonHelsebiblioteketUri.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(mContext, PoisoningsWebViewActivity.class);
                    intent.putExtra("title", "Helsebiblioteket - Forgiftninger");
                    intent.putExtra("uri", "http://www.helsebiblioteket.no/forgiftninger/");
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
