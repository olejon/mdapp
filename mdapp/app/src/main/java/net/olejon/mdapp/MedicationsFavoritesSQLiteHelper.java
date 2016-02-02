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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class MedicationsFavoritesSQLiteHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "medications_favorites.db";

    private static final int DB_VERSION = 6;

    public static final String TABLE = "medications_favorites";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PRESCRIPTION_GROUP = "prescription_group";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SUBSTANCE = "substance";
    public static final String COLUMN_MANUFACTURER = "manufacturer";

    public MedicationsFavoritesSQLiteHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE "+TABLE+"("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COLUMN_PRESCRIPTION_GROUP+" TEXT, "+COLUMN_NAME+" TEXT, "+COLUMN_SUBSTANCE+" TEXT, "+COLUMN_MANUFACTURER+" TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);

        onCreate(db);
    }
}