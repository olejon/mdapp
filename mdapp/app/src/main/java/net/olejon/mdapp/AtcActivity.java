package net.olejon.mdapp;

/*

Copyright 2016 Ole Jon Bjørkum

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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class AtcActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private Toolbar mToolbar;
    private ListView mListView;

    private String mGroup;
    private String mAnatomicalGroupsCode;
    private String mPharmacologicGroupsTitle;
    private String mPharmacologicGroupsCode;
    private String mTherapeuticGroupsTitle;
    private String mTherapeuticGroupsCode;
    private String mSubstancesGroupsTitle;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_atc);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.atc_toolbar);
        mToolbar.setTitle(getString(R.string.atc_title));

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
        switch(mGroup)
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

    // Get ATC
    private void getAnatomicalGroups()
    {
        mToolbar.setTitle(getString(R.string.atc_title));

        mGroup = "anatomical_groups";

        mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
        mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ATC_ANATOMICAL_GROUPS, null, null, null, null, null, SlDataSQLiteHelper.ATC_ANATOMICAL_GROUPS_COLUMN_CODE);

        String[] fromColumns = new String[] {SlDataSQLiteHelper.ATC_ANATOMICAL_GROUPS_COLUMN_CODE, SlDataSQLiteHelper.ATC_ANATOMICAL_GROUPS_COLUMN_NAME};
        int[] toViews = new int[] {R.id.atc_anatomical_groups_list_item_code, R.id.atc_anatomical_groups_list_item_name};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(mContext, R.layout.activity_atc_anatomical_groups_list_item, mCursor, fromColumns, toViews, 0);

        mListView.setAdapter(simpleCursorAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                if(mCursor.moveToPosition(i))
                {
                    mAnatomicalGroupsCode = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ATC_ANATOMICAL_GROUPS_COLUMN_CODE));

                    mPharmacologicGroupsTitle = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ATC_ANATOMICAL_GROUPS_COLUMN_NAME));

                    getPharmacologicGroups();
                }
            }
        });
    }

    private void getPharmacologicGroups()
    {
        mToolbar.setTitle(mPharmacologicGroupsTitle);

        mGroup = "pharmacologic_groups";

        mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ATC_PHARMACOLOGIC_GROUPS, null, SlDataSQLiteHelper.ATC_PHARMACOLOGIC_GROUPS_COLUMN_CODE+" LIKE "+mTools.sqe(mAnatomicalGroupsCode+"%"), null, null, null, SlDataSQLiteHelper.ATC_PHARMACOLOGIC_GROUPS_COLUMN_CODE);

        String[] fromColumns = new String[] {SlDataSQLiteHelper.ATC_PHARMACOLOGIC_GROUPS_COLUMN_CODE, SlDataSQLiteHelper.ATC_PHARMACOLOGIC_GROUPS_COLUMN_NAME};
        int[] toViews = new int[] {R.id.atc_pharmacologic_groups_list_item_code, R.id.atc_pharmacologic_groups_list_item_name};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(mContext, R.layout.activity_atc_pharmacologic_groups_list_item, mCursor, fromColumns, toViews, 0);

        mListView.setAdapter(simpleCursorAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                if(mCursor.moveToPosition(i))
                {
                    mPharmacologicGroupsCode = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ATC_PHARMACOLOGIC_GROUPS_COLUMN_CODE));

                    mTherapeuticGroupsTitle = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ATC_PHARMACOLOGIC_GROUPS_COLUMN_NAME));

                    getTherapeuticGroups();
                }
            }
        });
    }

    private void getTherapeuticGroups()
    {
        mToolbar.setTitle(mTherapeuticGroupsTitle);

        mGroup = "therapeutic_groups";

        mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ATC_THERAPEUTIC_GROUPS, null, SlDataSQLiteHelper.ATC_THERAPEUTIC_GROUPS_COLUMN_CODE+" LIKE "+mTools.sqe(mPharmacologicGroupsCode+"%"), null, null, null, SlDataSQLiteHelper.ATC_THERAPEUTIC_GROUPS_COLUMN_CODE);

        String[] fromColumns = new String[] {SlDataSQLiteHelper.ATC_THERAPEUTIC_GROUPS_COLUMN_CODE, SlDataSQLiteHelper.ATC_THERAPEUTIC_GROUPS_COLUMN_NAME};
        int[] toViews = new int[] {R.id.atc_therapeutic_groups_list_item_code, R.id.atc_therapeutic_groups_list_item_name};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(mContext, R.layout.activity_atc_therapeutic_groups_list_item, mCursor, fromColumns, toViews, 0);

        mListView.setAdapter(simpleCursorAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                if(mCursor.moveToPosition(i))
                {
                    mTherapeuticGroupsCode = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ATC_THERAPEUTIC_GROUPS_COLUMN_CODE));

                    mSubstancesGroupsTitle = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ATC_THERAPEUTIC_GROUPS_COLUMN_NAME));

                    getSubstancesGroups();
                }
            }
        });
    }

    private void getSubstancesGroups()
    {
        mToolbar.setTitle(mSubstancesGroupsTitle);

        mGroup = "substances_groups";

        mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ATC_SUBSTANCES_GROUPS, null, SlDataSQLiteHelper.ATC_SUBSTANCES_GROUPS_COLUMN_CODE+" LIKE "+mTools.sqe(mTherapeuticGroupsCode+"%"), null, null, null, SlDataSQLiteHelper.ATC_SUBSTANCES_GROUPS_COLUMN_CODE);

        String[] fromColumns = new String[] {SlDataSQLiteHelper.ATC_SUBSTANCES_GROUPS_COLUMN_CODE, SlDataSQLiteHelper.ATC_SUBSTANCES_GROUPS_COLUMN_NAME};
        int[] toViews = new int[] {R.id.atc_substances_groups_list_item_code, R.id.atc_substances_groups_list_item_name};

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(mContext, R.layout.activity_atc_substances_groups_list_item, mCursor, fromColumns, toViews, 0);

        mListView.setAdapter(simpleCursorAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                if(mCursor.moveToPosition(i))
                {
                    String code = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.ATC_SUBSTANCES_GROUPS_COLUMN_CODE));

                    Intent intent = new Intent(mContext, AtcCodesActivity.class);
                    intent.putExtra("code", code);
                    startActivity(intent);
                }
            }
        });
    }
}