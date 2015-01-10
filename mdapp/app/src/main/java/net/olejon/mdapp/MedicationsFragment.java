package net.olejon.mdapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
    private Activity mActivity;

    private Cursor mCursor;

    private EditText mToolbarSearchEditText;

    private ListView mListView;
    private View mListViewEmpty;

    // Create fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_medications, container, false);

        // Activity
        mActivity = getActivity();

        // Toolbar
        mToolbarSearchEditText = (EditText) mActivity.findViewById(R.id.main_toolbar_search);

        // List
        mListView = (ListView) viewGroup.findViewById(R.id.main_medications_list);
        mListViewEmpty = viewGroup.findViewById(R.id.main_medications_list_empty);

        // Get medications
        GetMedicationsTask getMedicationsTask = new GetMedicationsTask();
        getMedicationsTask.execute();

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
    private class GetMedicationsTask extends AsyncTask<Void, Void, FelleskatalogenSimpleCursorAdapter>
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
                    intent.putExtra("id", id);
                    startActivity(intent);
                }
            });

            mToolbarSearchEditText.addTextChangedListener(new TextWatcher()
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
    }
}