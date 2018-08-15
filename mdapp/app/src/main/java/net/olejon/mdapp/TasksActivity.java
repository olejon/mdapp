package net.olejon.mdapp;

/*

Copyright 2018 Ole Jon Bjørkum

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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

public class TasksActivity extends AppCompatActivity
{
	private final Activity mActivity = this;

	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private SQLiteDatabase mSqLiteDatabase;
	private Cursor mCursor;

	private TextView mEmptyTextView;
	private RecyclerView mRecyclerView;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Settings
		PreferenceManager.setDefaultValues(mContext, R.xml.settings, false);

		// Input manager
		final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// Layout
		setContentView(R.layout.activity_tasks);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.tasks_toolbar);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		AppBarLayout appBarLayout = findViewById(R.id.tasks_layout_appbar);

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
					mTools.setStatusbarColor(mActivity, R.color.statusbar);
				}
			}
		});

		CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.tasks_toolbar_layout);
		collapsingToolbarLayout.setTitle(getString(R.string.tasks_title));

		// Empty
		mEmptyTextView = findViewById(R.id.tasks_list_empty);

		// Recycler view
		mRecyclerView = findViewById(R.id.tasks_list);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(new TasksAdapter(mCursor));
		mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

		// Floating action button
		FloatingActionButton floatingActionButton = findViewById(R.id.tasks_fab);

		floatingActionButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				new MaterialDialog.Builder(mContext).title(R.string.tasks_dialog_title).customView(R.layout.activity_tasks_dialog, true).positiveText(R.string.tasks_dialog_positive_button).negativeText(R.string.tasks_dialog_negative_button).onPositive(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
					{
						final TextInputLayout taskTextInputLayout = (TextInputLayout) materialDialog.findViewById(R.id.tasks_dialog_task_layout);
						taskTextInputLayout.setHintAnimationEnabled(true);

						EditText taskEditText = (EditText) materialDialog.findViewById(R.id.tasks_dialog_task);

						taskEditText.addTextChangedListener(new TextWatcher()
						{
							@Override
							public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
							{
								taskTextInputLayout.setError(null);
							}

							@Override
							public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

							@Override
							public void afterTextChanged(Editable editable) { }
						});

						String task = taskEditText.getText().toString().trim();

						if(task.equals(""))
						{
							taskTextInputLayout.setError(getString(R.string.tasks_dialog_task_invalid_name));
						}
						else
						{
							ContentValues contentValues = new ContentValues();
							contentValues.put(TasksSQLiteHelper.COLUMN_TASK, task);
							contentValues.put(TasksSQLiteHelper.COLUMN_CREATED_TIME, "");
							contentValues.put(TasksSQLiteHelper.COLUMN_REMINDER_TIME, "");
							contentValues.put(TasksSQLiteHelper.COLUMN_COMPLETED, "no");

							mSqLiteDatabase.insert(TasksSQLiteHelper.TABLE, null, contentValues);

							materialDialog.dismiss();

							getTasks();
						}
					}
				}).onNegative(new MaterialDialog.SingleButtonCallback()
				{
					@Override
					public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
					{
						materialDialog.dismiss();
					}
				}).showListener(new DialogInterface.OnShowListener()
				{
					@Override
					public void onShow(DialogInterface dialogInterface)
					{
						if(inputMethodManager != null) inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
					}
				}).titleColorRes(R.color.teal).contentColorRes(R.color.dark).positiveColorRes(R.color.teal).negativeColorRes(R.color.dark).neutralColorRes(R.color.teal).buttonRippleColorRes(R.color.light_grey).autoDismiss(false).show();
			}
		});

		floatingActionButton.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fab));
		floatingActionButton.setVisibility(View.VISIBLE);

		// Get tasks
		getTasks();
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
		getMenuInflater().inflate(R.menu.menu_tasks, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
			{
				mTools.navigateUp(this);
				return true;
			}
			case R.id.tasks_menu_clear_tasks:
			{
				removeTasks();
				return true;
			}
			case R.id.tasks_menu_information:
			{
				showInformationDialog(true);
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
		if(!mTools.getSharedPreferencesBoolean("TASKS_HIDE_INFORMATION_DIALOG") || show)
		{
			new MaterialDialog.Builder(mContext).title(R.string.tasks_information_dialog_title).content(getString(R.string.tasks_information_dialog_message)).positiveText(R.string.tasks_information_dialog_positive_button).onPositive(new MaterialDialog.SingleButtonCallback()
			{
				@Override
				public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
				{
					mTools.setSharedPreferencesBoolean("TASKS_HIDE_INFORMATION_DIALOG", true);
				}
			}).titleColorRes(R.color.teal).contentColorRes(R.color.dark).positiveColorRes(R.color.teal).negativeColorRes(R.color.dark).neutralColorRes(R.color.teal).buttonRippleColorRes(R.color.light_grey).show();
		}
	}

	// Get tasks
	private void getTasks()
	{
		Handler handler = new Handler();

		handler.postDelayed(new Runnable() {
			@Override
			public void run()
			{
				mSqLiteDatabase = new TasksSQLiteHelper(mContext).getWritableDatabase();
				String[] queryColumns = {TasksSQLiteHelper.COLUMN_ID, TasksSQLiteHelper.COLUMN_TASK, TasksSQLiteHelper.COLUMN_COMPLETED};
				mCursor = mSqLiteDatabase.query(TasksSQLiteHelper.TABLE, queryColumns, null, null, null, null, TasksSQLiteHelper.COLUMN_COMPLETED+", "+TasksSQLiteHelper.COLUMN_TASK);

				if(mCursor.getCount() == 0)
				{
					mRecyclerView.setVisibility(View.GONE);
					mEmptyTextView.setVisibility(View.VISIBLE);
				}
				else
				{
					mEmptyTextView.setVisibility(View.GONE);
					mRecyclerView.setVisibility(View.VISIBLE);
					mRecyclerView.setAdapter(new TasksAdapter(mCursor));
				}

				showInformationDialog(false);
			}
		}, 250);
	}

	// Remove tasks
	private void removeTasks()
	{
		int removed = mSqLiteDatabase.delete(TasksSQLiteHelper.TABLE, TasksSQLiteHelper.COLUMN_COMPLETED+" = 'yes'", null);

		String tasks = (removed == 1) ? getString(R.string.tasks_task) : getString(R.string.tasks_tasks);

		mTools.showToast(removed+" "+tasks+" "+getString(R.string.tasks_removed), 0);

		getTasks();
	}

	// Adapter
	class TasksAdapter extends RecyclerView.Adapter<TasksAdapter.TasksViewHolder>
	{
		final Cursor mCursor;

		TasksAdapter(Cursor cursor)
		{
			mCursor = cursor;
		}

		class TasksViewHolder extends RecyclerView.ViewHolder
		{
			final LinearLayout listItem;
			final TextView task;

			TasksViewHolder(View view)
			{
				super(view);

				listItem = view.findViewById(R.id.tasks_list_item_layout);
				task = view.findViewById(R.id.tasks_list_item_task);
			}
		}

		@NonNull
		@Override
		public TasksViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
		{
			View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_tasks_list_item, viewGroup, false);
			return new TasksViewHolder(view);
		}

		@Override
		public void onBindViewHolder(@NonNull TasksViewHolder viewHolder, int i)
		{
			if(mCursor.moveToPosition(i))
			{
				final int taskId = mCursor.getInt(mCursor.getColumnIndexOrThrow(TasksSQLiteHelper.COLUMN_ID));

				final String taskCompleted = mCursor.getString(mCursor.getColumnIndexOrThrow(TasksSQLiteHelper.COLUMN_COMPLETED));

				String task = mCursor.getString(mCursor.getColumnIndexOrThrow(TasksSQLiteHelper.COLUMN_TASK));

				viewHolder.task.setText(task);

				if(taskCompleted.equals("yes"))
				{
					viewHolder.task.setTextColor(ContextCompat.getColor(mContext, R.color.dark_grey));
					viewHolder.task.setPaintFlags(viewHolder.task.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
				}
				else
				{
					viewHolder.task.setTextColor(ContextCompat.getColor(mContext, R.color.black));
					viewHolder.task.setPaintFlags(viewHolder.task.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
				}

				viewHolder.listItem.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						String completed = (taskCompleted.equals("no")) ? "yes" : "no";

						ContentValues contentValues = new ContentValues();
						contentValues.put(TasksSQLiteHelper.COLUMN_COMPLETED, completed);

						mSqLiteDatabase.update(TasksSQLiteHelper.TABLE, contentValues, TasksSQLiteHelper.COLUMN_ID+" = "+taskId, null);

						getTasks();
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