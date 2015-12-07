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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class SavedArticlesActivity extends AppCompatActivity
{
    private final Activity mActivity = this;
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private TextView mEmptyTextView;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_saved_articles);

        mTools.setStatusbarColor(mActivity, R.color.statusbar_transparent);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.saved_articles_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.saved_articles_appbar);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener()
        {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset)
            {
                if(appBarLayout.getTotalScrollRange() == - verticalOffset)
                {
                    mTools.setStatusbarColor(mActivity, R.color.dark_blue);
                }
                else
                {
                    mTools.setStatusbarColor(mActivity, R.color.statusbar_transparent);
                }
            }
        });

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.saved_articles_toolbar_layout);
        collapsingToolbarLayout.setTitle(getString(R.string.saved_articles_title));

        // Empty
        mEmptyTextView = (TextView) findViewById(R.id.saved_articles_list_empty);

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.saved_articles_list);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new SavedArticlesAdapter(mCursor));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

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
    private class GetSavedArticlesTask extends AsyncTask<Void, Void, Cursor>
    {
        @Override
        protected void onPostExecute(Cursor cursor)
        {
            if(mCursor.getCount() == 0)
            {
                mRecyclerView.setVisibility(View.GONE);
                mEmptyTextView.setVisibility(View.VISIBLE);
            }
            else
            {
                mEmptyTextView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);

                mRecyclerView.setAdapter(new SavedArticlesAdapter(cursor));
            }
        }

        @Override
        protected Cursor doInBackground(Void... voids)
        {
            mSqLiteDatabase = new SavedArticlesSQLiteHelper(mContext).getWritableDatabase();

            mCursor = mSqLiteDatabase.query(SavedArticlesSQLiteHelper.TABLE, null, null, null, null, null, SavedArticlesSQLiteHelper.COLUMN_ID+" DESC");

            return mCursor;
        }
    }

    // Adapter
    private class SavedArticlesAdapter extends RecyclerView.Adapter<SavedArticlesAdapter.SavedArticlesAdapterViewHolder>
    {
        private final Cursor mCursor;

        private SavedArticlesAdapter(Cursor cursor)
        {
            mCursor = cursor;
        }

        class SavedArticlesAdapterViewHolder extends RecyclerView.ViewHolder
        {
            final LinearLayout listItem;
            final TextView titleTextView;
            final TextView domainTextView;

            public SavedArticlesAdapterViewHolder(View view)
            {
                super(view);

                listItem = (LinearLayout) view.findViewById(R.id.saved_articles_list_item_layout);
                titleTextView = (TextView) view.findViewById(R.id.saved_articles_list_item_title);
                domainTextView = (TextView) view.findViewById(R.id.saved_articles_list_item_domain);
            }
        }

        @Override
        public SavedArticlesAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_saved_articles_list_item, viewGroup, false);
            return new SavedArticlesAdapterViewHolder(view);
        }

        @Override
        public void onBindViewHolder(SavedArticlesAdapterViewHolder viewHolder, int i)
        {
            if(mCursor.moveToPosition(i))
            {
                final long id = mCursor.getLong(mCursor.getColumnIndexOrThrow(SavedArticlesSQLiteHelper.COLUMN_ID));
                final String title = mCursor.getString(mCursor.getColumnIndexOrThrow(SavedArticlesSQLiteHelper.COLUMN_TITLE));
                final String domain = mCursor.getString(mCursor.getColumnIndexOrThrow(SavedArticlesSQLiteHelper.COLUMN_DOMAIN));
                final String uri = mCursor.getString(mCursor.getColumnIndexOrThrow(SavedArticlesSQLiteHelper.COLUMN_URI));
                final String webview = mCursor.getString(mCursor.getColumnIndexOrThrow(SavedArticlesSQLiteHelper.COLUMN_WEBVIEW));

                viewHolder.titleTextView.setText(title);
                viewHolder.domainTextView.setText(domain);

                viewHolder.listItem.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = (webview.equals("main")) ? new Intent(mContext, MainWebViewActivity.class) : new Intent(mContext, DiseasesAndTreatmentsSearchWebViewActivity.class);
                        intent.putExtra("title", title);
                        intent.putExtra("uri", uri);
                        startActivity(intent);
                    }
                });

                viewHolder.listItem.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View view)
                    {
                        new MaterialDialog.Builder(mContext).title(R.string.saved_articles_remove_article_dialog_title).content(R.string.saved_articles_remove_article_dialog_message).positiveText(R.string.saved_articles_remove_article_dialog_positive_button).neutralText(R.string.saved_articles_remove_article_dialog_neutral_button).onPositive(new MaterialDialog.SingleButtonCallback()
                        {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                            {
                                removeSavedArticle(id);
                            }
                        }).contentColorRes(R.color.black).positiveColorRes(R.color.red).neutralColorRes(R.color.dark_blue).show();

                        return true;
                    }
                });
            }
        }

        @Override
        public int getItemCount()
        {
            return (mCursor == null) ? 0 : mCursor.getCount();
        }
    }
}