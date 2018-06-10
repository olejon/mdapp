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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class NotesSQLiteHelper extends SQLiteOpenHelper
{
	private static final String DB_NAME = "notes.db";

	private static final int DB_VERSION = 1;

	static final String TABLE = "notes";

	static final String COLUMN_ID = "_id";
	static final String COLUMN_TITLE = "title";
	static final String COLUMN_TEXT = "text";
	static final String COLUMN_DATA = "data";
	static final String COLUMN_PATIENT_ID = "patient_id";
	static final String COLUMN_PATIENT_NAME = "patient_name";
	static final String COLUMN_PATIENT_DOCTOR = "patient_doctor";
	static final String COLUMN_PATIENT_DEPARTMENT = "patient_department";
	static final String COLUMN_PATIENT_ROOM = "patient_room";
	static final String COLUMN_PATIENT_MEDICATIONS = "patient_medications";

	NotesSQLiteHelper(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE "+TABLE+"("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ON CONFLICT IGNORE, "+COLUMN_TITLE+" TEXT, "+COLUMN_TEXT+" TEXT, "+COLUMN_DATA+" TEXT, "+COLUMN_PATIENT_ID+" TEXT, "+COLUMN_PATIENT_NAME+" TEXT, "+COLUMN_PATIENT_DOCTOR+" TEXT, "+COLUMN_PATIENT_DEPARTMENT+" TEXT, "+COLUMN_PATIENT_ROOM+" TEXT, "+COLUMN_PATIENT_MEDICATIONS+" TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS "+TABLE);

		onCreate(db);
	}
}