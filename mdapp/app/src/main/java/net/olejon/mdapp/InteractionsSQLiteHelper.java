package net.olejon.mdapp;

/*

Copyright 2017 Ole Jon Bjørkum

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

class InteractionsSQLiteHelper extends SQLiteOpenHelper
{
	private static final String DB_NAME = "interactions_recent_searches.db";

	private static final int DB_VERSION = 3400;

	static final String TABLE = "interactions_recent_searches";

	static final String COLUMN_ID = "_id";
	static final String COLUMN_STRING = "string";

	InteractionsSQLiteHelper(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE "+TABLE+"("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE ON CONFLICT IGNORE, "+COLUMN_STRING+" TEXT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS "+TABLE);

		onCreate(db);
	}
}