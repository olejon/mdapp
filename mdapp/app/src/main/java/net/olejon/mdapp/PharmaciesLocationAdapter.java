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
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.net.URLEncoder;

public class PharmaciesLocationAdapter extends RecyclerView.Adapter<PharmaciesLocationAdapter.PharmaciesViewHolder>
{
    private final Context mContext;

    private final Cursor mCursor;

    private boolean isGoogleMapsInstalled = false;

    private int mLastPosition = -1;

    public PharmaciesLocationAdapter(Context context, Cursor cursor)
    {
        mContext = context;

        mCursor = cursor;

        try
        {
            mContext.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );

            isGoogleMapsInstalled = true;
        }
        catch(Exception e)
        {
            Log.e("PharmaciesLocation", Log.getStackTraceString(e));
        }
    }

    static class PharmaciesViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView card;
        private final TextView name;
        private final TextView address;
        private final TextView mapButton;
        private final TextView contactButton;

        public PharmaciesViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.pharmacies_location_card);
            name = (TextView) view.findViewById(R.id.pharmacies_location_card_name);
            address = (TextView) view.findViewById(R.id.pharmacies_location_card_address);
            mapButton = (TextView) view.findViewById(R.id.pharmacies_location_card_map_button);
            contactButton = (TextView) view.findViewById(R.id.pharmacies_location_card_contact_button);
        }
    }

    @Override
    public PharmaciesViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_pharmacies_location_card, viewGroup, false);
        return new PharmaciesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PharmaciesViewHolder viewHolder, int i)
    {
        if(mCursor.moveToPosition(i))
        {
            final String name = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.PHARMACIES_COLUMN_NAME));
            final String address = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.PHARMACIES_COLUMN_ADDRESS));

            viewHolder.name.setText(name);
            viewHolder.address.setText(address);

            viewHolder.mapButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if(isGoogleMapsInstalled)
                    {
                        Intent intent = new Intent(mContext, PharmaciesLocationMapActivity.class);
                        intent.putExtra("name", name);
                        intent.putExtra("address", address);
                        mContext.startActivity(intent);
                    }
                    else
                    {
                        new MaterialDialog.Builder(mContext).title(mContext.getString(R.string.device_not_supported_dialog_title)).content(mContext.getString(R.string.device_not_supported_dialog_message)).positiveText(mContext.getString(R.string.device_not_supported_dialog_positive_button)).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
                    }
                }
            });

            viewHolder.contactButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    try
                    {
                        Intent intent = new Intent(mContext, MainWebViewActivity.class);
                        intent.putExtra("title", name);
                        intent.putExtra("uri", "http://www.gulesider.no/finn:"+URLEncoder.encode(name, "utf-8"));
                        mContext.startActivity(intent);
                    }
                    catch(Exception e)
                    {
                        Log.e("PharmaciesLocation", Log.getStackTraceString(e));
                    }
                }
            });

            animateView(viewHolder.card, i);
        }
    }

    @Override
    public int getItemCount()
    {
        return (mCursor == null) ? 0 : mCursor.getCount();
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