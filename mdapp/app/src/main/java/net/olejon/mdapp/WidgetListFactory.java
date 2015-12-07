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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

class WidgetListFactory implements RemoteViewsService.RemoteViewsFactory
{
    private final Context mContext;

    private final MyTools mTools;

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    public WidgetListFactory(Context context)
    {
        mContext = context;

        mTools = new MyTools(mContext);
    }

    @Override
    public void onCreate()
    {
        mSqLiteDatabase = new MedicationsFavoritesSQLiteHelper(mContext).getReadableDatabase();

        String[] favoritesQueryColumns = {MedicationsFavoritesSQLiteHelper.COLUMN_ID, MedicationsFavoritesSQLiteHelper.COLUMN_NAME, MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER};
        mCursor = mSqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, favoritesQueryColumns, null, null, null, null, MedicationsFavoritesSQLiteHelper.COLUMN_NAME);
    }

    @Override
    public void onDestroy()
    {
        if(mCursor != null && !mCursor.isClosed()) mCursor.close();
        if(mSqLiteDatabase != null && mSqLiteDatabase.isOpen()) mSqLiteDatabase.close();
    }

    @Override
    public RemoteViews getViewAt(int i)
    {
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_list_item);

        try
        {
            if(mCursor.moveToPosition(i))
            {
                String medicationName = mCursor.getString(mCursor.getColumnIndexOrThrow(MedicationsFavoritesSQLiteHelper.COLUMN_NAME));
                String medicationManufacturer = mCursor.getString(mCursor.getColumnIndexOrThrow(MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER));

                remoteViews.setTextViewText(R.id.widget_list_item_name, medicationName);
                remoteViews.setTextViewText(R.id.widget_list_item_manufacturer, medicationManufacturer);

                SQLiteDatabase sqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();

                String[] queryColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID};
                Cursor cursor = sqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, queryColumns, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME+" = "+mTools.sqe(medicationName)+" AND "+SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER+" = "+mTools.sqe(medicationManufacturer), null, null, null, null);

                if(cursor.moveToFirst())
                {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID));

                    Intent intent = new Intent();
                    intent.putExtra("id", id);

                    remoteViews.setOnClickFillInIntent(R.id.widget_list_item, intent);
                }

                cursor.close();
                sqLiteDatabase.close();
            }
        }
        catch(Exception e)
        {
            Log.e("WidgetListFactory", Log.getStackTraceString(e));
        }

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView()
    {
        return new RemoteViews(mContext.getPackageName(), R.layout.widget_list_loading);
    }

    @Override
    public int getCount()
    {
        return mCursor.getCount();
    }

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }

    @Override
    public void onDataSetChanged() { }
}