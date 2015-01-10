package net.olejon.mdapp;

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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.net.URLEncoder;

public class PoisoningsCardsActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

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
        Intent intent = getIntent();

        searchString = intent.getStringExtra("search").replaceAll("(?i)\\s*depot", "");

        // Layout
        setContentView(R.layout.activity_poisonings_cards);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.poisonings_cards_toolbar);
        toolbar.setTitle(getString(R.string.poisonings_cards_search)+": \""+searchString+"\"");

        setSupportActionBar(toolbar);
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

        Button noPoisoningsButton = (Button) findViewById(R.id.poisonings_cards_no_poisonings_button);

        noPoisoningsButton.setOnClickListener(new View.OnClickListener()
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

        // Search
        search(searchString, true);
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
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+4722591300"));
                startActivity(intent);
                return true;
            }
            case R.id.poisonings_cards_menu_uri:
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

            String apiUri = getString(R.string.project_website)+"api/1/poisonings/?search="+URLEncoder.encode(string.toLowerCase(), "utf-8");

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

            requestQueue.add(jsonArrayRequest);
        }
        catch(Exception e)
        {
            Log.e("PoisoningsCardsActivity", Log.getStackTraceString(e));
        }
    }
}
