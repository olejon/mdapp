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
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

public class MedicationPicturesAdapter extends RecyclerView.Adapter<MedicationPicturesAdapter.MedicationViewHolder>
{
    private final Context mContext;

    private final JSONArray mNotifications;

    private int lastPosition = -1;

    public MedicationPicturesAdapter(Context context, JSONArray jsonArray)
    {
        mContext = context;

        mNotifications = jsonArray;
    }

    static class MedicationViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView card;
        private final TextView name;
        private final ImageView picture;
        private final TextView marking;
        private final TextView shape;
        private final TextView splitting;
        private final TextView measurement;

        public MedicationViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.medication_pictures_card);
            name = (TextView) view.findViewById(R.id.medication_pictures_card_name);
            picture = (ImageView) view.findViewById(R.id.medication_pictures_card_picture);
            marking = (TextView) view.findViewById(R.id.medication_pictures_card_marking);
            shape = (TextView) view.findViewById(R.id.medication_pictures_card_shape);
            splitting = (TextView) view.findViewById(R.id.medication_pictures_card_splitting);
            measurement = (TextView) view.findViewById(R.id.medication_pictures_card_measurement);
        }
    }

    @Override
    public MedicationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_medication_pictures_card, viewGroup, false);

        return new MedicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MedicationViewHolder viewHolder, int i)
    {
        try
        {
            final JSONObject notificationsJsonObject = mNotifications.getJSONObject(i);

            viewHolder.name.setText(notificationsJsonObject.getString("name"));

            Picasso.with(mContext).load(notificationsJsonObject.getString("uri")).into(viewHolder.picture);

            viewHolder.picture.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    try
                    {
                        Intent intent = new Intent(mContext, MedicationPictureActivity.class);
                        intent.putExtra("uri", notificationsJsonObject.getString("uri"));
                        mContext.startActivity(intent);
                    }
                    catch(Exception e)
                    {
                        Log.e("MedicationPicturesAdapter", Log.getStackTraceString(e));
                    }
                }
            });

            viewHolder.marking.setText(notificationsJsonObject.getString("marking"));
            viewHolder.shape.setText(notificationsJsonObject.getString("shape"));
            viewHolder.splitting.setText(notificationsJsonObject.getString("splitting"));
            viewHolder.measurement.setText(notificationsJsonObject.getString("measurement"));

            animateView(viewHolder.card, i);
        }
        catch(Exception e)
        {
            Log.e("MedicationPicturesAdapter", Log.getStackTraceString(e));
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