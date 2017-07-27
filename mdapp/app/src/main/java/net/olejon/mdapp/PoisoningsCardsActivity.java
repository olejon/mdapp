package net.olejon.mdapp;

/*

Copyright 2017 Ole Jon Bjørkum

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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class PoisoningsCardsActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mNoPoisoningsLayout;

    private String mSearchString;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected())
        {
            mTools.showToast(getString(R.string.device_not_connected), 1);

            finish();

            return;
        }

        // Intent
        Intent intent = getIntent();

        mSearchString = intent.getStringExtra("search");

        // Layout
        setContentView(R.layout.activity_poisonings_cards);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.poisonings_cards_toolbar);
        mToolbar.setTitle(getString(R.string.poisonings_cards_search, mSearchString));

        TextView mToolbarTextView = (TextView) mToolbar.getChildAt(1);
        mToolbarTextView.setEllipsize(TextUtils.TruncateAt.MIDDLE);

        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.poisonings_cards_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.poisonings_cards_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_purple, R.color.accent_teal);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                search(mSearchString);
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.poisonings_cards_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new PoisoningsCardsAdapter(new JSONArray()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // No poisonings
        mNoPoisoningsLayout = (LinearLayout) findViewById(R.id.poisonings_cards_no_poisonings);

        Button noPoisoningsHelsenorgeButton = (Button) findViewById(R.id.poisonings_cards_search_on_helsenorge);
        Button noPoisoningsHelsebiblioteketButton = (Button) findViewById(R.id.poisonings_cards_search_on_helsebiblioteket);

        noPoisoningsHelsenorgeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(mContext, MainWebViewActivity.class);
                intent.putExtra("title", getString(R.string.poisonings_cards_search_on_helsenorge));
                intent.putExtra("uri", "https://helsenorge.no/giftinformasjon/");
                mContext.startActivity(intent);
            }
        });

        noPoisoningsHelsebiblioteketButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(mContext, MainWebViewActivity.class);
                intent.putExtra("title", getString(R.string.poisonings_cards_search_on_helsebiblioteket));
                intent.putExtra("uri", "http://www.helsebiblioteket.no/forgiftninger/");
                mContext.startActivity(intent);
            }
        });

        // Search
        search(mSearchString);

        // Correct
        try
        {
            final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

            requestQueue.start();

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mTools.getApiUri()+"api/1/correct/?search="+URLEncoder.encode(mSearchString, "utf-8"), null, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response)
                {
                    requestQueue.stop();

                    try
                    {
                        final String correctSearchString = response.getString("correct");

                        if(!correctSearchString.equals(""))
                        {
                            new MaterialDialog.Builder(mContext).title(R.string.correct_dialog_title).content(getString(R.string.correct_dialog_message, correctSearchString)).positiveText(R.string.correct_dialog_positive_button).negativeText(R.string.correct_dialog_negative_button).onPositive(new MaterialDialog.SingleButtonCallback()
                            {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                                {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(PoisoningsSQLiteHelper.COLUMN_STRING, correctSearchString);

                                    SQLiteDatabase sqLiteDatabase = new PoisoningsSQLiteHelper(mContext).getWritableDatabase();

                                    sqLiteDatabase.delete(PoisoningsSQLiteHelper.TABLE, PoisoningsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(mSearchString)+" COLLATE NOCASE", null);
                                    sqLiteDatabase.insert(PoisoningsSQLiteHelper.TABLE, null, contentValues);

                                    sqLiteDatabase.close();

                                    mToolbar.setTitle(getString(R.string.poisonings_cards_search, correctSearchString));

                                    mProgressBar.setVisibility(View.VISIBLE);

                                    mNoPoisoningsLayout.setVisibility(View.GONE);
                                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);

                                    search(correctSearchString);
                                }
                            }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e("PoisoningsCardsActivity", Log.getStackTraceString(e));
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    requestQueue.stop();

                    Log.e("PoisoningsCardsActivity", error.toString());
                }
            });

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsonObjectRequest);
        }
        catch(Exception e)
        {
            Log.e("PoisoningsCardsActivity", Log.getStackTraceString(e));
        }
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_poisonings_cards, menu);
        return true;
    }

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
            case R.id.poisonings_cards_menu_call:
            {
                try
                {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+4722591300"));
                    startActivity(intent);
                }
                catch(Exception e)
                {
                    new MaterialDialog.Builder(mContext).title(R.string.device_not_supported_dialog_title).content(R.string.device_not_supported_dialog_message).positiveText(R.string.device_not_supported_dialog_positive_button).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
                }

                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Search
    private void search(final String searchString)
    {
        try
        {
            final RequestQueue requestQueue = new RequestQueue(new DiskBasedCache(getCacheDir(), 0), new BasicNetwork(new HurlStack()));

            requestQueue.start();

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(mTools.getApiUri()+"api/1/poisonings/?search="+URLEncoder.encode(searchString, "utf-8"), new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
                    requestQueue.stop();

                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    if(response.length() == 0)
                    {
                        mSwipeRefreshLayout.setVisibility(View.GONE);
                        mNoPoisoningsLayout.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        if(mTools.isTablet())
                        {
                            int spanCount = (response.length() == 1) ? 1 : 2;

                            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
                        }

                        mRecyclerView.setAdapter(new PoisoningsCardsAdapter(response));

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PoisoningsSQLiteHelper.COLUMN_STRING, searchString);

                        SQLiteDatabase sqLiteDatabase = new PoisoningsSQLiteHelper(mContext).getWritableDatabase();

                        sqLiteDatabase.delete(PoisoningsSQLiteHelper.TABLE, PoisoningsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(searchString)+" COLLATE NOCASE", null);
                        sqLiteDatabase.insert(PoisoningsSQLiteHelper.TABLE, null, contentValues);

                        sqLiteDatabase.close();
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    requestQueue.stop();

                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    mTools.showToast(getString(R.string.poisonings_cards_something_went_wrong), 1);

                    Log.e("PoisoningsCardsActivity", error.toString());

                    finish();
                }
            });

            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsonArrayRequest);
        }
        catch(Exception e)
        {
            Log.e("PoisoningsCardsActivity", Log.getStackTraceString(e));
        }
    }

    // Adapter
    class PoisoningsCardsAdapter extends RecyclerView.Adapter<PoisoningsCardsAdapter.PoisoningsViewHolder>
    {
        final JSONArray mPoisonings;

        int mLastPosition = -1;

        PoisoningsCardsAdapter(JSONArray jsonArray)
        {
            mPoisonings = jsonArray;
        }

        class PoisoningsViewHolder extends RecyclerView.ViewHolder
        {
            final CardView card;
            final TextView type;
            final TextView title;
            final TextView text;
            final TextView uri;

            PoisoningsViewHolder(View view)
            {
                super(view);

                card = (CardView) view.findViewById(R.id.poisonings_cards_card);
                type = (TextView) view.findViewById(R.id.poisonings_cards_card_type);
                title = (TextView) view.findViewById(R.id.poisonings_cards_card_title);
                text = (TextView) view.findViewById(R.id.poisonings_cards_card_text);
                uri = (TextView) view.findViewById(R.id.poisonings_cards_card_button_uri);
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
                JSONObject interactionJsonObject = mPoisonings.getJSONObject(i);

                final String title = interactionJsonObject.getString("title");
                final String uri = interactionJsonObject.getString("uri");
                String type = interactionJsonObject.getString("type");
                String text = interactionJsonObject.getString("text");

                viewHolder.title.setText(title);
                viewHolder.text.setText(text);

                if(type.equals("helsenorge"))
                {
                    viewHolder.type.setText(mContext.getString(R.string.poisonings_cards_source_helsenorge));
                }
                else if(type.equals("helsebiblioteket"))
                {
                    viewHolder.type.setText(mContext.getString(R.string.poisonings_cards_source_helsebiblioteket));
                }

                viewHolder.title.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(mContext, MainWebViewActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("uri", uri);
                        mContext.startActivity(intent);
                    }
                });

                viewHolder.uri.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(mContext, MainWebViewActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("uri", uri);
                        mContext.startActivity(intent);
                    }
                });

                animateCard(viewHolder.card, i);
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

        private void animateCard(View view, int position)
        {
            if(position > mLastPosition)
            {
                mLastPosition = position;

                view.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.card));
            }
        }
    }
}