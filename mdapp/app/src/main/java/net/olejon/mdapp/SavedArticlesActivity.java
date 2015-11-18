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
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class SavedArticlesActivity extends AppCompatActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private ListView mListView;
    private View mListViewEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_saved_articles);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.saved_articles_toolbar);
        toolbar.setTitle(R.string.saved_articles_title);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // List
        mListView = (ListView) findViewById(R.id.saved_articles_list);
        mListViewEmpty = findViewById(R.id.saved_articles_list_empty);

        // Get saved articles
        getSavedArticles();
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
        getMenuInflater().inflate(R.menu.menu_saved_articles, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.saved_articles_menu_information:
            {
                showInformationDialog(true);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Context menu
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        getMenuInflater().inflate(R.menu.menu_saved_articles_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        int id = item.getItemId();

        if(id == R.id.saved_articles_menu_context_remove)
        {
            removeSavedArticle(adapterContextMenuInfo.id);

            return true;
        }

        return super.onContextItemSelected(item);
    }

    // Information dialog
    private void showInformationDialog(boolean show)
    {
        if(show || !mTools.getSharedPreferencesBoolean("SAVED_ARTICLES_HIDE_INFORMATION_DIALOG"))
        {
            new MaterialDialog.Builder(mContext).title(getString(R.string.saved_articles_tip_dialog_title)).content(getString(R.string.saved_articles_tip_dialog_message)).positiveText(getString(R.string.saved_articles_tip_dialog_positive_button)).onPositive(new MaterialDialog.SingleButtonCallback()
            {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                {
                    mTools.setSharedPreferencesBoolean("SAVED_ARTICLES_HIDE_INFORMATION_DIALOG", true);
                }
            }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).show();
        }
    }

    // Saved articles
    private void getSavedArticles()
    {
        GetSavedArticlesTask getSavedArticlesTask = new GetSavedArticlesTask();
        getSavedArticlesTask.execute();
    }

    private void removeSavedArticle(long id)
    {
        mSqLiteDatabase.delete(SavedArticlesSQLiteHelper.TABLE, SavedArticlesSQLiteHelper.COLUMN_ID+" = "+id, null);

        getSavedArticles();
    }

    // Get saved articles
    private class GetSavedArticlesTask extends AsyncTask<Void, Void, SimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(SimpleCursorAdapter simpleCursorAdapter)
        {
            mListView.setAdapter(simpleCursorAdapter);
            mListView.setEmptyView(mListViewEmpty);

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    String title = mCursor.getString(mCursor.getColumnIndexOrThrow(SavedArticlesSQLiteHelper.COLUMN_TITLE));
                    String uri = mCursor.getString(mCursor.getColumnIndexOrThrow(SavedArticlesSQLiteHelper.COLUMN_URI));
                    String webview = mCursor.getString(mCursor.getColumnIndexOrThrow(SavedArticlesSQLiteHelper.COLUMN_WEBVIEW));

                    Intent intent = (webview.equals("main")) ? new Intent(mContext, MainWebViewActivity.class) : new Intent(mContext, DiseasesAndTreatmentsSearchWebViewActivity.class);
                    intent.putExtra("title", title);
                    intent.putExtra("uri", uri);
                    startActivity(intent);
                }
            });

            showInformationDialog(false);

            registerForContextMenu(mListView);
        }

        @Override
        protected SimpleCursorAdapter doInBackground(Void... voids)
        {
            mSqLiteDatabase = new SavedArticlesSQLiteHelper(mContext).getWritableDatabase();

            mCursor = mSqLiteDatabase.query(SavedArticlesSQLiteHelper.TABLE, null, null, null, null, null, SavedArticlesSQLiteHelper.COLUMN_ID+" DESC");

            String[] fromColumns = {SavedArticlesSQLiteHelper.COLUMN_TITLE, SavedArticlesSQLiteHelper.COLUMN_DOMAIN};
            int[] toViews = {R.id.saved_articles_list_item_title, R.id.saved_articles_list_item_domain};

            return new SimpleCursorAdapter(mContext, R.layout.activity_saved_articles_list_item, mCursor, fromColumns, toViews, 0);
        }
    }
}