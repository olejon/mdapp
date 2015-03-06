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
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationsFromSlvAdapter extends RecyclerView.Adapter<NotificationsFromSlvAdapter.NotificationViewHolder>
{
    private final Context mContext;

    private final MyTools mTools;

    private final LayoutInflater mLayoutInflater;

    private final JSONArray mNotifications;

    private int lastPosition = -1;

    public NotificationsFromSlvAdapter(Context context, JSONArray jsonArray)
    {
        mContext = context;

        mTools = new MyTools(mContext);

        mLayoutInflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mNotifications = jsonArray;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView card;
        private final TextView title;
        private final TextView date;
        private final TextView type;
        private final TextView message;
        private final LinearLayout medications;
        private final View uriSeparator;
        private final TextView uri;

        public NotificationViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.notifications_from_slv_card);
            title = (TextView) view.findViewById(R.id.notifications_from_slv_card_title);
            date = (TextView) view.findViewById(R.id.notifications_from_slv_card_date);
            type = (TextView) view.findViewById(R.id.notifications_from_slv_card_type);
            message = (TextView) view.findViewById(R.id.notifications_from_slv_card_message);
            medications = (LinearLayout) view.findViewById(R.id.notifications_from_slv_card_medications);
            uriSeparator = view.findViewById(R.id.notifications_from_slv_card_uri_separator);
            uri = (TextView) view.findViewById(R.id.notifications_from_slv_card_button);
        }
    }

    @Override
    public NotificationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_notifications_from_slv_card, viewGroup, false);

        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationViewHolder viewHolder, int i)
    {
        try
        {
            final JSONObject notificationsJsonObject = mNotifications.getJSONObject(i);

            viewHolder.title.setText(notificationsJsonObject.getString("title"));
            viewHolder.date.setText(notificationsJsonObject.getString("date"));
            viewHolder.type.setText(notificationsJsonObject.getString("type"));
            viewHolder.message.setText(notificationsJsonObject.getString("message"));

            viewHolder.medications.removeAllViews();

            final JSONArray medicationsJsonArray = notificationsJsonObject.getJSONArray("medications");

            int medicationsCount = medicationsJsonArray.length();

            if(medicationsCount != 0)
            {
                for(int n = 0; n < medicationsCount; n++)
                {
                    final JSONObject medicationJsonObject = medicationsJsonArray.getJSONObject(n);

                    final String uri = medicationJsonObject.getString("uri");

                    LinearLayout linearLayout = (LinearLayout) mLayoutInflater.inflate(R.layout.card_item_separator, null);
                    viewHolder.medications.addView(linearLayout);

                    TextView textView = (TextView) mLayoutInflater.inflate(R.layout.activity_notifications_from_slv_card_medications_item, null);
                    textView.setText(medicationJsonObject.getString("name"));

                    textView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            mTools.getMedicationWithFullContent(uri);
                        }
                    });

                    viewHolder.medications.addView(textView);
                }
            }

            final String title = notificationsJsonObject.getString("title");
            final String uri = notificationsJsonObject.getString("uri");

            if(uri.equals(""))
            {
                viewHolder.uriSeparator.setVisibility(View.GONE);
                viewHolder.uri.setVisibility(View.GONE);
            }
            else
            {
                viewHolder.uriSeparator.setVisibility(View.VISIBLE);
                viewHolder.uri.setVisibility(View.VISIBLE);

                viewHolder.title.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(uri.matches("^https?://.*?\\.pdf$"))
                        {
                            mTools.showToast(mContext.getString(R.string.notifications_from_slv_downloading_pdf), 1);

                            mTools.downloadFile(title, uri);
                        }
                        else
                        {
                            Intent intent = new Intent(mContext, NotificationsFromSlvWebViewActivity.class);
                            intent.putExtra("title", title);
                            intent.putExtra("uri", uri);
                            mContext.startActivity(intent);
                        }
                    }
                });

                viewHolder.uri.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if(uri.matches("^https?://.*?\\.pdf$"))
                        {
                            mTools.showToast(mContext.getString(R.string.notifications_from_slv_downloading_pdf), 1);

                            mTools.downloadFile(title, uri);
                        }
                        else
                        {
                            Intent intent = new Intent(mContext, NotificationsFromSlvWebViewActivity.class);
                            intent.putExtra("title", title);
                            intent.putExtra("uri", uri);
                            mContext.startActivity(intent);
                        }
                    }
                });
            }

            animateView(viewHolder.card, i);
        }
        catch(Exception e)
        {
            Log.e("NotificationsFromSlv", Log.getStackTraceString(e));
        }
    }

    @Override
    public int getItemCount()
    {
        return mNotifications.length();
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