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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class InteractionsCardsAdapter extends RecyclerView.Adapter<InteractionsCardsAdapter.InteractionsViewHolder>
{
    private final Context mContext;

    private final MyTools mTools;

    private final ProgressBar mProgressBar;

    private final JSONArray mInteractions;

    private int mLastPosition = -1;

    public InteractionsCardsAdapter(Context context, ProgressBar progressBar, JSONArray jsonArray)
    {
        mContext = context;

        mTools = new MyTools(mContext);

        mProgressBar = progressBar;

        mInteractions = jsonArray;
    }

    static class InteractionsViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView card;
        private final ImageView icon;
        private final TextView title;
        private final TextView text;
        private final TextView relevance;
        private final TextView buttonHandling;
        private final TextView buttonPubmedSearchUri;
        private final TextView buttonUri;

        public InteractionsViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.interactions_cards_card);
            icon = (ImageView) view.findViewById(R.id.interactions_cards_card_icon);
            title = (TextView) view.findViewById(R.id.interactions_cards_card_title);
            text = (TextView) view.findViewById(R.id.interactions_cards_card_text);
            relevance = (TextView) view.findViewById(R.id.interactions_cards_card_relevance);
            buttonHandling = (TextView) view.findViewById(R.id.interactions_cards_card_button_handling);
            buttonPubmedSearchUri = (TextView) view.findViewById(R.id.interactions_cards_card_button_pubmed_search_uri);
            buttonUri = (TextView) view.findViewById(R.id.interactions_cards_card_button_uri);
        }
    }

    @Override
    public InteractionsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_interactions_card, viewGroup, false);
        return new InteractionsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(InteractionsViewHolder viewHolder, int i)
    {
        try
        {
            final JSONObject interactionJsonObject = mInteractions.getJSONObject(i);

            final String color = interactionJsonObject.getString("color");
            final String title = interactionJsonObject.getString("title");
            final String text = interactionJsonObject.getString("text");
            final String pubmedSearchUri = interactionJsonObject.getString("pubmed_search_uri");
            final String uri = interactionJsonObject.getString("uri");

            switch(color)
            {
                case "red":
                {
                    viewHolder.card.setCardBackgroundColor(mContext.getResources().getColor(R.color.red));
                    viewHolder.relevance.setText(mContext.getString(R.string.interactions_cards_relevance)+": "+mContext.getString(R.string.interactions_cards_relevance_red));
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.ic_error_white_24dp);

                    break;
                }
                case "orange":
                {
                    viewHolder.card.setCardBackgroundColor(mContext.getResources().getColor(R.color.orange));
                    viewHolder.relevance.setText(mContext.getString(R.string.interactions_cards_relevance)+": "+mContext.getString(R.string.interactions_cards_relevance_orange));
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.ic_warning_white_24dp);

                    break;
                }
                case "green":
                {
                    viewHolder.card.setCardBackgroundColor(mContext.getResources().getColor(R.color.green));
                    viewHolder.relevance.setText(mContext.getString(R.string.interactions_cards_relevance)+": "+mContext.getString(R.string.interactions_cards_relevance_green));
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.ic_check_white_24dp);

                    break;
                }
                default:
                {
                    viewHolder.card.setCardBackgroundColor(mContext.getResources().getColor(R.color.red));
                    viewHolder.relevance.setText(mContext.getString(R.string.interactions_cards_relevance)+": "+mContext.getString(R.string.interactions_cards_relevance_red));
                    mTools.setImageDrawable(viewHolder.icon, R.drawable.ic_error_white_24dp);

                    break;
                }
            }

            viewHolder.title.setText(title);
            viewHolder.text.setText(text);

            viewHolder.buttonHandling.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(mTools.isDeviceConnected())
                    {
                        try
                        {
                            mProgressBar.setVisibility(View.VISIBLE);

                            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mContext.getString(R.string.project_website_uri)+"api/1/interactions/handling/?uri="+ URLEncoder.encode(uri, "utf-8"), new Response.Listener<JSONObject>()
                            {
                                @Override
                                public void onResponse(JSONObject response)
                                {
                                    mProgressBar.setVisibility(View.GONE);

                                    try
                                    {
                                        new MaterialDialog.Builder(mContext).title(mContext.getString(R.string.interactions_cards_handling_dialog_title)).content(response.getString("handling")).positiveText(mContext.getString(R.string.interactions_cards_handling_dialog_positive_button)).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
                                    }
                                    catch(Exception e)
                                    {
                                        mTools.showToast(mContext.getString(R.string.interactions_cards_no_handling_information_available), 1);

                                        Log.e("InteractionsCards", Log.getStackTraceString(e));
                                    }
                                }
                            }, new Response.ErrorListener()
                            {
                                @Override
                                public void onErrorResponse(VolleyError error)
                                {
                                    mProgressBar.setVisibility(View.GONE);

                                    mTools.showToast(mContext.getString(R.string.interactions_cards_no_handling_information_available), 1);

                                    Log.e("InteractionsCards", error.toString());
                                }
                            });

                            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                            requestQueue.add(jsonObjectRequest);
                        }
                        catch(Exception e)
                        {
                            Log.e("InteractionsCards", Log.getStackTraceString(e));
                        }
                    }
                    else
                    {
                        mTools.showToast(mContext.getString(R.string.device_not_connected), 1);
                    }
                }
            });

            viewHolder.buttonPubmedSearchUri.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(mContext, InteractionsWebViewActivity.class);
                    intent.putExtra("title", "PubMed");
                    intent.putExtra("uri", pubmedSearchUri);
                    mContext.startActivity(intent);
                }
            });

            viewHolder.buttonUri.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(mContext, InteractionsWebViewActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("uri", uri);
                    mContext.startActivity(intent);
                }
            });

            animateView(viewHolder.card, i);
        }
        catch(Exception e)
        {
            Log.e("InteractionsCards", Log.getStackTraceString(e));
        }
    }

    @Override
    public int getItemCount()
    {
        return mInteractions.length();
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
