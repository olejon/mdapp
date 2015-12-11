package net.olejon.mdapp;

/*

Copyright 2015 Ole Jon Bjørkum

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
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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

public class DiseasesAndTreatmentsSearchActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private Spinner mSpinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private String mSearchLanguage;
    private String mSearchString;

    private int mFirstPubMedPosition;
    private int mFirstWebOfSciencePosition;
    private int mFirstMedlinePlusPosition;
    private int mFirstWikipediaPosition;
    private int mFirstUpToDatePosition;
    private int mFirstBmjPosition;
    private int mFirstNhiPosition;
    private int mFirstSmlPosition;
    private int mFirstForskningPosition;
    private int mFirstHelsebiblioteketPosition;
    private int mFirstTidsskriftetPosition;
    private int mFirstLegehandbokaPosition;
    private int mFirstOncolexPosition;
    private int mFirstBrukerhandbokenPosition;
    private int mFirstHelsenorgePosition;

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

        mSearchLanguage = intent.getStringExtra("language");

        mSearchString = intent.getStringExtra("string");

        // Layout
        setContentView(R.layout.activity_diseases_and_treatments_search);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.diseases_and_treatments_search_toolbar);
        mToolbar.setTitle(getString(R.string.diseases_and_treatments_search_search)+": \""+mSearchString+"\"");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.diseases_and_treatments_search_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Spinner
        mSpinner = (Spinner) findViewById(R.id.diseases_and_treatments_search_spinner);

        ArrayAdapter<CharSequence> arrayAdapter;

        if(mSearchLanguage.equals(""))
        {
            arrayAdapter = ArrayAdapter.createFromResource(mContext, R.array.diseases_and_treatments_search_spinner_items_english, R.layout.activity_diseases_and_treatments_search_spinner_header);
        }
        else
        {
            arrayAdapter = ArrayAdapter.createFromResource(mContext, R.array.diseases_and_treatments_search_spinner_items_norwegian, R.layout.activity_diseases_and_treatments_search_spinner_header);
        }

        arrayAdapter.setDropDownViewResource(R.layout.activity_diseases_and_treatments_search_spinner_item);

        mSpinner.setAdapter(arrayAdapter);
        mSpinner.setOnItemSelectedListener(this);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.diseases_and_treatments_search_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_green, R.color.accent_purple, R.color.accent_orange);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                search(false);
            }
        });

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.diseases_and_treatments_search_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new DiseasesAndTreatmentsSearchAdapter(new JSONArray()));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Search
        search(true);

        // Correct
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        try
        {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website_uri)+"api/1/correct/?search="+URLEncoder.encode(mSearchString, "utf-8"), new Response.Listener<JSONObject>()
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
                                    contentValues.put(DiseasesAndTreatmentsSQLiteHelper.COLUMN_STRING, correctSearchString);

                                    SQLiteDatabase sqLiteDatabase = new DiseasesAndTreatmentsSQLiteHelper(mContext).getWritableDatabase();

                                    sqLiteDatabase.delete(DiseasesAndTreatmentsSQLiteHelper.TABLE, DiseasesAndTreatmentsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(mSearchString)+" COLLATE NOCASE", null);
                                    sqLiteDatabase.insert(DiseasesAndTreatmentsSQLiteHelper.TABLE, null, contentValues);

                                    sqLiteDatabase.close();

                                    mToolbar.setTitle(getString(R.string.diseases_and_treatments_search_search)+": \""+correctSearchString+"\"");

                                    mProgressBar.setVisibility(View.VISIBLE);

                                    search(true);
                                }
                            }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).show();
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e("DiseasesAndTreatments", Log.getStackTraceString(e));
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("DiseasesAndTreatments", error.toString());
                }
            });

            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsonObjectRequest);
        }
        catch(Exception e)
        {
            Log.e("DiseasesAndTreatments", Log.getStackTraceString(e));
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

    // Spinner
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        if(mSearchLanguage.equals(""))
        {
            switch(i)
            {
                case 1:
                {
                    mRecyclerView.scrollToPosition(mFirstPubMedPosition);
                    break;
                }
                case 2:
                {
                    mRecyclerView.scrollToPosition(mFirstWebOfSciencePosition);
                    break;
                }
                case 3:
                {
                    mRecyclerView.scrollToPosition(mFirstMedlinePlusPosition);
                    break;
                }
                case 4:
                {
                    mRecyclerView.scrollToPosition(mFirstWikipediaPosition);
                    break;
                }
                case 5:
                {
                    if(mFirstUpToDatePosition == 0)
                    {
                        mTools.showToast(getString(R.string.diseases_and_treatments_search_no_results), 1);
                    }
                    else
                    {
                        mRecyclerView.scrollToPosition(mFirstUpToDatePosition);
                    }

                    break;
                }
                case 6:
                {
                    if(mFirstBmjPosition == 0)
                    {
                        mTools.showToast(getString(R.string.diseases_and_treatments_search_no_results), 1);
                    }
                    else
                    {
                        mRecyclerView.scrollToPosition(mFirstBmjPosition);
                    }

                    break;
                }
            }
        }
        else
        {
            switch(i)
            {
                case 1:
                {
                    mRecyclerView.scrollToPosition(mFirstNhiPosition);
                    break;
                }
                case 2:
                {
                    mRecyclerView.scrollToPosition(mFirstSmlPosition);
                    break;
                }
                case 3:
                {
                    mRecyclerView.scrollToPosition(mFirstWikipediaPosition);
                    break;
                }
                case 4:
                {
                    mRecyclerView.scrollToPosition(mFirstForskningPosition);
                    break;
                }
                case 5:
                {
                    if(mFirstHelsebiblioteketPosition == 0)
                    {
                        mTools.showToast(getString(R.string.diseases_and_treatments_search_no_results), 1);
                    }
                    else
                    {
                        mRecyclerView.scrollToPosition(mFirstHelsebiblioteketPosition);
                    }

                    break;
                }
                case 6:
                {
                    if(mFirstTidsskriftetPosition == 0)
                    {
                        mTools.showToast(getString(R.string.diseases_and_treatments_search_no_results), 1);
                    }
                    else
                    {
                        mRecyclerView.scrollToPosition(mFirstTidsskriftetPosition);
                    }

                    break;
                }
                case 7:
                {
                    if(mFirstLegehandbokaPosition == 0)
                    {
                        mTools.showToast(getString(R.string.diseases_and_treatments_search_no_results), 1);
                    }
                    else
                    {
                        mRecyclerView.scrollToPosition(mFirstLegehandbokaPosition);
                    }

                    break;
                }
                case 8:
                {
                    if(mFirstOncolexPosition == 0)
                    {
                        mTools.showToast(getString(R.string.diseases_and_treatments_search_no_results), 1);
                    }
                    else
                    {
                        mRecyclerView.scrollToPosition(mFirstOncolexPosition);
                    }

                    break;
                }
                case 9:
                {
                    if(mFirstBrukerhandbokenPosition == 0)
                    {
                        mTools.showToast(getString(R.string.diseases_and_treatments_search_no_results), 1);
                    }
                    else
                    {
                        mRecyclerView.scrollToPosition(mFirstBrukerhandbokenPosition);
                    }

                    break;
                }
                case 10:
                {
                    if(mFirstHelsenorgePosition == 0)
                    {
                        mTools.showToast(getString(R.string.diseases_and_treatments_search_no_results), 1);
                    }
                    else
                    {
                        mRecyclerView.scrollToPosition(mFirstHelsenorgePosition);
                    }

                    break;
                }
            }
        }

        Handler handler = new Handler();

        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mSpinner.setSelection(0);
            }
        }, 250);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) { }

    // Search
    private void search(final boolean cache)
    {
        mFirstPubMedPosition = 0;
        mFirstWebOfSciencePosition = 0;
        mFirstMedlinePlusPosition = 0;
        mFirstWikipediaPosition = 0;
        mFirstUpToDatePosition = 0;
        mFirstBmjPosition = 0;
        mFirstNhiPosition = 0;
        mFirstSmlPosition = 0;
        mFirstForskningPosition = 0;
        mFirstHelsebiblioteketPosition = 0;
        mFirstTidsskriftetPosition = 0;
        mFirstLegehandbokaPosition = 0;
        mFirstOncolexPosition = 0;
        mFirstBrukerhandbokenPosition = 0;
        mFirstHelsenorgePosition = 0;

        try
        {
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            String apiUri = getString(R.string.project_website_uri)+"api/1/diseases-and-treatments/"+mSearchLanguage+"/?search="+URLEncoder.encode(mSearchString, "utf-8");

            if(!cache) requestQueue.getCache().remove(apiUri);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(apiUri, new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
                    for(int i = 0; i < response.length(); i++)
                    {
                        try
                        {
                            JSONObject jsonObject = response.getJSONObject(i);

                            String type = jsonObject.getString("type");

                            if(mFirstPubMedPosition == 0 && type.equals("pubmed")) mFirstPubMedPosition = i;

                            if(mFirstWebOfSciencePosition == 0 && type.equals("webofscience")) mFirstWebOfSciencePosition = i;

                            if(mFirstMedlinePlusPosition == 0 && type.equals("medlineplus")) mFirstMedlinePlusPosition = i;

                            if(mFirstWikipediaPosition == 0 && type.equals("wikipedia")) mFirstWikipediaPosition = i;

                            if(mFirstUpToDatePosition == 0 && type.equals("uptodate")) mFirstUpToDatePosition = i;

                            if(mFirstBmjPosition == 0 && type.equals("bmj")) mFirstBmjPosition = i;

                            if(mFirstNhiPosition == 0 && type.equals("nhi")) mFirstNhiPosition = i;

                            if(mFirstSmlPosition == 0 && type.equals("sml")) mFirstSmlPosition = i;

                            if(mFirstForskningPosition == 0 && type.equals("forskning")) mFirstForskningPosition = i;

                            if(mFirstHelsebiblioteketPosition == 0 && type.equals("helsebiblioteket")) mFirstHelsebiblioteketPosition = i;

                            if(mFirstTidsskriftetPosition == 0 && type.equals("tidsskriftet")) mFirstTidsskriftetPosition = i;

                            if(mFirstLegehandbokaPosition == 0 && type.equals("legehandboka")) mFirstLegehandbokaPosition = i;

                            if(mFirstOncolexPosition == 0 && type.equals("oncolex")) mFirstOncolexPosition = i;

                            if(mFirstBrukerhandbokenPosition == 0 && type.equals("brukerhandboken")) mFirstBrukerhandbokenPosition = i;

                            if(mFirstHelsenorgePosition == 0 && type.equals("helsenorge")) mFirstHelsenorgePosition = i;
                        }
                        catch(Exception e)
                        {
                            Log.e("DiseasesAndTreatments", Log.getStackTraceString(e));
                        }
                    }

                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    if(mTools.isTablet()) mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

                    mRecyclerView.setAdapter(new DiseasesAndTreatmentsSearchAdapter(response));

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DiseasesAndTreatmentsSQLiteHelper.COLUMN_STRING, mSearchString);

                    SQLiteDatabase sqLiteDatabase = new DiseasesAndTreatmentsSQLiteHelper(mContext).getWritableDatabase();

                    sqLiteDatabase.delete(DiseasesAndTreatmentsSQLiteHelper.TABLE, DiseasesAndTreatmentsSQLiteHelper.COLUMN_STRING+" = "+mTools.sqe(mSearchString)+" COLLATE NOCASE", null);
                    sqLiteDatabase.insert(DiseasesAndTreatmentsSQLiteHelper.TABLE, null, contentValues);

                    sqLiteDatabase.close();
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    mProgressBar.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    mTools.showToast(getString(R.string.diseases_and_treatments_search_could_not_search), 1);

                    Log.e("DiseasesAndTreatments", error.toString());

                    finish();
                }
            });

            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsonArrayRequest);
        }
        catch(Exception e)
        {
            Log.e("DiseasesAndTreatments", Log.getStackTraceString(e));
        }
    }

    // Adapter
    private class DiseasesAndTreatmentsSearchAdapter extends RecyclerView.Adapter<DiseasesAndTreatmentsSearchAdapter.DiseasesAndTreatmentsSearchViewHolder>
    {
        private final JSONArray mResults;

        private int mLastPosition = -1;

        private DiseasesAndTreatmentsSearchAdapter(JSONArray results)
        {
            mResults = results;
        }

        class DiseasesAndTreatmentsSearchViewHolder extends RecyclerView.ViewHolder
        {
            final CardView card;
            final ImageView icon;
            final TextView title;
            final TextView text;

            public DiseasesAndTreatmentsSearchViewHolder(View view)
            {
                super(view);

                card = (CardView) view.findViewById(R.id.diseases_and_treatments_search_card);
                icon = (ImageView) view.findViewById(R.id.diseases_and_treatments_search_card_icon);
                title = (TextView) view.findViewById(R.id.diseases_and_treatments_search_card_title);
                text = (TextView) view.findViewById(R.id.diseases_and_treatments_search_card_text);
            }
        }

        @Override
        public DiseasesAndTreatmentsSearchViewHolder onCreateViewHolder(final ViewGroup viewGroup, final int i)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_diseases_and_treatments_search_card, viewGroup, false);
            return new DiseasesAndTreatmentsSearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DiseasesAndTreatmentsSearchViewHolder viewHolder, final int i)
        {
            try
            {
                final JSONObject result = mResults.getJSONObject(i);

                final String type = result.getString("type");
                final String title = result.getString("title");
                final String text = result.getString("text");
                final String uri = result.getString("uri");

                viewHolder.title.setText(title);

                switch(type)
                {
                    case "pubmed":
                    {
                        viewHolder.icon.setImageResource(R.drawable.pubmed);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "webofscience":
                    {
                        viewHolder.icon.setImageResource(R.drawable.webofscience);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "medlineplus":
                    {
                        viewHolder.icon.setImageResource(R.drawable.medlineplus);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "wikipedia":
                    {
                        viewHolder.icon.setImageResource(R.drawable.wikipedia);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "uptodate":
                    {
                        viewHolder.icon.setImageResource(R.drawable.uptodate);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "bmj":
                    {
                        viewHolder.icon.setImageResource(R.drawable.bmj);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "nhi":
                    {
                        viewHolder.icon.setImageResource(R.drawable.nhi);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "sml":
                    {
                        viewHolder.icon.setImageResource(R.drawable.sml);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "forskning":
                    {
                        viewHolder.icon.setImageResource(R.drawable.forskning);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "helsebiblioteket":
                    {
                        viewHolder.icon.setImageResource(R.drawable.helsebiblioteket);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "tidsskriftet":
                    {
                        viewHolder.icon.setImageResource(R.drawable.tidsskriftet);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "legehandboka":
                    {
                        viewHolder.icon.setImageResource(R.drawable.legehandboka);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "oncolex":
                    {
                        viewHolder.icon.setImageResource(R.drawable.oncolex);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "brukerhandboken":
                    {
                        viewHolder.icon.setImageResource(R.drawable.brukerhandboken);
                        viewHolder.text.setText(text);
                        break;
                    }
                    case "helsenorge":
                    {
                        viewHolder.icon.setImageResource(R.drawable.helsenorge);
                        viewHolder.text.setText(text);
                        break;
                    }
                }

                viewHolder.card.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(mContext, DiseasesAndTreatmentsSearchWebViewActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("uri", uri);
                        intent.putExtra("search", mSearchString);
                        mContext.startActivity(intent);
                    }
                });

                animateCard(viewHolder.card, i);
            }
            catch(Exception e)
            {
                Log.e("DiseasesAndTreatments", Log.getStackTraceString(e));
            }
        }

        @Override
        public int getItemCount()
        {
            return mResults.length();
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