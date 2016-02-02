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

public class MedicationsFragment extends Fragment
{
    private Context mContext;

    private MyTools mTools;

    private Cursor mCursor;

    private EditText mSearchEditText;
    private ListView mListView;
    private View mListViewEmpty;

    // Create fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_medications, container, false);

        // Context
        mContext = viewGroup.getContext();

        // Tools
        mTools = new MyTools(mContext);

        // Search
        mSearchEditText = (EditText) getActivity().findViewById(R.id.main_search_edittext);

        // List
        mListView = (ListView) viewGroup.findViewById(R.id.main_medications_list);
        mListViewEmpty = viewGroup.findViewById(R.id.main_medications_list_empty);

        // Get medications
        GetMedicationsTask getMedicationsTask = new GetMedicationsTask();
        getMedicationsTask.execute();

        return viewGroup;
    }

    // Destroy fragment
    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(mCursor != null && !mCursor.isClosed()) mCursor.close();
    }

    // Get medications
    private class GetMedicationsTask extends AsyncTask<Void, Void, MedicationsSimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(final MedicationsSimpleCursorAdapter medicationsSimpleCursorAdapter)
        {
            mListView.setAdapter(medicationsSimpleCursorAdapter);
            mListView.setEmptyView(mListViewEmpty);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long id)
                {
                    Intent intent = new Intent(mContext, MedicationActivity.class);

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        if(mTools.getDefaultSharedPreferencesBoolean("MEDICATION_MULTIPLE_DOCUMENTS")) intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                    }

                    intent.putExtra("id", id);
                    startActivity(intent);
                }
            });

            mSearchEditText.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void onTextChanged(final CharSequence charSequence, int i, int i2, int i3)
                {
                    if(MainActivity.VIEW_PAGER_POSITION == 0) medicationsSimpleCursorAdapter.getFilter().filter(charSequence);
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
                    String[] queryColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID, SlDataSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME, SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE, SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER};

                    if(charSequence.length() == 0) return MainActivity.SQLITE_DATABASE.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, queryColumns, null, null, null, null, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME+" COLLATE NOCASE");

                    String query = charSequence.toString().trim();

                    return MainActivity.SQLITE_DATABASE.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, queryColumns, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME+" LIKE "+mTools.sqe("%"+query+"%"), null, null, null, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME+" COLLATE NOCASE");
                }
            });
        }

        @Override
        protected MedicationsSimpleCursorAdapter doInBackground(Void... voids)
        {
            String[] queryColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID, SlDataSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME, SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE, SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER};
            mCursor = MainActivity.SQLITE_DATABASE.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, queryColumns, null, null, null, null, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME+" COLLATE NOCASE");

            String[] fromColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME, SlDataSQLiteHelper.MEDICATIONS_COLUMN_SUBSTANCE, SlDataSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER};
            int[] toViews = {R.id.main_medications_list_item_prescription_group, R.id.main_medications_list_item_name, R.id.main_medications_list_item_substance, R.id.main_medications_list_item_manufacturer};

            return new MedicationsSimpleCursorAdapter(mContext, mCursor, fromColumns, toViews);
        }
    }
}