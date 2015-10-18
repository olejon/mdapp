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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class Icd10SearchActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private ProgressBar mProgressBar;
    private ListView mListView;
    private View mListViewEmpty;

    private String mSearchString;

    private ArrayList<String> mCodesArrayList;
    private ArrayList<String> mNamesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Intent
        final Intent intent = getIntent();

        mSearchString = intent.getStringExtra("search");

        // Layout
        setContentView(R.layout.activity_icd10_search);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.icd10_search_toolbar);
        toolbar.setTitle(getString(R.string.icd10_search_search)+": \""+mSearchString+"\"");

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Progress bar
        mProgressBar = (ProgressBar) findViewById(R.id.icd10_search_toolbar_progressbar);
        mProgressBar.setVisibility(View.VISIBLE);

        // List
        mListView = (ListView) findViewById(R.id.icd10_search_list);
        mListViewEmpty = findViewById(R.id.icd10_search_list_empty);

        // Get search
        GetSearchTask getSearchTask = new GetSearchTask();
        getSearchTask.execute();
    }

    // Destroy activity
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(mCursor != null && !mCursor.isClosed()) mCursor.close();
        if(mSqLiteDatabase != null && mSqLiteDatabase.isOpen()) mSqLiteDatabase.close();
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

    // Get search
    private class GetSearchTask extends AsyncTask<Void, Void, SimpleAdapter>
    {
        @Override
        protected void onPostExecute(SimpleAdapter simpleAdapter)
        {
            mProgressBar.setVisibility(View.GONE);

            mListView.setAdapter(simpleAdapter);
            mListView.setEmptyView(mListViewEmpty);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
                {
                    mProgressBar.setVisibility(View.VISIBLE);

                    try
                    {
                        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

                        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website_uri)+"api/1/icd-10/search/?uri="+URLEncoder.encode("http://www.icd10data.com/Search.aspx?search="+mCodesArrayList.get(i), "utf-8"), new Response.Listener<JSONObject>()
                        {
                            @Override
                            public void onResponse(JSONObject response)
                            {
                                try
                                {
                                    mProgressBar.setVisibility(View.GONE);

                                    String uri = response.getString("uri");

                                    Intent intent = new Intent(mContext, MainWebViewActivity.class);
                                    intent.putExtra("title", mNamesArrayList.get(i));
                                    intent.putExtra("uri", uri);
                                    startActivity(intent);
                                }
                                catch(Exception e)
                                {
                                    mProgressBar.setVisibility(View.GONE);

                                    mTools.showToast(getString(R.string.icd10_search_could_not_find_code), 1);

                                    Log.e("Icd10SearchActivity", Log.getStackTraceString(e));
                                }
                            }
                        }, new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                mProgressBar.setVisibility(View.GONE);

                                mTools.showToast(getString(R.string.icd10_search_could_not_find_code), 1);

                                Log.e("Icd10SearchActivity", error.toString());
                            }
                        });

                        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                        requestQueue.add(jsonObjectRequest);
                    }
                    catch(Exception e)
                    {
                        Log.e("Icd10SearchActivity", Log.getStackTraceString(e));
                    }
                }
            });
        }

        @Override
        protected SimpleAdapter doInBackground(Void... voids)
        {
            mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();

            String[] queryColumns = {SlDataSQLiteHelper.ICD_10_COLUMN_DATA};
            mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ICD_10, queryColumns, null, null, null, null, null);

            final ArrayList<HashMap<String, String>> itemsArrayList = new ArrayList<>();

            mCodesArrayList = new ArrayList<>();
            mNamesArrayList = new ArrayList<>();

            String[] fromColumns = new String[] {"code", "name"};
            int[] toViews = new int[] {R.id.icd10_search_list_item_code, R.id.icd10_search_list_item_name};

            if(mCursor.moveToFirst())
            {
                for(int i = 0; i < mCursor.getCount(); i++)
                {
                    if(mCursor.moveToPosition(i))
                    {
                        try
                        {
                            final JSONArray data = new JSONArray(mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ICD_10_COLUMN_DATA)));

                            for(int n = 0; n < data.length(); n++)
                            {
                                HashMap<String, String> item = new HashMap<>();

                                JSONObject itemJsonObject = data.getJSONObject(n);

                                String code = itemJsonObject.getString("code");
                                String name = itemJsonObject.getString("name");

                                if(code.matches("(?i).*?"+mSearchString+".*") || name.matches("(?i).*?"+mSearchString+".*"))
                                {
                                    item.put("code", code);
                                    item.put("name", name);

                                    itemsArrayList.add(item);

                                    mCodesArrayList.add(code);
                                    mNamesArrayList.add(name);
                                }
                            }
                        }
                        catch(JSONException e)
                        {
                            Log.e("Icd10SearchActivity", Log.getStackTraceString(e));
                        }
                    }
                }
            }

            return new SimpleAdapter(mContext, itemsArrayList, R.layout.activity_icd10_search_list_item, fromColumns, toViews);
        }
    }
}