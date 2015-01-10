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
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;

public class SubstancesFragment extends Fragment
{
    private Activity mActivity;

    private Cursor mCursor;

    private EditText mToolbarSearchEditText;

    private ListView mListView;
    private View mListViewEmpty;

    // Create fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_substances, container, false);

        // Activity
        mActivity = getActivity();

        // Toolbar
        mToolbarSearchEditText = (EditText) mActivity.findViewById(R.id.main_toolbar_search);

        // List
        mListView = (ListView) viewGroup.findViewById(R.id.main_substances_list);
        mListViewEmpty = viewGroup.findViewById(R.id.main_substances_list_empty);

        // Get substances
        GetSubstancesTask getSubstancesTask = new GetSubstancesTask();
        getSubstancesTask.execute();

        return viewGroup;
    }

    // Destroy fragment view
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();

        if(mCursor != null && !mCursor.isClosed()) mCursor.close();
    }

    // Get substances
    private class GetSubstancesTask extends AsyncTask<Void, Void, SimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(final SimpleCursorAdapter simpleCursorAdapter)
        {
            mListView.setAdapter(simpleCursorAdapter);

            mListView.setEmptyView(mListViewEmpty);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long id)
                {
                    Intent intent = new Intent(mActivity, SubstanceActivity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                }
            });

            mToolbarSearchEditText.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)
                {
                    if(MainActivity.VIEW_PAGER_POSITION == 1) simpleCursorAdapter.getFilter().filter(charSequence);
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

                @Override
                public void afterTextChanged(Editable editable) { }
            });

            simpleCursorAdapter.setFilterQueryProvider(new FilterQueryProvider()
            {
                @Override
                public Cursor runQuery(CharSequence charSequence)
                {
                    if(MainActivity.SQLITE_DATABASE_FELLESKATALOGEN != null && MainActivity.SQLITE_DATABASE_FELLESKATALOGEN.isOpen())
                    {
                        String[] queryColumns = {FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_ID, FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_NAME, FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_MEDICATIONS_COUNT};

                        if(charSequence.length() == 0) return MainActivity.SQLITE_DATABASE_FELLESKATALOGEN.query(FelleskatalogenSQLiteHelper.TABLE_SUBSTANCES, queryColumns, null, null, null, null, null);

                        String query = charSequence.toString().trim();

                        return MainActivity.SQLITE_DATABASE_FELLESKATALOGEN.query(FelleskatalogenSQLiteHelper.TABLE_SUBSTANCES, queryColumns, FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_NAME+" LIKE '%"+query.replace("'", "")+"%'", null, null, null, null);
                    }

                    return null;
                }
            });
        }

        @Override
        protected SimpleCursorAdapter doInBackground(Void... voids)
        {
            String[] queryColumns = {FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_ID, FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_NAME, FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_MEDICATIONS_COUNT};
            mCursor = MainActivity.SQLITE_DATABASE_FELLESKATALOGEN.query(FelleskatalogenSQLiteHelper.TABLE_SUBSTANCES, queryColumns, null, null, null, null, null);

            String[] fromColumns = {FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_NAME, FelleskatalogenSQLiteHelper.SUBSTANCES_COLUMN_MEDICATIONS_COUNT};
            int[] toViews = {R.id.main_substances_list_item_name, R.id.main_substances_list_item_medications_count};

            return new SimpleCursorAdapter(mActivity, R.layout.fragment_substances_list_item, mCursor, fromColumns, toViews, 0);
        }
    }
}