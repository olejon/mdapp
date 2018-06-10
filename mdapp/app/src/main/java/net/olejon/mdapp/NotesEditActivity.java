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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class NotesEditActivity extends AppCompatActivity
{
	private static final int NOTES_EDIT_MEDICATION_REQUEST_CODE = 1;

	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	private ScrollView mScrollView;
	private TextInputLayout mTitleInputLayout;
	private TextInputLayout mTextInputLayout;
	private EditText mTitleEditText;
	private EditText mTextEditText;
	private EditText mPatientIdEditText;
	private EditText mPatientNameEditText;
	private EditText mPatientDoctorEditText;
	private EditText mPatientDepartmentEditText;
	private EditText mPatientRoomEditText;

	private JSONArray mPatientMedicationsJsonArray = new JSONArray();

	private int mNoteId;

	private boolean mNoteHasBeenChanged;

	// Create activity
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Intent
		Intent intent = getIntent();

		String noteTitle = intent.getStringExtra("title");

		mNoteId = intent.getIntExtra("id", 0);

		// Input manager
		final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		if(mNoteId == 0)
		{
			Handler handler = new Handler();

			handler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					if(inputMethodManager != null) inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
				}
			}, 250);
		}

		// Layout
		setContentView(R.layout.activity_notes_edit);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.notes_edit_toolbar);

		String title = (mNoteId == 0) ? getString(R.string.notes_edit_title_new) : getString(R.string.notes_edit_title_edit);

		toolbar.setTitle(title);

		setSupportActionBar(toolbar);
		if(getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Elements
		mScrollView = findViewById(R.id.notes_edit_inner_layout);

		TextInputLayout patientIdInputLayout;
		TextInputLayout patientNameInputLayout;
		TextInputLayout patientDoctorInputLayout;
		TextInputLayout patientDepartmentInputLayout;
		TextInputLayout patientRoomInputLayout;

		mTitleInputLayout = findViewById(R.id.notes_edit_title_layout);
		mTextInputLayout = findViewById(R.id.notes_edit_text_layout);

		patientIdInputLayout = findViewById(R.id.notes_edit_patient_id_layout);
		patientNameInputLayout = findViewById(R.id.notes_edit_patient_name_layout);
		patientDoctorInputLayout = findViewById(R.id.notes_edit_patient_doctor_layout);
		patientDepartmentInputLayout = findViewById(R.id.notes_edit_patient_department_layout);
		patientRoomInputLayout = findViewById(R.id.notes_edit_patient_room_layout);

		mTitleEditText = findViewById(R.id.notes_edit_title);
		mTextEditText = findViewById(R.id.notes_edit_text);
		mPatientIdEditText = findViewById(R.id.notes_edit_patient_id);
		mPatientNameEditText = findViewById(R.id.notes_edit_patient_name);
		mPatientDoctorEditText = findViewById(R.id.notes_edit_patient_doctor);
		mPatientDepartmentEditText = findViewById(R.id.notes_edit_patient_department);
		mPatientRoomEditText = findViewById(R.id.notes_edit_patient_room);

		mTitleInputLayout.setHintAnimationEnabled(true);
		mTextInputLayout.setHintAnimationEnabled(true);

		patientIdInputLayout.setHintAnimationEnabled(true);
		patientNameInputLayout.setHintAnimationEnabled(true);
		patientDoctorInputLayout.setHintAnimationEnabled(true);
		patientDepartmentInputLayout.setHintAnimationEnabled(true);
		patientRoomInputLayout.setHintAnimationEnabled(true);

		if(noteTitle != null && !noteTitle.equals("")) mTitleEditText.setText(noteTitle);

		Button patientAddMedicationButton = findViewById(R.id.notes_edit_patient_add_medication);

		patientAddMedicationButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent intent = new Intent(mContext, NotesEditMedicationsActivity.class);
				startActivityForResult(intent, NOTES_EDIT_MEDICATION_REQUEST_CODE);
			}
		});

		// Note
		getNote();
	}

	// Activity result
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == NOTES_EDIT_MEDICATION_REQUEST_CODE && data != null)
		{
			if(resultCode == RESULT_OK)
			{
				mNoteHasBeenChanged = true;

				String name = data.getStringExtra("name");

				try
				{
					JSONObject jsonObject = new JSONObject();

					jsonObject.put("name", name);

					mPatientMedicationsJsonArray.put(jsonObject);

					getMedications();

					mScrollView.post(new Runnable()
					{
						@Override
						public void run()
						{
							mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
						}
					});
				}
				catch(Exception e)
				{
					Log.e("NotesEditActivity", Log.getStackTraceString(e));
				}
			}
		}
	}

	// Back button
	@Override
	public void onBackPressed()
	{
		showSaveNoteDialog();
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_notes_edit, menu);

		if(mNoteId != 0)
		{
			MenuItem menuItem = menu.add(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.notes_edit_menu_delete)).setIcon(R.drawable.ic_delete_white_24dp);
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
			{
				@Override
				public boolean onMenuItemClick(MenuItem menuItem)
				{
					deleteNote(false);
					return true;
				}
			});
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case android.R.id.home:
			{
				showSaveNoteDialog();
				return true;
			}
			case R.id.notes_edit_menu_save:
			{
				saveNote();
				return true;
			}
			default:
			{
				return super.onOptionsItemSelected(item);
			}
		}
	}

	// Get note
	private void getNote()
	{
		mNoteHasBeenChanged = false;

		if(mNoteId != 0)
		{
			SQLiteDatabase sqLiteDatabase = new NotesSQLiteHelper(mContext).getReadableDatabase();
			String[] queryColumns = {NotesSQLiteHelper.COLUMN_TITLE, NotesSQLiteHelper.COLUMN_TEXT, NotesSQLiteHelper.COLUMN_PATIENT_ID, NotesSQLiteHelper.COLUMN_PATIENT_NAME, NotesSQLiteHelper.COLUMN_PATIENT_DOCTOR, NotesSQLiteHelper.COLUMN_PATIENT_DEPARTMENT, NotesSQLiteHelper.COLUMN_PATIENT_ROOM, NotesSQLiteHelper.COLUMN_PATIENT_MEDICATIONS};
			Cursor cursor = sqLiteDatabase.query(NotesSQLiteHelper.TABLE, queryColumns, NotesSQLiteHelper.COLUMN_ID+" = "+mNoteId, null, null, null, null);

			if(cursor.moveToFirst())
			{
				String title = cursor.getString(cursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_TITLE));
				String text = cursor.getString(cursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_TEXT));
				String patientId = cursor.getString(cursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_PATIENT_ID));
				String patientName = cursor.getString(cursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_PATIENT_NAME));
				String patientDoctor = cursor.getString(cursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_PATIENT_DOCTOR));
				String patientDepartment = cursor.getString(cursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_PATIENT_DEPARTMENT));
				String patientRoom = cursor.getString(cursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_PATIENT_ROOM));
				String patientMedications = cursor.getString(cursor.getColumnIndexOrThrow(NotesSQLiteHelper.COLUMN_PATIENT_MEDICATIONS));

				mTitleEditText.setText(title);
				mTextEditText.setText(text);
				mPatientIdEditText.setText(patientId);
				mPatientNameEditText.setText(patientName);
				mPatientDoctorEditText.setText(patientDoctor);
				mPatientDepartmentEditText.setText(patientDepartment);
				mPatientRoomEditText.setText(patientRoom);

				if(patientMedications != null && !patientMedications.equals(""))
				{
					try
					{
						mPatientMedicationsJsonArray = new JSONArray(patientMedications);

						getMedications();
					}
					catch(Exception e)
					{
						Log.e("NotesEditActivity", Log.getStackTraceString(e));
					}
				}
			}

			cursor.close();
			sqLiteDatabase.close();

			mTitleEditText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
				{
					mNoteHasBeenChanged = true;
					mTitleInputLayout.setError(null);
				}

				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

				@Override
				public void afterTextChanged(Editable editable) { }
			});

			mTextEditText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
				{
					mNoteHasBeenChanged = true;
					mTextInputLayout.setError(null);
				}

				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

				@Override
				public void afterTextChanged(Editable editable) { }
			});

			mPatientIdEditText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
				{
					mNoteHasBeenChanged = true;
				}

				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

				@Override
				public void afterTextChanged(Editable editable) { }
			});

			mPatientNameEditText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
				{
					mNoteHasBeenChanged = true;
				}

				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

				@Override
				public void afterTextChanged(Editable editable) { }
			});

			mPatientDoctorEditText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
				{
					mNoteHasBeenChanged = true;
				}

				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

				@Override
				public void afterTextChanged(Editable editable) { }
			});

			mPatientDepartmentEditText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
				{
					mNoteHasBeenChanged = true;
				}

				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

				@Override
				public void afterTextChanged(Editable editable) { }
			});

			mPatientRoomEditText.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
				{
					mNoteHasBeenChanged = true;
				}

				@Override
				public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

				@Override
				public void afterTextChanged(Editable editable) { }
			});
		}
	}

	// Save note
	private void saveNote()
	{
		String title = mTitleEditText.getText().toString().trim();
		String text = mTextEditText.getText().toString().trim();
		String patientId = mPatientIdEditText.getText().toString().trim();
		String patientName = mPatientNameEditText.getText().toString().trim();
		String patientDoctor = mPatientDoctorEditText.getText().toString().trim();
		String patientDepartment = mPatientDepartmentEditText.getText().toString().trim();
		String patientRoom = mPatientRoomEditText.getText().toString().trim();

		if(title.equals(""))
		{
			mTitleInputLayout.setError(getString(R.string.notes_edit_invalid_values));
		}
		else if(text.equals(""))
		{
			mTextInputLayout.setError(getString(R.string.notes_edit_invalid_values));
		}
		else
		{
			ContentValues contentValues = new ContentValues();
			contentValues.put(NotesSQLiteHelper.COLUMN_TITLE, title);
			contentValues.put(NotesSQLiteHelper.COLUMN_TEXT, text);
			contentValues.put(NotesSQLiteHelper.COLUMN_DATA, "");
			contentValues.put(NotesSQLiteHelper.COLUMN_PATIENT_ID, patientId);
			contentValues.put(NotesSQLiteHelper.COLUMN_PATIENT_NAME, patientName);
			contentValues.put(NotesSQLiteHelper.COLUMN_PATIENT_DOCTOR, patientDoctor);
			contentValues.put(NotesSQLiteHelper.COLUMN_PATIENT_DEPARTMENT, patientDepartment);
			contentValues.put(NotesSQLiteHelper.COLUMN_PATIENT_ROOM, patientRoom);

			if(mPatientMedicationsJsonArray.length() == 0)
			{
				contentValues.put(NotesSQLiteHelper.COLUMN_PATIENT_MEDICATIONS, "");
			}
			else
			{
				contentValues.put(NotesSQLiteHelper.COLUMN_PATIENT_MEDICATIONS, mPatientMedicationsJsonArray.toString());
			}

			SQLiteDatabase sqLiteDatabase = new NotesSQLiteHelper(mContext).getWritableDatabase();

			if(mNoteId == 0)
			{
				sqLiteDatabase.insert(NotesSQLiteHelper.TABLE, null, contentValues);
			}
			else
			{
				sqLiteDatabase.update(NotesSQLiteHelper.TABLE, contentValues, NotesSQLiteHelper.COLUMN_ID+" = "+mNoteId, null);
			}

			sqLiteDatabase.close();

			mTools.showToast(getString(R.string.notes_edit_saved), 1);

			finish();
		}
	}

	private void showSaveNoteDialog()
	{
		String title = mTitleEditText.getText().toString().trim();
		String text = mTextEditText.getText().toString().trim();

		if(title.equals("") && text.equals("") || !mNoteHasBeenChanged)
		{
			finish();
		}
		else
		{
			new MaterialDialog.Builder(mContext).title(R.string.notes_edit_save_dialog_title).content(getString(R.string.notes_edit_save_dialog_message)).positiveText(R.string.notes_edit_save_dialog_positive_button).neutralText(R.string.notes_edit_save_dialog_neutral_button).onPositive(new MaterialDialog.SingleButtonCallback()
			{
				@Override
				public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
				{
					saveNote();
				}
			}).onNeutral(new MaterialDialog.SingleButtonCallback()
			{
				@Override
				public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
				{
					finish();
				}
			}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.black).show();
		}
	}

	// Delete note
	private void deleteNote(boolean delete)
	{
		if(delete)
		{
			SQLiteDatabase sqLiteDatabase = new NotesSQLiteHelper(mContext).getWritableDatabase();
			sqLiteDatabase.delete(NotesSQLiteHelper.TABLE, NotesSQLiteHelper.COLUMN_ID+" = "+mNoteId, null);
			sqLiteDatabase.close();

			mTools.showToast(getString(R.string.notes_edit_deleted), 1);

			finish();
		}
		else
		{
			new MaterialDialog.Builder(mContext).title(R.string.notes_edit_delete_dialog_title).content(getString(R.string.notes_edit_delete_dialog_message)).positiveText(R.string.notes_edit_delete_dialog_positive_button).neutralText(R.string.notes_edit_delete_dialog_neutral_button).onPositive(new MaterialDialog.SingleButtonCallback()
			{
				@Override
				public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction)
				{
					deleteNote(true);
				}
			}).contentColorRes(R.color.black).positiveColorRes(R.color.dark_blue).neutralColorRes(R.color.black).show();
		}
	}

	// Get medications
	private void getMedications()
	{
		try
		{
			int jsonArrayLength = mPatientMedicationsJsonArray.length();

			final ArrayList<String> arrayList = new ArrayList<>();

			final CharSequence[] stringArrayList = new String[jsonArrayLength + 2];

			stringArrayList[0] = getString(R.string.notes_edit_medications_dialog_interactions);
			stringArrayList[1] = getString(R.string.notes_edit_medications_dialog_remove_all);

			for(int i = 0; i < jsonArrayLength; i++)
			{
				JSONObject jsonObject = mPatientMedicationsJsonArray.getJSONObject(i);

				String name = jsonObject.getString("name");

				arrayList.add(name);

				stringArrayList[i + 2] = name;
			}

			StringBuilder names = new StringBuilder();

			for(int n = 0; n < arrayList.size(); n++)
			{
				names.append(arrayList.get(n)).append(", ");
			}

			names = new StringBuilder(names.toString().replaceAll(", $", ""));

			Button button = findViewById(R.id.notes_edit_patient_medications);

			if(jsonArrayLength == 0)
			{
				button.setVisibility(View.GONE);
			}
			else
			{
				button.setText(names.toString());
				button.setVisibility(View.VISIBLE);
			}

			button.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					new MaterialDialog.Builder(mContext).title(R.string.notes_edit_medications_dialog_title).items(stringArrayList).itemsCallback(new MaterialDialog.ListCallback()
					{
						@Override
						public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence)
						{
							switch(i)
							{
								case 0:
								{
									StringBuilder interactions = new StringBuilder();

									for(int n = 0; n < arrayList.size(); n++)
									{
										interactions.append(arrayList.get(n).replace(" ", "_")).append(" ");
									}

									interactions = new StringBuilder(interactions.toString().trim());

									Intent intent = new Intent(mContext, InteractionsCardsActivity.class);
									intent.putExtra("search", interactions.toString());
									startActivity(intent);

									break;
								}
								case 1:
								{
									try
									{
										mPatientMedicationsJsonArray = new JSONArray("[]");

										mNoteHasBeenChanged = true;

										getMedications();
									}
									catch(Exception e)
									{
										Log.e("NotesEditActivity", Log.getStackTraceString(e));
									}

									break;
								}
								default:
								{
									int position = i - 2;

									String name = arrayList.get(position);

									SQLiteDatabase sqLiteDatabase = new SlDataSQLiteHelper(mContext).getReadableDatabase();
									String[] queryColumns = {SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID};
									Cursor cursor = sqLiteDatabase.query(SlDataSQLiteHelper.TABLE_MEDICATIONS, queryColumns, SlDataSQLiteHelper.MEDICATIONS_COLUMN_NAME+" = "+mTools.sqe(name), null, null, null, null);

									if(cursor.moveToFirst())
									{
										long id = cursor.getLong(cursor.getColumnIndexOrThrow(SlDataSQLiteHelper.MEDICATIONS_COLUMN_ID));

										Intent intent = new Intent(mContext, MedicationActivity.class);

										if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && mTools.getDefaultSharedPreferencesBoolean("MEDICATION_MULTIPLE_DOCUMENTS")) intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK|Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

										intent.putExtra("id", id);
										startActivity(intent);
									}

									cursor.close();
									sqLiteDatabase.close();

									break;
								}
							}
						}
					}).itemsColorRes(R.color.dark_blue).show();
				}
			});
		}
		catch(Exception e)
		{
			Log.e("NotesEditActivity", Log.getStackTraceString(e));
		}
	}
}