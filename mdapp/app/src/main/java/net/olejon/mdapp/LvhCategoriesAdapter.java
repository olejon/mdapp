package net.olejon.mdapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class LvhCategoriesAdapter extends RecyclerView.Adapter<LvhCategoriesAdapter.CategoryViewHolder>
{
    private final Context mContext;

    private final LayoutInflater mLayoutInflater;

    private final JSONArray mCategories;

    private final String mColor;
    private final String mIcon;

    private int lastPosition = -1;

    public LvhCategoriesAdapter(Context context, JSONArray jsonArray, String color, String icon)
    {
        mContext = context;

        mLayoutInflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mCategories = jsonArray;

        mColor = color;
        mIcon = icon;
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder
    {
        private final CardView card;
        private final ImageView icon;
        private final TextView title;
        private final LinearLayout categories;

        public CategoryViewHolder(View view)
        {
            super(view);

            card = (CardView) view.findViewById(R.id.lvh_categories_card);
            icon = (ImageView) view.findViewById(R.id.lvh_categories_card_icon);
            title = (TextView) view.findViewById(R.id.lvh_categories_card_title);
            categories = (LinearLayout) view.findViewById(R.id.lvh_categories_card_categories);
        }
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_lvh_categories_card, viewGroup, false);

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder viewHolder, int i)
    {
        try
        {
            final JSONObject categoriesJsonObject = mCategories.getJSONObject(i);

            viewHolder.card.setCardBackgroundColor(Color.parseColor(mColor));

            switch(mIcon)
            {
                case "lvh_urgent":
                {
                    viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lvh_urgent));
                    break;
                }
                case "lvh_symptoms":
                {
                    viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lvh_symptoms));
                    break;
                }
                case "lvh_injuries":
                {
                    viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lvh_injuries));
                    break;
                }
                case "lvh_administrative":
                {
                    viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lvh_administrative));
                    break;
                }
            }

            viewHolder.title.setText(categoriesJsonObject.getString("title"));

            viewHolder.categories.removeAllViews();

            final JSONArray itemsJsonArray = categoriesJsonObject.getJSONArray("items");

            for(int f = 0; f < itemsJsonArray.length(); f++)
            {
                final JSONObject categoryJsonObject = itemsJsonArray.getJSONObject(f);

                final String name = categoryJsonObject.getString("name");
                final String uri = categoryJsonObject.getString("uri");

                TextView textView = (TextView) mLayoutInflater.inflate(R.layout.activity_lvh_categories_card_categories_item, null);
                textView.setText(name);

                textView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(mContext, LvhCategoriesWebViewActivity.class);
                        intent.putExtra("title", name);
                        intent.putExtra("uri", uri);
                        mContext.startActivity(intent);
                    }
                });

                viewHolder.categories.addView(textView);
            }

            animateView(viewHolder.card, i);
        }
        catch(Exception e)
        {
            Log.e("LvhCategoriesAdapter", Log.getStackTraceString(e));
        }
    }

    @Override
    public int getItemCount()
    {
        return mCategories.length();
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