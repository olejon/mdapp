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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

public class NotesActivity extends ActionBarActivity
{
    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private SQLiteDatabase mSqLiteDatabase;
    private Cursor mCursor;

    private InputMethodManager mInputMethodManager;

    private TextView mEmptyTextView;
    private RecyclerView mRecyclerView;

    private boolean mIsAuthenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Input manager
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        // Layout
        setContentView(R.layout.activity_notes);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.notes_toolbar);
        toolbar.setTitle(getString(R.string.notes_title));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Empty
        mEmptyTextView = (TextView) findViewById(R.id.notes_empty);

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.notes_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new NotesAdapter(mContext, mCursor));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        // Floating action button
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.notes_fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(mContext, NotesEditActivity.class);
                startActivity(intent);
            }
        });

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.fab);

        floatingActionButton.startAnimation(animation);
        floatingActionButton.setVisibility(View.VISIBLE);
    }

    // Pause activity
    @Override
    protected void onPause()
    {
        super.onPause();

        mIsAuthenticated = true;
    }

    // Resume activity
    @Override
    protected void onResume()
    {
        super.onResume();

        // PIN code
        if(mTools.getSharedPreferencesString("NOTES_PIN_CODE").equals(""))
        {
            new MaterialDialog.Builder(mContext).title(getString(R.string.notes_dialog_pin_code_title)).customView(R.layout.activity_notes_dialog_pin_code, true).positiveText(getString(R.string.notes_dialog_pin_code_positive_button)).negativeText(getString(R.string.notes_dialog_pin_code_negative_button)).callback(new MaterialDialog.ButtonCallback()
            {
                @Override
                public void onPositive(MaterialDialog dialog)
                {
                    EditText pinCodeEditText = (EditText) dialog.findViewById(R.id.notes_dialog_pin_code);

                    String pinCode = pinCodeEditText.getText().toString();

                    if(pinCode.length() < 4)
                    {
                        mTools.showToast(getString(R.string.notes_dialog_pin_code_invalid), 1);
                    }
                    else
                    {
                        mTools.setSharedPreferencesString("NOTES_PIN_CODE", pinCode);

                        mIsAuthenticated = true;

                        dialog.dismiss();

                        getNotes();
                    }
                }

                @Override
                public void onNegative(MaterialDialog dialog)
                {
                    dialog.dismiss();

                    finish();
                }
            }).showListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface dialogInterface)
                {
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                }
            }).cancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialogInterface)
                {
                    dialogInterface.dismiss();

                    finish();
                }
            }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).autoDismiss(false).show();
        }
        else
        {
            getNotes();
        }
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    // Get notes
    private void getNotes()
    {
        if(mIsAuthenticated)
        {
            GetNotesTask getNotesTask = new GetNotesTask();
            getNotesTask.execute();
        }
        else
        {
            new MaterialDialog.Builder(mContext).title(getString(R.string.notes_dialog_verify_pin_code_title)).customView(R.layout.activity_notes_dialog_verify_pin_code, true).positiveText(getString(R.string.notes_dialog_verify_pin_code_positive_button)).negativeText(getString(R.string.notes_dialog_verify_pin_code_negative_button)).neutralText(getString(R.string.notes_dialog_verify_pin_code_neutral_button)).callback(new MaterialDialog.ButtonCallback()
            {
                @Override
                public void onPositive(MaterialDialog dialog)
                {
                    EditText pinCodeEditText = (EditText) dialog.findViewById(R.id.notes_dialog_verify_pin_code);

                    String pinCode = pinCodeEditText.getText().toString();

                    if(pinCode.equals(mTools.getSharedPreferencesString("NOTES_PIN_CODE")))
                    {
                        GetNotesTask getNotesTask = new GetNotesTask();
                        getNotesTask.execute();

                        dialog.dismiss();
                    }
                    else
                    {
                        mTools.showToast(getString(R.string.notes_dialog_verify_pin_code_wrong), 1);
                    }
                }

                @Override
                public void onNegative(MaterialDialog dialog)
                {
                    dialog.dismiss();

                    finish();
                }

                @Override
                public void onNeutral(MaterialDialog dialog)
                {
                    dialog.dismiss();

                    new MaterialDialog.Builder(mContext).title(getString(R.string.notes_dialog_reset_pin_code_title)).content(getString(R.string.notes_dialog_reset_pin_code_message)).positiveText(getString(R.string.notes_dialog_reset_pin_code_positive_button)).neutralText(getString(R.string.notes_dialog_reset_pin_code_neutral_button)).callback(new MaterialDialog.ButtonCallback()
                    {
                        @Override
                        public void onPositive(MaterialDialog dialog)
                        {
                            mTools.setSharedPreferencesString("NOTES_PIN_CODE", "");

                            SQLiteDatabase sqLiteDatabase = new NotesSQLiteHelper(mContext).getWritableDatabase();

                            sqLiteDatabase.delete(NotesSQLiteHelper.TABLE, null, null);

                            sqLiteDatabase.close();

                            mTools.showToast(getString(R.string.notes_dialog_reset_pin_code_reset), 1);

                            finish();
                        }

                        @Override
                        public void onNeutral(MaterialDialog dialog)
                        {
                            finish();
                        }
                    }).cancelListener(new DialogInterface.OnCancelListener()
                    {
                        @Override
                        public void onCancel(DialogInterface dialogInterface)
                        {
                            finish();
                        }
                    }).contentColorRes(R.color.black).positiveColorRes(R.color.red).neutralColorRes(R.color.dark_blue).show();
                }
            }).showListener(new DialogInterface.OnShowListener()
            {
                @Override
                public void onShow(DialogInterface dialogInterface)
                {
                    mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                }
            }).cancelListener(new DialogInterface.OnCancelListener()
            {
                @Override
                public void onCancel(DialogInterface dialogInterface)
                {
                    dialogInterface.dismiss();

                    finish();
                }
            }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).negativeColorRes(R.color.black).neutralColorRes(R.color.dark_blue).autoDismiss(false).show();
        }
    }

    private class GetNotesTask extends AsyncTask<Void, Void, SimpleCursorAdapter>
    {
        @Override
        protected void onPostExecute(final SimpleCursorAdapter simpleCursorAdapter)
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

                if(mTools.isTablet())
                {
                    int spanCount = (mCursor.getCount() == 1) ? 1 : 2;

                    mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
                }

                mRecyclerView.setAdapter(new NotesAdapter(mContext, mCursor));
            }
        }

        @Override
        protected SimpleCursorAdapter doInBackground(Void... voids)
        {
            mSqLiteDatabase = new NotesSQLiteHelper(mContext).getReadableDatabase();

            String[] queryColumns = {NotesSQLiteHelper.COLUMN_ID, NotesSQLiteHelper.COLUMN_TITLE, NotesSQLiteHelper.COLUMN_TEXT};
            mCursor = mSqLiteDatabase.query(NotesSQLiteHelper.TABLE, queryColumns, null, null, null, null, NotesSQLiteHelper.COLUMN_ID+" DESC");

            String[] fromColumns = {NotesSQLiteHelper.COLUMN_TITLE, NotesSQLiteHelper.COLUMN_TEXT};
            int[] toViews = {R.id.notes_card_title, R.id.notes_card_text};

            return new SimpleCursorAdapter(mContext, R.layout.activity_notes_card, mCursor, fromColumns, toViews, 0);
        }
    }
}