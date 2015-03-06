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
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

public class MedicationsFragment extends Fragment
{
    private Activity mActivity;

    private MyTools mTools;

    private Cursor mCursor;

    private EditText mSearchEditText;

    private ListView mListView;
    private View mListViewEmpty;

    // Create fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_medications, container, false);

        mActivity = getActivity();

        mTools = new MyTools(mActivity);

        // Search
        //mSearchEditText = (EditText) mActivity.findViewById(R.id.main_search_edittext);

        // List
        mListView = (ListView) viewGroup.findViewById(R.id.main_medications_list);
        mListViewEmpty = viewGroup.findViewById(R.id.main_medications_list_empty);

        // Get medications
        //GetMedicationsTask getMedicationsTask = new GetMedicationsTask();
        //getMedicationsTask.execute();

        return viewGroup;
    }

    // Destroy fragment view
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        if(mCursor != null && !mCursor.isClosed()) mCursor.close();
    }

    // Get medications
    /*private class GetMedicationsTask extends AsyncTask<Void, Void, FelleskatalogenSimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(final FelleskatalogenSimpleCursorAdapter felleskatalogenSimpleCursorAdapter)
        {
            mListView.setAdapter(felleskatalogenSimpleCursorAdapter);

            mListView.setEmptyView(mListViewEmpty);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long id)
                {
                    Intent intent = new Intent(mActivity, MedicationActivity.class);

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
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
                {
                    if(MainActivity.VIEW_PAGER_POSITION == 0) felleskatalogenSimpleCursorAdapter.getFilter().filter(charSequence);
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                @Override
                public void afterTextChanged(Editable editable) { }
            });

            felleskatalogenSimpleCursorAdapter.setFilterQueryProvider(new FilterQueryProvider()
            {
                @Override
                public Cursor runQuery(CharSequence charSequence)
                {
                    if(MainActivity.SQLITE_DATABASE_FELLESKATALOGEN != null && MainActivity.SQLITE_DATABASE_FELLESKATALOGEN.isOpen())
                    {
                        String[] queryColumns = {FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_ID, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_NAME, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_TYPE, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP};

                        if(charSequence.length() == 0) return MainActivity.SQLITE_DATABASE_FELLESKATALOGEN.query(FelleskatalogenSQLiteHelper.TABLE_MEDICATIONS, queryColumns, null, null, null, null, null);

                        String query = charSequence.toString().trim();

                        return MainActivity.SQLITE_DATABASE_FELLESKATALOGEN.query(FelleskatalogenSQLiteHelper.TABLE_MEDICATIONS, queryColumns, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_NAME+" LIKE '%"+query.replace("'", "")+"%'", null, null, null, null);
                    }

                    return null;
                }
            });
        }

        @Override
        protected FelleskatalogenSimpleCursorAdapter doInBackground(Void... voids)
        {
            String[] queryColumns = {FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_ID, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_NAME, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_TYPE, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP};
            mCursor = MainActivity.SQLITE_DATABASE_FELLESKATALOGEN.query(FelleskatalogenSQLiteHelper.TABLE_MEDICATIONS, queryColumns, null, null, null, null, null);

            String[] fromColumns = {FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_NAME, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_TYPE, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_MANUFACTURER, FelleskatalogenSQLiteHelper.MEDICATIONS_COLUMN_PRESCRIPTION_GROUP};
            int[] toViews = {R.id.main_medications_list_item_name, R.id.main_medications_list_item_type, R.id.main_medications_list_item_manufacturer, R.id.main_medications_list_item_prescription_group};

            return new FelleskatalogenSimpleCursorAdapter(mActivity, mCursor, fromColumns, toViews);
        }
    }*/
}