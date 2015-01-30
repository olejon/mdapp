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
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Icd10Activity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private ProgressBar mProgressBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Connected?
        if(!mTools.isDeviceConnected()) mTools.showToast(getString(R.string.device_not_connected), 1);

        // Layout
        setContentView(R.layout.activity_icd10);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.icd10_toolbar);
        toolbar.setTitle(getString(R.string.icd10_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.icd10_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // Refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.icd10_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_blue, R.color.accent_green, R.color.accent_purple, R.color.accent_orange);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                getData(false);
            }
        });

        // List
        mListView = (ListView) findViewById(R.id.icd10_list);

        // Get data
        getData(true);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_icd10, menu);
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
            case R.id.icd10_uri:
            {
                mTools.openUri("http://helsedirektoratet.no/kvalitet-planlegging/helsefaglige-kodeverk/icd-10/Sider/default.aspx");
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Get data
    private void getData(boolean cache)
    {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        String apiUri = getString(R.string.project_website)+"api/1/icd-10/";

        if(!cache) requestQueue.getCache().remove(apiUri);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(apiUri, new Response.Listener<JSONArray>()
        {
            @Override
            public void onResponse(JSONArray response)
            {
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                try
                {
                    final ArrayList<HashMap<String, String>> itemsArrayList = new ArrayList<>();

                    final ArrayList<String> titlesArrayList = new ArrayList<>();

                    String[] fromColumns = new String[] {"chapter", "codes", "name"};
                    int[] toViews = new int[] {R.id.icd10_list_item_chapter, R.id.icd10_list_item_codes, R.id.icd10_list_item_name};

                    for(int i = 0; i < response.length(); i++)
                    {
                        HashMap<String, String> item = new HashMap<>();

                        JSONObject itemJsonObject = response.getJSONObject(i);

                        String chapter = itemJsonObject.getString("chapter");
                        String codes = getString(R.string.icd10_codes)+": "+itemJsonObject.getString("codes");
                        String name = itemJsonObject.getString("name");

                        item.put("chapter", chapter);
                        item.put("codes", codes);
                        item.put("name", name);

                        itemsArrayList.add(item);

                        titlesArrayList.add(name);
                    }

                    SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, itemsArrayList, R.layout.activity_icd10_list_item, fromColumns, toViews);

                    mListView.setAdapter(simpleAdapter);

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                        {
                            int chapter = i + 1;

                            Intent intent = new Intent(mContext, Icd10ChapterActivity.class);
                            intent.putExtra("title", titlesArrayList.get(i));
                            intent.putExtra("chapter", chapter);
                            startActivity(intent);
                        }
                    });
                }
                catch(Exception e)
                {
                    Log.e("Icd10Activity", Log.getStackTraceString(e));
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                mTools.showToast(getString(R.string.icd10_could_not_get_data), 1);

                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);

                Log.e("Icd10Activity", error.toString());

                finish();
            }
        });

        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonArrayRequest);
    }
}