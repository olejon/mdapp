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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class SubstancesFragment extends Fragment
{
    private Context mContext;

    private final MyTools mTools = new MyTools(mContext);

    private Cursor mCursor;

    private InputMethodManager mInputMethodManager;

    private EditText mSearchEditText;
    private ListView mListView;
    private View mListViewEmpty;

    // Create fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_substances, container, false);

        // Context
        mContext = viewGroup.getContext();

        // Input manager
        mInputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);

        // Search
        mSearchEditText = (EditText) getActivity().findViewById(R.id.main_search_edittext);

        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                if(i == EditorInfo.IME_ACTION_DONE || keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                {
                    mInputMethodManager.toggleSoftInputFromWindow(mSearchEditText.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    return true;
                }

                return false;
            }
        });

        // List
        mListView = (ListView) viewGroup.findViewById(R.id.main_substances_list);
        mListViewEmpty = viewGroup.findViewById(R.id.main_substances_list_empty);

        // Get substances
        GetSubstancesTask getSubstancesTask = new GetSubstancesTask();
        getSubstancesTask.execute();

        return viewGroup;
    }

    // Destroy fragment
    @Override
    public void onDestroy()
    {
        super.onDestroy();

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
                    Intent intent = new Intent(mContext, SubstanceActivity.class);
                    intent.putExtra("id", id);
                    startActivity(intent);
                }
            });

            mSearchEditText.addTextChangedListener(new TextWatcher()
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
                    if(charSequence.length() == 0) return MainActivity.SQLITE_DATABASE.query(SlDataSQLiteHelper.TABLE_SUBSTANCES, null, null, null, null, null, SlDataSQLiteHelper.SUBSTANCES_COLUMN_NAME);

                    String query = charSequence.toString().trim();

                    return MainActivity.SQLITE_DATABASE.query(SlDataSQLiteHelper.TABLE_SUBSTANCES, null, SlDataSQLiteHelper.SUBSTANCES_COLUMN_NAME+" LIKE "+mTools.sqe("%"+query+"%"), null, null, null, SlDataSQLiteHelper.SUBSTANCES_COLUMN_NAME);
                }
            });
        }

        @Override
        protected SimpleCursorAdapter doInBackground(Void... voids)
        {
            mCursor = MainActivity.SQLITE_DATABASE.query(SlDataSQLiteHelper.TABLE_SUBSTANCES, null, null, null, null, null, SlDataSQLiteHelper.SUBSTANCES_COLUMN_NAME);

            String[] fromColumns = {SlDataSQLiteHelper.SUBSTANCES_COLUMN_NAME, SlDataSQLiteHelper.SUBSTANCES_COLUMN_ATC_CODE};
            int[] toViews = {R.id.main_substances_list_item_name, R.id.main_substances_list_item_atc_code};

            return new SimpleCursorAdapter(mContext, R.layout.fragment_substances_list_item, mCursor, fromColumns, toViews, 0);
        }
    }
}