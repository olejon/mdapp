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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotesEditActivity extends ActionBarActivity
{
    private static final int MEDICATION_REQUEST_CODE = 1;

    private final Context mContext = this;

    private final MyTools mTools = new MyTools(mContext);

    private EditText mTitleEditText;
    private EditText mTextEditText;
    private EditText mPatientIdEditText;
    private EditText mPatientNameEditText;
    private EditText mPatientDoctorEditText;
    private EditText mPatientDepartmentEditText;
    private EditText mPatientRoomEditText;

    private JSONArray mPatientMedicationsJsonArray = new JSONArray();

    private int noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Intent
        Intent intent = getIntent();

        final String noteTitle = intent.getStringExtra("title");

        noteId = intent.getIntExtra("id", 0);

        // Layout
        setContentView(R.layout.activity_notes_edit);

        // Toolbar
        final Toolbar toolbar = (Toolbar) findViewById(R.id.notes_edit_toolbar);

        final String title = (noteId == 0) ? getString(R.string.notes_edit_title_new) : getString(R.string.notes_edit_title_edit);

        toolbar.setTitle(title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Text fields and buttons
        mTitleEditText = (EditText) findViewById(R.id.notes_edit_title);
        mTextEditText = (EditText) findViewById(R.id.notes_edit_text);
        mPatientIdEditText = (EditText) findViewById(R.id.notes_edit_patient_id);
        mPatientNameEditText = (EditText) findViewById(R.id.notes_edit_patient_name);
        mPatientDoctorEditText = (EditText) findViewById(R.id.notes_edit_patient_doctor);
        mPatientDepartmentEditText = (EditText) findViewById(R.id.notes_edit_patient_department);
        mPatientRoomEditText = (EditText) findViewById(R.id.notes_edit_patient_room);

        if(noteTitle != null && !noteTitle.equals("")) mTitleEditText.setText(noteTitle);

        Button patientMedicationButton = (Button) findViewById(R.id.notes_edit_patient_add_medication);

        patientMedicationButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(mContext, NotesEditMedicationsActivity.class);
                startActivityForResult(intent, MEDICATION_REQUEST_CODE);
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

        if(requestCode == MEDICATION_REQUEST_CODE && data != null)
        {
            if(resultCode == RESULT_OK)
            {
                String name = data.getStringExtra("name");
                String manufacturer = data.getStringExtra("manufacturer");
                String uri = data.getStringExtra("uri");

                try
                {
                    JSONObject patientMedicationjsonObject = new JSONObject("{\"name\":\""+name+"\",\"manufacturer\":\""+manufacturer+"\",\"uri\":\""+uri+"\"}");

                    mPatientMedicationsJsonArray.put(patientMedicationjsonObject);

                    getMedications(mPatientMedicationsJsonArray);
                }
                catch(Exception e)
                {
                    Log.e("NotesEditActivity", Log.getStackTraceString(e));
                }
            }
        }
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_notes_edit, menu);

        if(noteId != 0)
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
                NavUtils.navigateUpFromSameTask(this);
                return true;
            }
            case R.id.notes_edit_menu_save:
            {
                editNote();
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
        if(noteId != 0)
        {
            SQLiteDatabase sqLiteDatabase = new NotesSQLiteHelper(mContext).getWritableDatabase();

            Cursor cursor = sqLiteDatabase.query(NotesSQLiteHelper.TABLE, null, NotesSQLiteHelper.COLUMN_ID+" = "+noteId, null, null, null, null);

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

                try
                {
                    JSONArray patientMedicationsJsonArray = new JSONArray(patientMedications);

                    mPatientMedicationsJsonArray = patientMedicationsJsonArray;

                    getMedications(patientMedicationsJsonArray);
                }
                catch(Exception e)
                {
                    Log.e("NotesEditActivity", Log.getStackTraceString(e));
                }
            }

            cursor.close();
            sqLiteDatabase.close();
        }
    }

    // Edit note
    private void editNote()
    {
        String title = mTitleEditText.getText().toString().trim();
        String text = mTextEditText.getText().toString().trim();
        String patientId = mPatientIdEditText.getText().toString().trim();
        String patientName = mPatientNameEditText.getText().toString().trim();
        String patientDoctor = mPatientDoctorEditText.getText().toString().trim();
        String patientDepartment = mPatientDepartmentEditText.getText().toString().trim();
        String patientRoom = mPatientRoomEditText.getText().toString().trim();

        if(title.equals("") || text.equals(""))
        {
            mTools.showToast(getString(R.string.notes_edit_invalid_values), 1);
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

            if(mPatientMedicationsJsonArray.length() > 0) contentValues.put(NotesSQLiteHelper.COLUMN_PATIENT_MEDICATIONS, mPatientMedicationsJsonArray.toString());

            SQLiteDatabase sqLiteDatabase = new NotesSQLiteHelper(mContext).getWritableDatabase();

            if(noteId == 0)
            {
                sqLiteDatabase.insert(NotesSQLiteHelper.TABLE, null, contentValues);
            }
            else
            {
                sqLiteDatabase.update(NotesSQLiteHelper.TABLE, contentValues, NotesSQLiteHelper.COLUMN_ID+" = "+noteId, null);
            }

            sqLiteDatabase.close();

            mTools.showToast(getString(R.string.notes_edit_saved), 1);

            finish();
        }
    }

    // Delete note
    private void deleteNote(boolean delete)
    {
        if(delete)
        {
            SQLiteDatabase sqLiteDatabase = new NotesSQLiteHelper(mContext).getWritableDatabase();

            sqLiteDatabase.delete(NotesSQLiteHelper.TABLE, NotesSQLiteHelper.COLUMN_ID+" = "+noteId, null);

            sqLiteDatabase.close();

            mTools.showToast(getString(R.string.notes_edit_deleted), 1);

            finish();
        }
        else
        {
            new MaterialDialog.Builder(mContext).title(getString(R.string.notes_edit_delete_dialog_title)).content(getString(R.string.notes_edit_delete_dialog_message)).positiveText(getString(R.string.notes_edit_delete_dialog_positive_button)).neutralText(getString(R.string.notes_edit_delete_dialog_neutral_button)).callback(new MaterialDialog.ButtonCallback()
            {
                @Override
                public void onPositive(MaterialDialog dialog)
                {
                    deleteNote(true);
                }
            }).contentColorRes(R.color.black).positiveColorRes(R.color.red).neutralColorRes(R.color.dark_blue).show();
        }
    }

    // Get medications
    private void getMedications(JSONArray medicationsJsonArray)
    {
        try
        {
            int medicationsJsonArrayLength = medicationsJsonArray.length();

            if(medicationsJsonArrayLength > 0)
            {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                TextView noMedicationsTextView = (TextView) findViewById(R.id.notes_edit_patient_no_medications);
                noMedicationsTextView.setVisibility(View.GONE);

                LinearLayout medicationsLinearLayout = (LinearLayout) findViewById(R.id.notes_edit_patient_medications);
                medicationsLinearLayout.setVisibility(View.VISIBLE);

                for(int i = 0; i < medicationsJsonArrayLength; i++)
                {
                    JSONObject medicationJsonObject = medicationsJsonArray.getJSONObject(i);

                    final String name = medicationJsonObject.getString("name");
                    final String uri = medicationJsonObject.getString("uri");

                    LinearLayout separatorLinearLayout = (LinearLayout) layoutInflater.inflate(R.layout.card_item_separator, null);
                    medicationsLinearLayout.addView(separatorLinearLayout);

                    TextView medicationTextView = (TextView) layoutInflater.inflate(R.layout.activity_notes_edit_patient_medications_item, null);
                    medicationTextView.setText(name);

                    medicationTextView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            mTools.getMedicationWithFullContent(uri);
                        }
                    });

                    medicationsLinearLayout.addView(medicationTextView);
                }
            }
        }
        catch(Exception e)
        {
            Log.e("NotesEditActivity", Log.getStackTraceString(e));
        }
    }
}
