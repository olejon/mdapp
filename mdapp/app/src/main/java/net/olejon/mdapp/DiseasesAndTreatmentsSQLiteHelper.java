package net.olejon.mdapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DiseasesAndTreatmentsSQLiteHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "diseases_and_treatments_recent_searches.db";

    private static final int DB_VERSION = 2;

    public static final String TABLE = "diseases_and_treatments_recent_searches";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_STRING = "string";

    public DiseasesAndTreatmentsSQLiteHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE "+TABLE+"("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COLUMN_STRING+" TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);

        onCreate(db);
    }
}