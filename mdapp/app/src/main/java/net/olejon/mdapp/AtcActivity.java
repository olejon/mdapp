package net.olejon.mdapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AtcActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private Toolbar mToolbar;

    private ListView mListView;

    private String mGroups = "anatomical_groups";

    private String mPharmacologicGroupsTitle;
    private String mPharmacologicGroupsData;
    private String mTherapeuticGroupsTitle;
    private String mTherapeuticGroupsData;
    private String mSubstancesGroupsTitle;
    private String mSubstancesGroupsData;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_atc);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.atc_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // List
        mListView = (ListView) findViewById(R.id.atc_list);

        // Get ATC
        getAnatomicalGroups();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(mCursor != null && !mCursor.isClosed()) mCursor.close();
        if(mSqLiteDatabase != null && mSqLiteDatabase.isOpen()) mSqLiteDatabase.close();
    }

    // Back button
    @Override
    public void onBackPressed()
    {
        switch(mGroups)
        {
            case "pharmacologic_groups":
            {
                getAnatomicalGroups();
                break;
            }
            case "therapeutic_groups":
            {
                getPharmacologicGroups();
                break;
            }
            case "substances_groups":
            {
                getTherapeuticGroups();
                break;
            }
            default:
            {
                super.onBackPressed();
            }
        }
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_atc, menu);
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
            case R.id.atc_menu_uri:
            {
                mTools.openUri("http://www.felleskatalogen.no/m/medisin/atc-register");
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Get ATC
    private void getAnatomicalGroups()
    {
        GetAtcTask getAtcTask = new GetAtcTask();
        getAtcTask.execute();

        mGroups = "anatomical_groups";
    }

    private void getPharmacologicGroups()
    {
        mToolbar.setTitle(mPharmacologicGroupsTitle);

        try
        {
            final JSONArray dataJsonArray = new JSONArray(mPharmacologicGroupsData);

            String[] fromColumns = new String[] {"code", "name"};
            int[] toViews = new int[] {R.id.atc_pharmacologic_groups_list_item_code, R.id.atc_pharmacologic_groups_list_item_name};

            final ArrayList<HashMap<String, String>> itemsArrayList = new ArrayList<>();

            for(int i = 0; i < dataJsonArray.length(); i++)
            {
                HashMap<String, String> item = new HashMap<>();

                JSONObject itemJsonObject = dataJsonArray.getJSONObject(i);

                String code = itemJsonObject.getString("code");
                String name = itemJsonObject.getString("name");

                item.put("code", code);
                item.put("name", name);

                itemsArrayList.add(item);
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, itemsArrayList, R.layout.activity_atc_pharmacologic_groups_list_item, fromColumns, toViews);

            mListView.setAdapter(simpleAdapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    try
                    {
                        JSONObject itemJsonObject = dataJsonArray.getJSONObject(i);

                        mTherapeuticGroupsTitle = itemJsonObject.getString("name");
                        mTherapeuticGroupsData = itemJsonObject.getString("therapeutic_groups");

                        getTherapeuticGroups();
                    }
                    catch(Exception e)
                    {
                        Log.e("AtcActivity", Log.getStackTraceString(e));
                    }
                }
            });

            mGroups = "pharmacologic_groups";
        }
        catch(Exception e)
        {
            Log.e("AtcActivity", Log.getStackTraceString(e));
        }
    }

    private void getTherapeuticGroups()
    {
        mToolbar.setTitle(mTherapeuticGroupsTitle);

        try
        {
            final JSONArray dataJsonArray = new JSONArray(mTherapeuticGroupsData);

            String[] fromColumns = new String[] {"code", "name"};
            int[] toViews = new int[] {R.id.atc_therapeutic_groups_list_item_code, R.id.atc_therapeutic_groups_list_item_name};

            final ArrayList<HashMap<String, String>> itemsArrayList = new ArrayList<>();

            for(int i = 0; i < dataJsonArray.length(); i++)
            {
                HashMap<String, String> item = new HashMap<>();

                JSONObject itemJsonObject = dataJsonArray.getJSONObject(i);

                String code = itemJsonObject.getString("code");
                String name = itemJsonObject.getString("name");

                item.put("code", code);
                item.put("name", name);

                itemsArrayList.add(item);
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, itemsArrayList, R.layout.activity_atc_therapeutic_groups_list_item, fromColumns, toViews);

            mListView.setAdapter(simpleAdapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    try
                    {
                        JSONObject itemJsonObject = dataJsonArray.getJSONObject(i);

                        mSubstancesGroupsTitle = itemJsonObject.getString("name");
                        mSubstancesGroupsData = itemJsonObject.getString("substances_groups");

                        getSubstancesGroups();
                    }
                    catch(Exception e)
                    {
                        Log.e("AtcActivity", Log.getStackTraceString(e));
                    }
                }
            });

            mGroups = "therapeutic_groups";
        }
        catch(Exception e)
        {
            Log.e("AtcActivity", Log.getStackTraceString(e));
        }
    }

    private void getSubstancesGroups()
    {
        mToolbar.setTitle(mSubstancesGroupsTitle);

        try
        {
            final JSONArray dataJsonArray = new JSONArray(mSubstancesGroupsData);

            String[] fromColumns = new String[] {"code", "name"};
            int[] toViews = new int[] {R.id.atc_substances_groups_list_item_code, R.id.atc_substances_groups_list_item_name};

            final ArrayList<HashMap<String, String>> itemsArrayList = new ArrayList<>();

            for(int i = 0; i < dataJsonArray.length(); i++)
            {
                HashMap<String, String> item = new HashMap<>();

                JSONObject itemJsonObject = dataJsonArray.getJSONObject(i);

                String code = itemJsonObject.getString("code");
                String name = itemJsonObject.getString("name");

                item.put("code", code);
                item.put("name", name);

                itemsArrayList.add(item);
            }

            SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, itemsArrayList, R.layout.activity_atc_substances_groups_list_item, fromColumns, toViews);

            mListView.setAdapter(simpleAdapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    try
                    {
                        JSONObject itemJsonObject = dataJsonArray.getJSONObject(i);

                        Intent intent = new Intent(mContext, AtcCodesActivity.class);
                        intent.putExtra("code", itemJsonObject.getString("code"));
                        startActivity(intent);
                    }
                    catch(Exception e)
                    {
                        Log.e("AtcActivity", Log.getStackTraceString(e));
                    }
                }
            });

            mGroups = "substances_groups";
        }
        catch(Exception e)
        {
            Log.e("AtcActivity", Log.getStackTraceString(e));
        }
    }

    private class GetAtcTask extends AsyncTask<Void, Void, SimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(final SimpleCursorAdapter simpleCursorAdapter)
        {
            mToolbar.setTitle(getString(R.string.atc_title));

            mListView.setAdapter(simpleCursorAdapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    mPharmacologicGroupsTitle = mCursor.getString(mCursor.getColumnIndexOrThrow("anatomical_group_name"));
                    mPharmacologicGroupsData = mCursor.getString(mCursor.getColumnIndexOrThrow("anatomical_group_details"));

                    if(mCursor.moveToPosition(i)) getPharmacologicGroups();
                }
            });

            mGroups = "anatomical_groups";
        }

        @Override
        protected SimpleCursorAdapter doInBackground(Void... voids)
        {
            mSqLiteDatabase = new FelleskatalogenSQLiteHelper(mContext).getReadableDatabase();

            mCursor = mSqLiteDatabase.query(FelleskatalogenSQLiteHelper.TABLE_ATC, null, null, null, null, null, null);

            String[] fromColumns = {FelleskatalogenSQLiteHelper.ATC_COLUMN_ANATOMICAL_GROUP_LETTER, FelleskatalogenSQLiteHelper.ATC_COLUMN_ANATOMICAL_GROUP_NAME};
            int[] toViews = {R.id.atc_anatomical_groups_list_item_letter, R.id.atc_anatomical_groups_list_item_name};

            return new SimpleCursorAdapter(mContext, R.layout.activity_atc_anatomical_groups_list_item, mCursor, fromColumns, toViews, 0);
        }
    }
}