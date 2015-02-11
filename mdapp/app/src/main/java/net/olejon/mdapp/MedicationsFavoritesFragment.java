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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

public class MedicationsFavoritesFragment extends Fragment
{
    private Activity mActivity;
    private Context mContext;

    private MyTools mTools;

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private EditText mSearchEditText;

    private ListView mListView;
    private View mListViewEmpty;

    // Create fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_medications_favorites, container, false);

        // Activity
        mActivity = getActivity();

        // Context
        mContext = viewGroup.getContext();

        // Tools
        mTools = new MyTools(mActivity);

        // Search
        mSearchEditText = (EditText) mActivity.findViewById(R.id.main_search_edittext);

        // List
        mListView = (ListView) viewGroup.findViewById(R.id.main_medications_favorites_list);
        mListViewEmpty = viewGroup.findViewById(R.id.main_medications_favorites_list_empty);

        return viewGroup;
    }

    // Resume fragment
    @Override
    public void onResume()
    {
        super.onResume();

        if(mSearchEditText.getText().toString().equals("")) getFavorites();
    }

    // Destroy fragment
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(mCursor != null && !mCursor.isClosed()) mCursor.close();
        if(mSqLiteDatabase != null && mSqLiteDatabase.isOpen()) mSqLiteDatabase.close();
    }

    // Context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater menuInflater = mActivity.getMenuInflater();
        menuInflater.inflate(R.menu.menu_medications_favorites_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int id = item.getItemId();

        if(id == R.id.medications_favorites_menu_context_remove)
        {
            removeFromFavorites(adapterContextMenuInfo.id);

            return true;
        }

        return super.onContextItemSelected(item);
    }

    // Favorites
    private void getFavorites()
    {
        GetMedicationsFavoritesTask getMedicationsFavoritesTask = new GetMedicationsFavoritesTask();
        getMedicationsFavoritesTask.execute();
    }

    private void removeFromFavorites(long id)
    {
        mSqLiteDatabase.delete(MedicationsFavoritesSQLiteHelper.TABLE, MedicationsFavoritesSQLiteHelper.COLUMN_ID+" = "+id, null);

        mTools.updateWidget();

        getFavorites();
    }

    private class GetMedicationFromFavoriteUriTask extends AsyncTask<Long, Void, Long>
    {
        @Override
        protected void onPostExecute(Long id)
        {
            Intent intent = new Intent(mActivity, MedicationActivity.class);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                if(mTools.getDefaultSharedPreferencesBoolean("MEDICATION_MULTIPLE_DOCUMENTS")) intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }

            intent.putExtra("id", id);
            mActivity.startActivity(intent);
        }

        @Override
        protected Long doInBackground(Long... longs)
        {
            mCursor = mSqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, null, MedicationsFavoritesSQLiteHelper.COLUMN_ID+" = "+longs[0], null, null, null, null);

            long id = 0;

            if(mCursor.moveToFirst())
            {
                try
                {
                    id = mTools.getMedicationIdFromUri(mCursor.getString(mCursor.getColumnIndexOrThrow(MedicationsFavoritesSQLiteHelper.COLUMN_URI)));
                }
                catch(Exception e)
                {
                    Log.e("MedicationsFavorites", Log.getStackTraceString(e));
                }
            }

            return id;
        }
    }

    // Get medications
    private class GetMedicationsFavoritesTask extends AsyncTask<Void, Void, MedicationsFavoritesSimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(final MedicationsFavoritesSimpleCursorAdapter medicationsFavoritesSimpleCursorAdapter)
        {
            mListView.setAdapter(medicationsFavoritesSimpleCursorAdapter);

            mListView.setEmptyView(mListViewEmpty);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long id)
                {
                    GetMedicationFromFavoriteUriTask getMedicationFromFavoriteUriTask = new GetMedicationFromFavoriteUriTask();
                    getMedicationFromFavoriteUriTask.execute(id);
                }
            });

            mSearchEditText.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
                {
                    if(MainActivity.VIEW_PAGER_POSITION == 2) medicationsFavoritesSimpleCursorAdapter.getFilter().filter(charSequence);
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                @Override
                public void afterTextChanged(Editable editable) { }
            });

            medicationsFavoritesSimpleCursorAdapter.setFilterQueryProvider(new FilterQueryProvider()
            {
                @Override
                public Cursor runQuery(CharSequence charSequence)
                {
                    String[] queryColumns = {MedicationsFavoritesSQLiteHelper.COLUMN_ID, MedicationsFavoritesSQLiteHelper.COLUMN_NAME, MedicationsFavoritesSQLiteHelper.COLUMN_TYPE, MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER, MedicationsFavoritesSQLiteHelper.COLUMN_PRESCRIPTION_GROUP};

                    if(charSequence.length() == 0) return mSqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, queryColumns, null, null, null, null, null);

                    String query = charSequence.toString().trim();

                    return mSqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, queryColumns, MedicationsFavoritesSQLiteHelper.COLUMN_NAME+" LIKE '%"+query.replace("'", "")+"%'", null, null, null, null);
                }
            });

            registerForContextMenu(mListView);
        }

        @Override
        protected MedicationsFavoritesSimpleCursorAdapter doInBackground(Void... voids)
        {
            mSqLiteDatabase = new MedicationsFavoritesSQLiteHelper(mContext).getWritableDatabase();

            mCursor = mSqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, null, null, null, null, null, MedicationsFavoritesSQLiteHelper.COLUMN_NAME);

            String[] fromColumns = {MedicationsFavoritesSQLiteHelper.COLUMN_NAME, MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER, MedicationsFavoritesSQLiteHelper.COLUMN_TYPE, MedicationsFavoritesSQLiteHelper.COLUMN_PRESCRIPTION_GROUP};
            int[] toViews = {R.id.main_medications_favorites_list_item_name, R.id.main_medications_favorites_list_item_manufacturer, R.id.main_medications_favorites_list_item_type, R.id.main_medications_favorites_list_item_prescription_group};

            return new MedicationsFavoritesSimpleCursorAdapter(mActivity, mCursor, fromColumns, toViews);
        }
    }
}