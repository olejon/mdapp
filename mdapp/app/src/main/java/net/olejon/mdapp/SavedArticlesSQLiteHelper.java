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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class SavedArticlesSQLiteHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "saved_articles.db";

    private static final int DB_VERSION = 1;

    public static final String TABLE = "saved_articles";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DOMAIN = "domain";
    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_WEBVIEW = "webview";

    public SavedArticlesSQLiteHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

     @Override
     public void onCreate(SQLiteDatabase db)
     {
        db.execSQL("CREATE TABLE "+TABLE+"("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COLUMN_TITLE+" TEXT, "+COLUMN_DOMAIN+" TEXT, "+COLUMN_URI+" TEXT, "+COLUMN_WEBVIEW+" TEXT);");
     }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);

        onCreate(db);
    }
}