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
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class LvhAdapter extends RecyclerView.Adapter<LvhAdapter.CategoryViewHolder>
{
    private final Context mContext;

    private final LayoutInflater mLayoutInflater;

    private final JSONArray mCategories;

    private int lastPosition = -1;

    public LvhAdapter(Context context, JSONArray jsonArray)
    {
        mContext = context;

        mLayoutInflater =  (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mCategories = jsonArray;
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

            card = (CardView) view.findViewById(R.id.lvh_card);
            icon = (ImageView) view.findViewById(R.id.lvh_card_icon);
            title = (TextView) view.findViewById(R.id.lvh_card_title);
            categories = (LinearLayout) view.findViewById(R.id.lvh_card_categories);
        }
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_lvh_card, viewGroup, false);

        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoryViewHolder viewHolder, int i)
    {
        try
        {
            final String color;
            final String icon;

            final JSONObject categoriesJsonObject = mCategories.getJSONObject(i);

            switch(i)
            {
                case 0:
                {
                    color = "#F44336";
                    icon = "lvh_urgent";

                    viewHolder.card.setCardBackgroundColor(mContext.getResources().getColor(R.color.red));
                    viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lvh_urgent));

                    break;
                }
                case 1:
                {
                    color = "#9C27B0";
                    icon = "lvh_symptoms";

                    viewHolder.card.setCardBackgroundColor(mContext.getResources().getColor(R.color.purple));
                    viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lvh_symptoms));

                    break;
                }
                case 2:
                {
                    color = "#FF9800";
                    icon = "lvh_injuries";

                    viewHolder.card.setCardBackgroundColor(mContext.getResources().getColor(R.color.orange));
                    viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lvh_injuries));

                    break;
                }
                case 3:
                {
                    color = "#009688";
                    icon = "lvh_administrative";

                    viewHolder.card.setCardBackgroundColor(mContext.getResources().getColor(R.color.teal));
                    viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lvh_administrative));

                    break;
                }
                default:
                {
                    color = "#009688";
                    icon = "lvh_administrative";

                    viewHolder.card.setCardBackgroundColor(mContext.getResources().getColor(R.color.teal));
                    viewHolder.icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.lvh_administrative));
                }
            }

            viewHolder.title.setText(categoriesJsonObject.getString("title"));

            viewHolder.categories.removeAllViews();

            final JSONArray categoriesJsonArray = categoriesJsonObject.getJSONArray("categories");

            for(int f = 0; f < categoriesJsonArray.length(); f++)
            {
                final JSONObject categoryJsonObject = categoriesJsonArray.getJSONObject(f);

                final String title = categoryJsonObject.getString("title");
                final String subcategories = categoryJsonObject.getString("subcategories");

                TextView textView = (TextView) mLayoutInflater.inflate(R.layout.activity_lvh_card_categories_item, null);
                textView.setText(categoryJsonObject.getString("title"));

                textView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(mContext, LvhCategoriesActivity.class);
                        intent.putExtra("color", color);
                        intent.putExtra("icon", icon);
                        intent.putExtra("title", title);
                        intent.putExtra("subcategories", subcategories);
                        mContext.startActivity(intent);
                    }
                });

                viewHolder.categories.addView(textView);
            }

            animateView(viewHolder.card, i);
        }
        catch(Exception e)
        {
            Log.e("LvhAdapter", Log.getStackTraceString(e));
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