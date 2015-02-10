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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtcCodesActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private String mAtcCode = "";

    private int mAtcCodePosition = 0;

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

        String matchedAtcCode = "";

        if(intentAction != null && intentAction.equals(Intent.ACTION_VIEW))
        {
            String uri = intent.getData().toString();

            Pattern pattern = Pattern.compile("/([^/]+)$");
            Matcher matcher = pattern.matcher(uri);

            while(matcher.find())
            {
                mAtcCode = matcher.group(1);

                matchedAtcCode = matcher.group(1);
            }
        }
        else
        {
            mAtcCode = intent.getStringExtra("code");

            Pattern pattern = Pattern.compile("([A-Za-z][0-9]{2}[A-Za-z]\\s+[A-Za-z])");
            Matcher matcher = pattern.matcher(mAtcCode);

            while(matcher.find())
            {
                matchedAtcCode = matcher.group(1);
            }
        }

        // Layout
        setContentView(R.layout.activity_atc_codes);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.atc_codes_toolbar);
        toolbar.setTitle(mAtcCode);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // List
        ListView listView = (ListView) findViewById(R.id.atc_codes_list);

        // Get substances
        mSqLiteDatabase = new FelleskatalogenSQLiteHelper(mContext).getReadableDatabase();

        mCursor = mSqLiteDatabase.query(FelleskatalogenSQLiteHelper.TABLE_ATC_CODES, null, FelleskatalogenSQLiteHelper.ATC_CODES_COLUMN_CODE+" LIKE '"+matchedAtcCode+"%'", null, null, null, null);

        int atcCodesCount = mCursor.getCount();

        if(atcCodesCount == 0)
        {
            mTools.showToast(getString(R.string.atc_codes_could_not_find_atc_code), 1);

            finish();
        }
        else
        {
            String[] fromColumns = new String[] {"code", "name"};
            int[] toViews = new int[] {R.id.atc_codes_list_item_code, R.id.atc_codes_list_item_name};

            final ArrayList<HashMap<String, String>> substancesArrayList = new ArrayList<>();
            final ArrayList<String> substancesNamesArrayList = new ArrayList<>();
            final ArrayList<String> substancesUrisArrayList = new ArrayList<>();

            for(int i = 0; i < atcCodesCount; i++)
            {
                if(mCursor.moveToPosition(i))
                {
                    try
                    {
                        String lastSubstancesAtcCode = "";

                        final String substancesAtcCode = mCursor.getString(mCursor.getColumnIndexOrThrow("code"));

                        final JSONArray substancesJsonArray = new JSONArray(mCursor.getString(mCursor.getColumnIndexOrThrow("substances")));

                        for(int n = 0; n < substancesJsonArray.length(); n++)
                        {
                            HashMap<String, String> substance = new HashMap<>();

                            JSONObject substanceJsonObject = substancesJsonArray.getJSONObject(n);

                            if(substancesAtcCode.equals(lastSubstancesAtcCode))
                            {
                                substance.put("code", "");
                            }
                            else
                            {
                                substance.put("code", substancesAtcCode);

                                if(substancesAtcCode.equals(mAtcCode)) mAtcCodePosition = substancesArrayList.size();
                            }

                            substance.put("name", substanceJsonObject.getString("name"));

                            lastSubstancesAtcCode = substancesAtcCode;

                            substancesArrayList.add(substance);

                            substancesNamesArrayList.add(substanceJsonObject.getString("name"));
                            substancesUrisArrayList.add(substanceJsonObject.getString("uri"));
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e("AtcCodesActivity", Log.getStackTraceString(e));
                    }
                }
            }

            AtcCodesSimpleAdapter atcCodesSimpleAdapter = new AtcCodesSimpleAdapter(mContext, toolbar, mAtcCode, substancesArrayList, fromColumns, toViews);

            listView.setAdapter(atcCodesSimpleAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    final String name = substancesNamesArrayList.get(i);
                    final String uri = substancesUrisArrayList.get(i);

                    if(name.equals("") || name.equals("Diverse") || name.equals("Kombinasjoner") || name.startsWith("Andre ") || name.contains("kombinasjon"))
                    {
                        mTools.showToast(getString(R.string.atc_codes_substance_not_a_substance), 1);
                    }
                    else if(name.matches("^\\w+ og \\w+$"))
                    {
                        Pattern pattern = Pattern.compile("^(\\w+) og (\\w+)$");
                        Matcher matcher = pattern.matcher(name);

                        final String[] substancesStringArray = {"", ""};

                        while(matcher.find())
                        {
                            substancesStringArray[0] = matcher.group(1).substring(0, 1).toUpperCase() + matcher.group(1).substring(1);
                            substancesStringArray[1] = matcher.group(2).substring(0, 1).toUpperCase() + matcher.group(2).substring(1);
                        }

                        new MaterialDialog.Builder(mContext).title(getString(R.string.atc_codes_dialog_title)).items(substancesStringArray).itemsCallback(new MaterialDialog.ListCallback()
                        {
                            @Override
                            public void onSelection(MaterialDialog materialDialog, View view, int n, CharSequence charSequence)
                            {
                                getSubstance(substancesStringArray[n]);
                            }
                        }).itemColorRes(R.color.dark_blue).show();
                    }
                    else if(name.matches("^\\w+, .*og \\w+$"))
                    {
                        final String[] substancesStringArray = name.split(" ");
                        final String[] substancesListStringArray = new String[substancesStringArray.length - 1];

                        int n = 0;

                        for(String substance : substancesStringArray)
                        {
                            if(!substance.equals("og"))
                            {
                                substancesListStringArray[n] = substance.substring(0, 1).toUpperCase() + substance.substring(1).replace(",", "");

                                n++;
                            }
                        }

                        new MaterialDialog.Builder(mContext).title(getString(R.string.atc_codes_dialog_title)).items(substancesListStringArray).itemsCallback(new MaterialDialog.ListCallback()
                        {
                            @Override
                            public void onSelection(MaterialDialog materialDialog, View view, int n, CharSequence charSequence)
                            {
                                getSubstance(substancesListStringArray[n]);
                            }
                        }).itemColorRes(R.color.dark_blue).show();
                    }
                    else if(uri.equals(""))
                    {
                        getSubstance(name);
                    }
                    else
                    {
                        mTools.getMedicationWithFullContent(uri);
                    }
                }
            });

            listView.setSelection(mAtcCodePosition);
        }
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
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void getSubstance(String name)
    {
        SQLiteDatabase sqLiteDatabase = new FelleskatalogenSQLiteHelper(mContext).getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(FelleskatalogenSQLiteHelper.TABLE_SUBSTANCES, null, FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_NAME+" = "+mTools.sqe(name), null, null, null, null);

        if(cursor.getCount() == 1)
        {
            if(cursor.moveToFirst())
            {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_ID));

                Intent intent = new Intent(mContext, SubstanceActivity.class);
                intent.putExtra("id", Long.parseLong(id));
                startActivity(intent);
            }
        }
        else
        {
            mTools.showToast(getString(R.string.atc_codes_substance_not_in_felleskatalogen), 1);
        }

        cursor.close();
        sqLiteDatabase.close();
    }
}