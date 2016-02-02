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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class NotesActivity extends AppCompatActivity
{
    private final Activity mActivity = this;
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

        final AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.notes_layout_appbar);

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

        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.notes_toolbar_layout);
        collapsingToolbarLayout.setTitle(getString(R.string.notes_title));

        // Empty
        mEmptyTextView = (TextView) findViewById(R.id.notes_empty);

        // Recycler view
        mRecyclerView = (RecyclerView) findViewById(R.id.notes_cards);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(new NotesAdapter());
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
            new MaterialDialog.Builder(mContext).title(getString(R.string.notes_dialog_pin_code_title)).customView(R.layout.activity_notes_dialog_pin_code, true).positiveText(getString(R.string.notes_dialog_pin_code_positive_button)).negativeText(getString(R.string.notes_dialog_pin_code_negative_button)).onPositive(new MaterialDialog.SingleButtonCallback()
            {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                {
                    final TextInputLayout pinCodeTextInputLayout = (TextInputLayout) materialDialog.findViewById(R.id.notes_dialog_pin_code_layout);
                    pinCodeTextInputLayout.setHintAnimationEnabled(true);

                    EditText pinCodeEditText = (EditText) materialDialog.findViewById(R.id.notes_dialog_pin_code);

                    pinCodeEditText.addTextChangedListener(new TextWatcher()
                    {
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                        {
                            pinCodeTextInputLayout.setError(null);
                        }

                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                        @Override
                        public void afterTextChanged(Editable editable) { }
                    });

                    String pinCode = pinCodeEditText.getText().toString();

                    if(pinCode.length() < 4)
                    {
                        pinCodeTextInputLayout.setError(getString(R.string.notes_dialog_pin_code_invalid));
                    }
                    else
                    {
                        mTools.setSharedPreferencesString("NOTES_PIN_CODE", pinCode);

                        mIsAuthenticated = true;

                        materialDialog.dismiss();

                        getNotes();
                    }
                }
            }).onNegative(new MaterialDialog.SingleButtonCallback()
            {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                {
                    materialDialog.dismiss();

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
            new MaterialDialog.Builder(mContext).title(getString(R.string.notes_dialog_verify_pin_code_title)).customView(R.layout.activity_notes_dialog_verify_pin_code, true).positiveText(getString(R.string.notes_dialog_verify_pin_code_positive_button)).negativeText(getString(R.string.notes_dialog_verify_pin_code_negative_button)).neutralText(getString(R.string.notes_dialog_verify_pin_code_neutral_button)).onPositive(new MaterialDialog.SingleButtonCallback()
            {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                {
                    final TextInputLayout pinCodeTextInputLayout = (TextInputLayout) materialDialog.findViewById(R.id.notes_dialog_verify_pin_code_layout);
                    pinCodeTextInputLayout.setHintAnimationEnabled(true);

                    EditText pinCodeEditText = (EditText) materialDialog.findViewById(R.id.notes_dialog_verify_pin_code);

                    pinCodeEditText.addTextChangedListener(new TextWatcher()
                    {
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
                        {
                            pinCodeTextInputLayout.setError(null);
                        }

                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                        @Override
                        public void afterTextChanged(Editable editable) { }
                    });

                    String pinCode = pinCodeEditText.getText().toString();

                    if(pinCode.equals(mTools.getSharedPreferencesString("NOTES_PIN_CODE")))
                    {
                        GetNotesTask getNotesTask = new GetNotesTask();
                        getNotesTask.execute();

                        materialDialog.dismiss();
                    }
                    else
                    {
                        pinCodeTextInputLayout.setError(getString(R.string.notes_dialog_verify_pin_code_wrong));
                    }
                }
            }).onNegative(new MaterialDialog.SingleButtonCallback()
            {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                {
                    materialDialog.dismiss();

                    finish();
                }
            }).onNeutral(new MaterialDialog.SingleButtonCallback()
            {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                {
                    materialDialog.dismiss();

                    new MaterialDialog.Builder(mContext).title(getString(R.string.notes_dialog_reset_pin_code_title)).content(getString(R.string.notes_dialog_reset_pin_code_message)).positiveText(getString(R.string.notes_dialog_reset_pin_code_positive_button)).neutralText(getString(R.string.notes_dialog_reset_pin_code_neutral_button)).onPositive(new MaterialDialog.SingleButtonCallback()
                    {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
                        {
                            mTools.setSharedPreferencesString("NOTES_PIN_CODE", "");

                            SQLiteDatabase sqLiteDatabase = new NotesSQLiteHelper(mContext).getWritableDatabase();

                            sqLiteDatabase.delete(NotesSQLiteHelper.TABLE, null, null);

                            sqLiteDatabase.close();

                            mTools.showToast(getString(R.string.notes_dialog_reset_pin_code_reset), 1);

                            finish();
                        }
                    }).onNeutral(new MaterialDialog.SingleButtonCallback()
                    {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
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
                    }).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.black).show();
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

    private class GetNotesTask extends AsyncTask<Void, Void, Cursor>
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

                if(mTools.isTablet())
                {
                    int spanCount = (mCursor.getCount() == 1) ? 1 : 2;

                    mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL));
                }

                mRecyclerView.setAdapter(new NotesAdapter());
            }
        }

        @Override
        protected Cursor doInBackground(Void... voids)
        {
            mSqLiteDatabase = new NotesSQLiteHelper(mContext).getReadableDatabase();

            String[] queryColumns = {NotesSQLiteHelper.COLUMN_ID, NotesSQLiteHelper.COLUMN_TITLE, NotesSQLiteHelper.COLUMN_TEXT};
            mCursor = mSqLiteDatabase.query(NotesSQLiteHelper.TABLE, queryColumns, null, null, null, null, NotesSQLiteHelper.COLUMN_ID+" DESC");

            return mCursor;
        }
    }

    // Adapter
    private class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder>
    {
        private int mLastPosition = -1;

        private NotesAdapter() { }

        class NoteViewHolder extends RecyclerView.ViewHolder
        {
            private final CardView card;
            private final TextView title;
            private final TextView text;

            public NoteViewHolder(View view)
            {
                super(view);

                card = (CardView) view.findViewById(R.id.notes_card);
                title = (TextView) view.findViewById(R.id.notes_card_title);
                text = (TextView) view.findViewById(R.id.notes_card_text);
            }
        }

        @Override
        public NoteViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_notes_card, viewGroup, false);
            return new NoteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(NoteViewHolder viewHolder, int i)
        {
            if(mCursor.moveToPosition(i))
            {
                final int id = mCursor.getInt(mCursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_ID));
                final String title = mCursor.getString(mCursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_TITLE));
                final String text = mCursor.getString(mCursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_TEXT));

                viewHolder.title.setText(title);
                viewHolder.text.setText(text);

                viewHolder.card.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intent = new Intent(mContext, NotesEditActivity.class);
                        intent.putExtra("id", id);
                        mContext.startActivity(intent);
                    }
                });

                animateCard(viewHolder.card, i);
            }
        }

        @Override
        public int getItemCount()
        {
            return (mCursor == null) ? 0 : mCursor.getCount();
        }

        private void animateCard(View view, int position)
        {
            if(position > mLastPosition)
            {
                mLastPosition = position;

                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.card);
                view.startAnimation(animation);
            }
        }
    }
}