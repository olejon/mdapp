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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

public class MedicationsFavoritesFragment extends Fragment
{
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

        // Context
        mContext = viewGroup.getContext();

        // Tools
        mTools = new MyTools(mContext);

        // Search
        mSearchEditText = (EditText) getActivity().findViewById(R.id.main_search_edittext);

        // List
        mListView = (ListView) viewGroup.findViewById(R.id.main_medications_favorites_list);
        mListViewEmpty = viewGroup.findViewById(R.id.main_medications_favorites_list_empty);

        return viewGroup;
    }

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

    // Favorites
    private void getFavorites()
    {
        GetMedicationsFavoritesTask getMedicationsFavoritesTask = new GetMedicationsFavoritesTask();
        getMedicationsFavoritesTask.execute();
    }

    // Get medications
    private class GetMedicationsFavoritesTask extends AsyncTask<Void, Void, MedicationsSimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(final MedicationsSimpleCursorAdapter medicationsSimpleCursorAdapter)
        {
            mListView.setAdapter(medicationsSimpleCursorAdapter);
            mListView.setEmptyView(mListViewEmpty);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    TextView medicationNameTextView = (TextView) view.findViewById(R.id.main_medications_list_item_name);

                    String medicationName = medicationNameTextView.getText().toString();

                    SQLiteDatabase sqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
                    String[] queryColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID};
                    Cursor cursor = sqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, queryColumns, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME+" = "+mTools.sqe(medicationName), null, null, null, null);

                    if(cursor.moveToFirst())
                    {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID));

                        Intent intent = new Intent(mContext, MedicationActivity.class);

                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mTools.getDefaultSharedPreferencesBoolean("MEDICATION_MULTIPLE_DOCUMENTS")) intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

                        intent.putExtra("id", id);
                        startActivity(intent);
                    }

                    cursor.close();
                    sqLiteDatabase.close();
                }
            });

            mSearchEditText.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
                {
                    if(MainActivity.VIEW_PAGER_POSITION == 2) medicationsSimpleCursorAdapter.getFilter().filter(charSequence);
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                @Override
                public void afterTextChanged(Editable editable) { }
            });

            medicationsSimpleCursorAdapter.setFilterQueryProvider(new FilterQueryProvider()
            {
                @Override
                public Cursor runQuery(CharSequence charSequence)
                {
                    String[] queryColumns = {MedicationsFavoritesSQLiteHelper.COLUMN_ID, MedicationsFavoritesSQLiteHelper.COLUMN_PRESCRIPTION_GROUP, MedicationsFavoritesSQLiteHelper.COLUMN_NAME, MedicationsFavoritesSQLiteHelper.COLUMN_SUBSTANCE, MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER};

                    if(charSequence.length() == 0) return mSqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, queryColumns, null, null, null, null, MedicationsFavoritesSQLiteHelper.COLUMN_NAME+" COLLATE NOCASE");

                    String query = charSequence.toString().trim();

                    return mSqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, queryColumns, MedicationsFavoritesSQLiteHelper.COLUMN_NAME+" LIKE "+mTools.sqe("%"+query+"%")+" OR "+MedicationsFavoritesSQLiteHelper.COLUMN_SUBSTANCE+" LIKE "+mTools.sqe("%"+query+"%")+" OR "+MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER+" LIKE "+mTools.sqe("%"+query+"%"), null, null, null, MedicationsFavoritesSQLiteHelper.COLUMN_NAME+" COLLATE NOCASE");
                }
            });
        }

        @Override
        protected MedicationsSimpleCursorAdapter doInBackground(Void... voids)
        {
            mSqLiteDatabase = new MedicationsFavoritesSQLiteHelper(mContext).getReadableDatabase();
            String[] queryColumns = {MedicationsFavoritesSQLiteHelper.COLUMN_ID, MedicationsFavoritesSQLiteHelper.COLUMN_PRESCRIPTION_GROUP, MedicationsFavoritesSQLiteHelper.COLUMN_NAME, MedicationsFavoritesSQLiteHelper.COLUMN_SUBSTANCE, MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER};
            mCursor = mSqLiteDatabase.query(MedicationsFavoritesSQLiteHelper.TABLE, queryColumns, null, null, null, null, MedicationsFavoritesSQLiteHelper.COLUMN_NAME+" COLLATE NOCASE");

            String[] fromColumns = {MedicationsFavoritesSQLiteHelper.COLUMN_PRESCRIPTION_GROUP, MedicationsFavoritesSQLiteHelper.COLUMN_NAME, MedicationsFavoritesSQLiteHelper.COLUMN_SUBSTANCE, MedicationsFavoritesSQLiteHelper.COLUMN_MANUFACTURER};
            int[] toViews = {R.id.main_medications_list_item_prescription_group, R.id.main_medications_list_item_name, R.id.main_medications_list_item_substance, R.id.main_medications_list_item_manufacturer};

            return new MedicationsSimpleCursorAdapter(mContext, mCursor, fromColumns, toViews);
        }
    }
}