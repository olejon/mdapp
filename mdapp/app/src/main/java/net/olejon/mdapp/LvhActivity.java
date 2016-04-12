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
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class LvhActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected()) mTools.showToast(getString(R.string.device_not_connected), 1);

        // Layout
        setContentView(R.layout.activity_lvh);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.lvh_toolbar);
        toolbar.setTitle(R.string.lvh_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.lvh_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.lvh_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_purple, R.color.accent_teal);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getCategories();
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.lvh_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new LvhAdapter(new JSONArray()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Get categories
        getCategories();
    }

    // Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Get categories
    private void getCategories()
    {
        final Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024);

        final Network network = new BasicNetwork(new HurlStack());

        final RequestQueue requestQueue = new RequestQueue(cache, network);

        requestQueue.start();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(getString(R.string.project_website_uri)+"api/1/lvh/", new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                requestQueue.stop();

                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                if(mTools.isTablet()) mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

                mRecyclerView.setAdapter(new LvhAdapter(response));
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                requestQueue.stop();

                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                mTools.showToast(getString(R.string.lvh_could_not_load_lvh), 1);

                finish();
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonArrayRequest);
    }

    // Adapter
    private class LvhAdapter extends RecyclerView.Adapter<LvhAdapter.CategoryViewHolder>
    {
        private final LayoutInflater mLayoutInflater;

        private final JSONArray mCategories;

        private int mLastPosition = -1;

        private LvhAdapter(JSONArray jsonArray)
        {
            mLayoutInflater =  (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            mCategories = jsonArray;
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

                        viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.red));
                        viewHolder.icon.setImageResource(R.drawable.ic_favorite_white_24dp);

                        break;
                    }
                    case 1:
                    {
                        color = "#9C27B0";
                        icon = "lvh_symptoms";

                        viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.purple));
                        viewHolder.icon.setImageResource(R.drawable.ic_stethoscope);

                        break;
                    }
                    case 2:
                    {
                        color = "#FF9800";
                        icon = "lvh_injuries";

                        viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.orange));
                        viewHolder.icon.setImageResource(R.drawable.ic_healing_white_24dp);

                        break;
                    }
                    case 3:
                    {
                        color = "#009688";
                        icon = "lvh_administrative";

                        viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.teal));
                        viewHolder.icon.setImageResource(R.drawable.ic_my_library_books_white_24dp);

                        break;
                    }
                    default:
                    {
                        color = "#009688";
                        icon = "lvh_administrative";

                        viewHolder.card.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.teal));
                        viewHolder.icon.setImageResource(R.drawable.ic_my_library_books_white_24dp);
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

                    final TextView textView = (TextView) mLayoutInflater.inflate(R.layout.activity_lvh_card_categories_item, null);
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

                animateCard(viewHolder.card, i);
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