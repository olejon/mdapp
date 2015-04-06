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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Icd10Activity extends ActionBarActivity
{
    private final Context mContext = this;

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private ListView mListView;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Layout
        setContentView(R.layout.activity_icd10);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.icd10_toolbar);
        toolbar.setTitle(getString(R.string.icd10_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // List
        mListView = (ListView) findViewById(R.id.icd10_list);

        // Get chapters
        GetChaptersTask getChaptersTask = new GetChaptersTask();
        getChaptersTask.execute();
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

    // Get chapters
    private class GetChaptersTask extends AsyncTask<Void, Void, SimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(final SimpleCursorAdapter simpleCursorAdapter)
        {
            mListView.setAdapter(simpleCursorAdapter);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long id)
                {
                    if(mCursor.moveToPosition(i))
                    {
                        Intent intent = new Intent(mContext, Icd10ChapterActivity.class);
                        intent.putExtra("chapter", id);
                        startActivity(intent);
                    }
                }
            });
        }

        @Override
        protected SimpleCursorAdapter doInBackground(Void... voids)
        {
            mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();

            String[] queryColumns = {SlDataSQLiteHelper.ICD_10_COLUMN_ID, SlDataSQLiteHelper.ICD_10_COLUMN_CHAPTER, SlDataSQLiteHelper.ICD_10_COLUMN_CODES, SlDataSQLiteHelper.ICD_10_COLUMN_NAME};
            mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_ICD_10, queryColumns, null, null, null, null, null);

            String[] fromColumns = {SlDataSQLiteHelper.ICD_10_COLUMN_CHAPTER, SlDataSQLiteHelper.ICD_10_COLUMN_CODES, SlDataSQLiteHelper.ICD_10_COLUMN_NAME};
            int[] toViews = {R.id.icd10_list_item_chapter, R.id.icd10_list_item_codes, R.id.icd10_list_item_name};

            return new SimpleCursorAdapter(mContext, R.layout.activity_icd10_list_item, mCursor, fromColumns, toViews, 0);
        }
    }
}