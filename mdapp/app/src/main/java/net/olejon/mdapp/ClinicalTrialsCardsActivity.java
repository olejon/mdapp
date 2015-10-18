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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.DialogAction;
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

public class ClinicalTrialsCardsActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private LinearLayout mNoClinicalTrialsLayout;

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
        setContentView(R.layout.activity_clinicaltrials_cards);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.clinicaltrials_cards_toolbar);
        mToolbar.setTitle(getString(R.string.clinicaltrials_cards_search)+": \""+searchString+"\"");

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.clinicaltrials_cards_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.clinicaltrials_cards_swipe_refresh_layout);
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
        mRecyclerView = (RecyclerView) findViewById(R.id.clinicaltrials_cards_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new ClinicalTrialsCardsAdapter(mContext, new JSONArray()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // No clinical trials
        mNoClinicalTrialsLayout = (LinearLayout) findViewById(R.id.clinicaltrials_cards_no_clinicaltrials);

        Button noClinicalTrialsButton = (Button) findViewById(R.id.clinicaltrials_cards_no_results_button);

        noClinicalTrialsButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    Intent intent = new Intent(mContext, MainWebViewActivity.class);
                    intent.putExtra("title", getString(R.string.clinicaltrials_cards_search)+": \""+searchString+"\"");
                    intent.putExtra("uri", "https://clinicaltrials.gov/ct2/results?term="+URLEncoder.encode(searchString.toLowerCase(), "utf-8")+"&no_unk=Y");
                    mContext.startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
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
                            new MaterialDialog.Builder(mContext).title(getString(R.string.correct_dialog_title)).content(Html.fromHtml(getString(R.string.correct_dialog_message)+":<br><br><b>"+correctSearchString+"</b>")).positiveText(getString(R.string.correct_dialog_positive_button)).negativeText(getString(R.string.correct_dialog_negative_button)).onPositive(new MaterialDialog.SingleButtonCallback()
                            {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                                {
                                    ContentValues contentValues = new ContentValues();
                                    contentValues.put(ClinicalTrialsSQLiteHelper.COLUMN_STRING, correctSearchString);

                                    SQLiteDatabase sqLiteDatabase = new ClinicalTrialsSQLiteHelper(mContext).getWritableDatabase();

                                    sqLiteDatabase.delete(ClinicalTrialsSQLiteHelper.TABLE, ClinicalTrialsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(searchString)+" COLLATE NOCASE", null);
                                    sqLiteDatabase.insert(ClinicalTrialsSQLiteHelper.TABLE, null, contentValues);

                                    sqLiteDatabase.close();

                                    mToolbar.setTitle(getString(R.string.clinicaltrials_cards_search)+": \""+correctSearchString+"\"");

                                    mProgressBar.setVisibility(View.VISIBLE);

                                    mNoClinicalTrialsLayout.setVisibility(View.GONE);
                                    mSwipeRefreshLayout.setVisibility(View.VISIBLE);

                                    search(correctSearchString, true);
                                }
                            }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("ClinicalTrialsCards", error.toString());
                }
            });

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsonObjectRequest);
        }
        catch(Exception e)
        {
            Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
        }
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

    // Search
    private void search(final String string, boolean cache)
    {
        try
        {
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            String apiUri = getString(R.string.project_website_uri)+"api/1/clinicaltrials/?search="+URLEncoder.encode(string.toLowerCase(), "utf-8");

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
                        mNoClinicalTrialsLayout.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        if(mTools.isTablet())
                        {
                            int spanCount = (response.length() == 1) ? 1 : 2;

                            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
                        }

                        mRecyclerView.setAdapter(new ClinicalTrialsCardsAdapter(mContext, response));

                        ContentValues contentValues = new ContentValues();
                        contentValues.put(ClinicalTrialsSQLiteHelper.COLUMN_STRING, string);

                        SQLiteDatabase sqLiteDatabase = new ClinicalTrialsSQLiteHelper(mContext).getWritableDatabase();

                        sqLiteDatabase.delete(ClinicalTrialsSQLiteHelper.TABLE, ClinicalTrialsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(string)+" COLLATE NOCASE", null);
                        sqLiteDatabase.insert(ClinicalTrialsSQLiteHelper.TABLE, null, contentValues);

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

                    mTools.showToast(getString(R.string.clinicaltrials_cards_something_went_wrong), 1);

                    finish();

                    Log.e("ClinicalTrialsCards", error.toString());
                }
            });

            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsonArrayRequest);
        }
        catch(Exception e)
        {
            Log.e("ClinicalTrialsCards", Log.getStackTraceString(e));
        }
    }
}