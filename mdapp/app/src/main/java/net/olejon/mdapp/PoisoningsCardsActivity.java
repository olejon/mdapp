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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

public class PoisoningsCardsActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mNoPoisoningsLayout;

    private String searchString;

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
        final Intent intent = getIntent();

        searchString = intent.getStringExtra("search");

        // Layout
        setContentView(R.layout.activity_poisonings_cards);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.poisonings_cards_toolbar);
        mToolbar.setTitle(getString(R.string.poisonings_cards_search)+": \""+searchString+"\"");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.poisonings_cards_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.poisonings_cards_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_green, R.color.accent_purple, R.color.accent_orange);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                search(searchString, false);
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.poisonings_cards_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new PoisoningsCardsAdapter(mContext, new JSONArray()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // No poisonings
        mNoPoisoningsLayout = (LinearLayout) findViewById(R.id.poisonings_cards_no_poisonings);

        Button noPoisoningsHelsenorgeButton = (Button) findViewById(R.id.poisonings_cards_check_on_helsenorge);
        Button noPoisoningsHelsebiblioteketButton = (Button) findViewById(R.id.poisonings_cards_check_on_helsebiblioteket);

        noPoisoningsHelsenorgeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    Intent intent = new Intent(mContext, PoisoningsWebViewActivity.class);
                    intent.putExtra("title", getString(R.string.poisonings_cards_search)+": \""+searchString+"\"");
                    intent.putExtra("uri", "https://helsenorge.no/sok/giftinformasjon/?k="+URLEncoder.encode(searchString.toLowerCase(), "utf-8"));
                    mContext.startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.e("PoisoningsCardsActivity", Log.getStackTraceString(e));
                }
            }
        });

        noPoisoningsHelsebiblioteketButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    Intent intent = new Intent(mContext, PoisoningsWebViewActivity.class);
                    intent.putExtra("title", getString(R.string.poisonings_cards_search)+": \""+searchString+"\"");
                    intent.putExtra("uri", "http://www.helsebiblioteket.no/forgiftninger/alle-anbefalinger?cx=005475784484624053973%3A3bnj2dj_uei&ie=UTF-8&q="+URLEncoder.encode(searchString.toLowerCase(), "utf-8")+"&sa=S%C3%B8k");
                    mContext.startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.e("PoisoningsCardsActivity", Log.getStackTraceString(e));
                }
            }
        });

        // Search
        search(searchString, true);

        // Correct
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        try
        {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website_uri)+"api/1/correct/?search="+URLEncoder.encode(searchString, "utf-8"), new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response)
                {
                    try
                    {
                        final String correctSearchString = response.getString("correct");

                        if(!correctSearchString.equals(""))
                        {
                            new MaterialDialog.Builder(mContext).title(getString(R.string.correct_dialog_title)).content(Html.fromHtml(getString(R.string.correct_dialog_message)+":<br><br><b>"+correctSearchString+"</b>")).positiveText(getString(R.string.correct_dialog_positive_button)).negativeText(getString(R.string.correct_dialog_negative_button)).callback(new MaterialDialog.ButtonCallback()
                            {
                                @Override
                                public void onPositive(MaterialDialog dialog)
                                {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(PoisoningsSQLiteHelper.COLUMN_STRING, correctSearchString);

                                    SQLiteDatabase sqLiteDatabase = new PoisoningsSQLiteHelper(mContext).getWritableDatabase();

                                    sqLiteDatabase.delete(PoisoningsSQLiteHelper.TABLE, PoisoningsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(searchString)+" COLLATE NOCASE", null);
                                    sqLiteDatabase.insert(PoisoningsSQLiteHelper.TABLE, null, contentValues);

                                    sqLiteDatabase.close();

                                    mToolbar.setTitle(getString(R.string.poisonings_cards_search)+": \""+correctSearchString+"\"");

                                    mProgressBar.setVisibility(View.VISIBLE);

                                    mNoPoisoningsLayout.setVisibility(View.GONE);
                                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);

                                    search(correctSearchString, true);
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
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+4722591300"));
                startActivity(intent);
                return true;
            }
            case R.id.poisonings_cards_menu_helsenorge_uri:
            {
                try
                {
                    mTools.openUri("https://helsenorge.no/sok/giftinformasjon/?k="+URLEncoder.encode(searchString.toLowerCase(), "utf-8"));
                }
                catch(Exception e)
                {
                    Log.e("PoisoningsCardsActivity", Log.getStackTraceString(e));
                }

                return true;
            }
            case R.id.poisonings_cards_menu_helsebiblioteket_uri:
            {
                try
                {
                    mTools.openUri("http://www.helsebiblioteket.no/forgiftninger/alle-anbefalinger?cx=005475784484624053973%3A3bnj2dj_uei&ie=UTF-8&q="+URLEncoder.encode(searchString.toLowerCase(), "utf-8")+"&sa=S%C3%B8k");
                }
                catch(Exception e)
                {
                    Log.e("PoisoningsCardsActivity", Log.getStackTraceString(e));
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
    private void search(final String string, boolean cache)
    {
        try
        {
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            String apiUri = getString(R.string.project_website_uri)+"api/1/poisonings/?search="+URLEncoder.encode(string.toLowerCase(), "utf-8");

            if(!cache) requestQueue.getCache().remove(apiUri);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(apiUri, new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
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

                        mRecyclerView.setAdapter(new PoisoningsCardsAdapter(mContext, response));

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PoisoningsSQLiteHelper.COLUMN_STRING, string);

                        SQLiteDatabase sqLiteDatabase = new PoisoningsSQLiteHelper(mContext).getWritableDatabase();

                        sqLiteDatabase.delete(PoisoningsSQLiteHelper.TABLE, PoisoningsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(string)+" COLLATE NOCASE", null);
                        sqLiteDatabase.insert(PoisoningsSQLiteHelper.TABLE, null, contentValues);

                        sqLiteDatabase.close();
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    mTools.showToast(getString(R.string.poisonings_cards_something_went_wrong), 1);

                    finish();

                    Log.e("PoisoningsCardsActivity", error.toString());
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
}
