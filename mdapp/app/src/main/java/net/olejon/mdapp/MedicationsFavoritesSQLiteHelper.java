package net.olejon.mdapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class MedicationsFavoritesSQLiteHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "medications_favorites.db";

    private static final int DB_VERSION = 1;

    public static final String TABLE = "medications_favorites";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_MANUFACTURER = "manufacturer";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_PRESCRIPTION_GROUP = "prescription_group";
    public static final String COLUMN_URI = "uri";

    public MedicationsFavoritesSQLiteHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE "+TABLE+"("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COLUMN_NAME+" TEXT, "+COLUMN_MANUFACTURER+" TEXT, "+COLUMN_TYPE+" TEXT, "+COLUMN_PRESCRIPTION_GROUP+" TEXT, "+COLUMN_URI+" TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE);

        onCreate(db);
    }
}