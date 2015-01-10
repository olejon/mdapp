package net.olejon.mdapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class PharmaciesLocationAdapter extends RecyclerView.Adapter<PharmaciesLocationAdapter.PharmaciesViewHolder>
{
    private final Context mContext;

    private final JSONArray mPharmacies;

    private int lastPosition = -1;

    public PharmaciesLocationAdapter(Context context, JSONArray jsonArray)
    {
        mContext = context;

        mPharmacies = jsonArray;
    }

    static class PharmaciesViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView card;
        private final TextView name;
        private final TextView information;
        private final TextView button;

        public PharmaciesViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.pharmacies_location_card);
            name = (TextView) view.findViewById(R.id.pharmacies_location_card_name);
            information = (TextView) view.findViewById(R.id.pharmacies_location_card_information);
            button = (TextView) view.findViewById(R.id.pharmacies_location_card_button);
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
        try
        {
            final JSONObject pharmacyJsonObject = mPharmacies.getJSONObject(i);

            final String coordinates = pharmacyJsonObject.getString("coordinates");
            final String name = pharmacyJsonObject.getString("name");

            viewHolder.name.setText(name);

            viewHolder.information.setText(Html.fromHtml(pharmacyJsonObject.getString("information")));
            viewHolder.information.setMovementMethod(LinkMovementMethod.getInstance());

            viewHolder.button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(mContext, PharmaciesLocationMapActivity.class);
                    intent.putExtra("name", name);
                    intent.putExtra("coordinates", coordinates);
                    mContext.startActivity(intent);
                }
            });

            animateView(viewHolder.card, i);
        }
        catch(Exception e)
        {
            Log.e("PharmaciesLocationAdapter", Log.getStackTraceString(e));
        }
    }

    @Override
    public int getItemCount()
    {
        return mPharmacies.length();
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