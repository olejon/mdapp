package net.olejon.mdapp;

/*

Copyright 2017 Ole Jon Bjørkum

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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.net.URLEncoder;

public class ManufacturerActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private String manufacturerName;

    // Create activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Intent
        final Intent intent = getIntent();

        final long manufacturerId = intent.getLongExtra("id", 0);

        // Layout
        setContentView(R.layout.activity_manufacturer);

        // Get manufacturer
        mSqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();

        String[] manufacturersQueryColumns = {SlDataSQLiteHelper.MANUFACTURERS_COLUMN_NAME};
        mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MANUFACTURERS, manufacturersQueryColumns, SlDataSQLiteHelper.MANUFACTURERS_COLUMN_ID+" = "+manufacturerId, null, null, null, null);

        if(mCursor.moveToFirst())
        {
            manufacturerName = mCursor.getString(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MANUFACTURERS_COLUMN_NAME));

            String[] medicationsQueryColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME, SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE};
            mCursor = mSqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, medicationsQueryColumns, SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER+" = "+mTools.sqe(manufacturerName), null, null, null, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME+" COLLATE NOCASE");

            String[] fromColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME, SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE};
            int[] toViews = {R.id.manufacturer_list_item_name, R.id.manufacturer_list_item_substance};

            SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(mContext, R.layout.activity_manufacturer_list_item, mCursor, fromColumns, toViews, 0);

            // Toolbar
            final Toolbar toolbar = (Toolbar) findViewById(R.id.manufacturer_toolbar);
            toolbar.setTitle(manufacturerName);

            setSupportActionBar(toolbar);
            if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Medications count
            int medicationsCount = mCursor.getCount();

            String medications = (medicationsCount == 1) ? getString(R.string.manufacturer_medication) : getString(R.string.manufacturer_medications);

            TextView medicationsCountTextView = (TextView) findViewById(R.id.manufacturer_medications_count);
            medicationsCountTextView.setText(medicationsCount+" "+medications);

            // List
            ListView listView = (ListView) findViewById(R.id.manufacturer_list);
            listView.setAdapter(simpleCursorAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    if(mCursor.moveToPosition(i))
                    {
                        long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID));

                        Intent intent = new Intent(mContext, MedicationActivity.class);

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mTools.getDefaultSharedPreferencesBoolean("MEDICATION_MULTIPLE_DOCUMENTS")) intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

                        intent.putExtra("id", id);
                        startActivity(intent);
                    }
                }
            });
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_manufacturer, menu);
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
            case R.id.manufacturer_menu_contact:
            {
                try
                {
                    Intent intent = new Intent(mContext, MainWebViewActivity.class);
                    intent.putExtra("title", manufacturerName);
                    intent.putExtra("uri", "http://www.gulesider.no/finn:"+URLEncoder.encode(manufacturerName.replaceAll(" .*", ""), "utf-8"));
                    startActivity(intent);
                }
                catch(Exception e)
                {
                    Log.e("ManufacturerActivity", Log.getStackTraceString(e));
                }
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}