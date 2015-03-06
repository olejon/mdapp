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
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class SubstanceActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private Toolbar mToolbar;
    private ListView mListView;

    private String substanceName;
    private String substanceUri;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Settings
        PreferenceManager.setDefaultValues(mContext, R.xml.settings, false);

        // Intent
        Intent intent = getIntent();
        String intentAction = intent.getAction();

        long substanceId;

        if(intentAction != null && intentAction.equals(Intent.ACTION_VIEW))
        {
            String uri = intent.getData().toString().replace("no/medisin/substansregister/", "no/m/medisin/substansregister/").replace("?json", "");

            substanceId = mTools.getSubstanceIdFromUri(uri);
        }
        else
        {
            substanceId = intent.getLongExtra("id", 0);
        }

        if(substanceId == 0)
        {
            mTools.showToast(getString(R.string.substance_could_not_find_substance), 1);

            finish();
        }
        else
        {
            // Layout
            setContentView(R.layout.activity_substance);

            // Toolbar
            mToolbar = (Toolbar) findViewById(R.id.substance_toolbar);

            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // List
            mListView = (ListView) findViewById(R.id.substance_list);

            View listViewHeader = getLayoutInflater().inflate(R.layout.activity_substance_list_header, mListView, false);
            mListView.addHeaderView(listViewHeader, null, false);

            // Get substance
            //GetSubstanceTask getSubstanceTask = new GetSubstanceTask();
            //getSubstanceTask.execute(substanceId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_substance, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                Intent intent = NavUtils.getParentActivityIntent(this);

                if(NavUtils.shouldUpRecreateTask(this, intent))
                {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(intent).startActivities();
                }
                else
                {
                    NavUtils.navigateUpTo(this, intent);
                }

                return true;
            }
            case R.id.substance_menu_interactions:
            {
                Intent intent = new Intent(mContext, InteractionsActivity.class);
                intent.putExtra("search", substanceName);
                startActivity(intent);
                return true;
            }
            case R.id.substance_menu_uri:
            {
                mTools.openUri(substanceUri);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Get substance
    /*private class GetSubstanceTask extends AsyncTask<Long, Void, HashMap<String, String>>
    {
        @Override
        protected void onPostExecute(HashMap<String, String> substance)
        {
            // Get substance details
            final String substanceMedications = substance.get(FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_MEDICATIONS);
            final String substanceMedicationsCount = substance.get(FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_MEDICATIONS_COUNT);
            final String substanceAtcCodes = substance.get(FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_ATC_CODES);

            substanceName = substance.get(FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_NAME);
            substanceUri = substance.get(FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_URI);

            mToolbar.setTitle(substanceName);

            TextView textView = (TextView) findViewById(R.id.substance_medications_count);
            textView.setText(substanceMedicationsCount+" - "+getString(R.string.substance_source));

            if(!substanceAtcCodes.equals(""))
            {
                try
                {
                    final JSONArray atcCodesJsonArray = new JSONArray(substanceAtcCodes);

                    LayoutInflater layoutInflater = getLayoutInflater();
                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.substance_atc_codes);

                    for(int i = 0; i < atcCodesJsonArray.length(); i++)
                    {
                        final String atcCode = atcCodesJsonArray.getString(i);

                        LinearLayout atcCodeLinearLayout = (LinearLayout) layoutInflater.inflate(R.layout.activity_substance_atc_code, null);
                        linearLayout.addView(atcCodeLinearLayout);

                        TextView atcCodeTextView = (TextView) atcCodeLinearLayout.findViewById(R.id.substance_atc_codes_code);
                        atcCodeTextView.setText(atcCode);

                        atcCodeTextView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                Intent intent = new Intent(mContext, AtcCodesActivity.class);
                                intent.putExtra("code", atcCode);
                                startActivity(intent);
                            }
                        });
                    }
                }
                catch(Exception e)
                {
                    Log.e("SubstanceActivity", Log.getStackTraceString(e));
                }
            }

            if(!substanceMedications.equals(""))
            {
                try
                {
                    final JSONArray medicationsJsonArray = new JSONArray(substanceMedications);

                    String[] fromColumns = new String[] {"name", "manufacturer"};
                    int[] toViews = new int[] {R.id.substance_list_item_name, R.id.substance_list_item_manufacturer};

                    ArrayList<HashMap<String, String>> medications = new ArrayList<>();

                    for(int i = 0; i < medicationsJsonArray.length(); i++)
                    {
                        HashMap<String, String> medication = new HashMap<>();

                        JSONObject jsonObject = medicationsJsonArray.getJSONObject(i);

                        medication.put("name", jsonObject.getString("name"));
                        medication.put("manufacturer", jsonObject.getString("manufacturer"));

                        medications.add(medication);
                    }

                    SimpleAdapter simpleAdapter = new SimpleAdapter(mContext, medications, R.layout.activity_substance_list_item, fromColumns, toViews);

                    mListView.setAdapter(simpleAdapter);

                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
                    {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                        {
                            try
                            {
                                int index = i - 1;

                                String uri = medicationsJsonArray.getJSONObject(index).getString("uri");

                                mTools.getMedicationWithFullContent(uri);
                            }
                            catch(Exception e)
                            {
                                Log.e("SubstanceActivity", Log.getStackTraceString(e));
                            }
                        }
                    });
                }
                catch(Exception e)
                {
                    Log.e("SubstanceActivity", Log.getStackTraceString(e));
                }
            }

            View listViewEmpty = findViewById(R.id.substance_list_empty);
            mListView.setEmptyView(listViewEmpty);
        }

        @Override
        protected HashMap<String, String> doInBackground(Long... longs)
        {
            SQLiteDatabase sqLiteDatabase = new FelleskatalogenSQLiteHelper(mContext).getReadableDatabase();

            Cursor cursor = sqLiteDatabase.query(FelleskatalogenSQLiteHelper.TABLE_SUBSTANCES, null, FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_ID+" = "+longs[0], null, null, null, null);

            String[] columns = cursor.getColumnNames();

            HashMap<String, String> substance = new HashMap<>();

            if(cursor.moveToFirst())
            {
                for(String column : columns)
                {
                    try
                    {
                        substance.put(column, cursor.getString(cursor.getColumnIndexOrThrow(column)));
                    }
                    catch(Exception e)
                    {
                        Log.e("SubstanceActivity", Log.getStackTraceString(e));
                    }
                }
            }

            cursor.close();
            sqLiteDatabase.close();

            return substance;
        }
    }*/
}
