package net.olejon.mdapp;

/*

Copyright 2016 Ole Jon Bjørkum

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see http://www.gnu.org/licenses/.

*/

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

public class LvhCategoriesActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Transition
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

        // Intent
        final Intent intent = getIntent();

        final String categoryColor = intent.getStringExtra("color");
        final String categoryIcon = intent.getStringExtra("icon");
        final String categoryTitle = intent.getStringExtra("title");

        JSONArray subcategories;

        try
        {
            subcategories = new JSONArray(intent.getStringExtra("subcategories"));
        }
        catch(Exception e)
        {
            subcategories = new JSONArray();

            Log.e("LvhCategoriesActivity", Log.getStackTraceString(e));
        }

        // Layout
        setContentView(R.layout.activity_lvh_categories);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.lvh_categories_toolbar);
        toolbar.setTitle(categoryTitle);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Recycler view
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lvh_categories_cards);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        if(mTools.isTablet())
        {
            int spanCount = (subcategories.length() == 1) ? 1 : 2;

            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
        }

        // Get categories
        recyclerView.setAdapter(new LvhCategoriesAdapter(subcategories, categoryColor, categoryIcon));
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    // Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                finish();
                overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Adapter
    private class LvhCategoriesAdapter extends RecyclerView.Adapter<LvhCategoriesAdapter.CategoryViewHolder>
    {
        private final LayoutInflater mLayoutInflater;

        private final JSONArray mCategories;

        private final String mColor;
        private final String mIcon;

        private int mLastPosition = -1;

        private LvhCategoriesAdapter(JSONArray jsonArray, String color, String icon)
        {
            mLayoutInflater =  (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mCategories = jsonArray;

            mColor = color;
            mIcon = icon;
        }

        class CategoryViewHolder extends RecyclerView.ViewHolder
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
                        viewHolder.icon.setImageResource(R.drawable.ic_favorite_white_24dp);
                        break;
                    }
                    case "lvh_symptoms":
                    {
                        viewHolder.icon.setImageResource(R.drawable.ic_stethoscope);
                        break;
                    }
                    case "lvh_injuries":
                    {
                        viewHolder.icon.setImageResource(R.drawable.ic_healing_white_24dp);
                        break;
                    }
                    case "lvh_administrative":
                    {
                        viewHolder.icon.setImageResource(R.drawable.ic_my_library_books_white_24dp);
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

                    final TextView textView = (TextView) mLayoutInflater.inflate(R.layout.activity_lvh_categories_card_categories_item, null);
                    textView.setText(name);

                    textView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            Intent intent = new Intent(mContext, MainWebViewActivity.class);
                            intent.putExtra("title", name);
                            intent.putExtra("uri", uri);
                            mContext.startActivity(intent);
                        }
                    });

                    viewHolder.categories.addView(textView);
                }

                animateCard(viewHolder.card, i);
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

        private void animateCard(View view, int position)
        {
            if(position > mLastPosition)
            {
                mLastPosition = position;

                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.card);
                view.startAnimation(animation);
            }
        }
    }
}