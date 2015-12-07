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
import android.util.Log;

class SlDataSQLiteHelper extends SQLiteOpenHelper
{
    private static final int DB_VERSION = 50;

    public static final String DB_NAME = "data.db";

    public static final String DB_CREATED = "SQLITE_DATABASE_CREATED"+DB_VERSION;

    public static final String TABLE_MEDICATIONS = "medications";
    public static final String TABLE_SUBSTANCES = "substances";
    public static final String TABLE_ATC_ANATOMICAL_GROUPS = "atc_anatomical_groups";
    public static final String TABLE_ATC_PHARMACOLOGIC_GROUPS = "atc_pharmacologic_groups";
    public static final String TABLE_ATC_THERAPEUTIC_GROUPS = "atc_therapeutic_groups";
    public static final String TABLE_ATC_SUBSTANCES_GROUPS = "atc_substances_groups";
    public static final String TABLE_ATC_CODES = "atc_codes";
    public static final String TABLE_ICD_10 = "icd_10";
    public static final String TABLE_MANUFACTURERS = "manufacturers";
    public static final String TABLE_MUNICIPALITIES = "municipalities";
    public static final String TABLE_PHARMACIES = "pharmacies";

    public static final String MEDICATIONS_COLUMN_ID = "_id";
    public static final String MEDICATIONS_COLUMN_PRESCRIPTION_GROUP = "prescription_group";
    public static final String MEDICATIONS_COLUMN_NAME = "name";
    public static final String MEDICATIONS_COLUMN_SUBSTANCE = "substance";
    public static final String MEDICATIONS_COLUMN_MANUFACTURER = "manufacturer";
    public static final String MEDICATIONS_COLUMN_BLUE_PRESCRIPTION = "blue_prescription";
    public static final String MEDICATIONS_COLUMN_ATC_CODE = "atc_code";

    public static final String SUBSTANCES_COLUMN_ID = "_id";
    public static final String SUBSTANCES_COLUMN_NAME = "name";
    public static final String SUBSTANCES_COLUMN_ATC_CODE = "atc_code";

    private static final String ATC_ANATOMICAL_GROUPS_COLUMN_ID = "_id";
    public static final String ATC_ANATOMICAL_GROUPS_COLUMN_CODE = "code";
    public static final String ATC_ANATOMICAL_GROUPS_COLUMN_NAME = "name";

    private static final String ATC_PHARMACOLOGIC_GROUPS_COLUMN_ID = "_id";
    public static final String ATC_PHARMACOLOGIC_GROUPS_COLUMN_CODE = "code";
    public static final String ATC_PHARMACOLOGIC_GROUPS_COLUMN_NAME = "name";

    private static final String ATC_THERAPEUTIC_GROUPS_COLUMN_ID = "_id";
    public static final String ATC_THERAPEUTIC_GROUPS_COLUMN_CODE = "code";
    public static final String ATC_THERAPEUTIC_GROUPS_COLUMN_NAME = "name";

    private static final String ATC_SUBSTANCES_GROUPS_COLUMN_ID = "_id";
    public static final String ATC_SUBSTANCES_GROUPS_COLUMN_CODE = "code";
    public static final String ATC_SUBSTANCES_GROUPS_COLUMN_NAME = "name";

    private static final String ATC_CODES_COLUMN_ID = "_id";
    public static final String ATC_CODES_COLUMN_CODE = "code";
    public static final String ATC_CODES_COLUMN_NAME = "name";

    public static final String ICD_10_COLUMN_ID = "_id";
    public static final String ICD_10_COLUMN_CHAPTER = "chapter";
    public static final String ICD_10_COLUMN_CODES = "codes";
    public static final String ICD_10_COLUMN_NAME = "name";
    public static final String ICD_10_COLUMN_DATA = "data";

    public static final String MANUFACTURERS_COLUMN_ID = "_id";
    public static final String MANUFACTURERS_COLUMN_NAME = "name";

    private static final String MUNICIPALITIES_COLUMN_ID = "_id";
    public static final String MUNICIPALITIES_COLUMN_NAME = "name";

    private static final String PHARMACIES_COLUMN_ID = "_id";
    public static final String PHARMACIES_COLUMN_NAME = "name";
    public static final String PHARMACIES_COLUMN_MUNICIPALITY = "municipality";
    public static final String PHARMACIES_COLUMN_ADDRESS = "address";

    private final MyTools mTools;

    private final boolean sqliteDatabaseHasBeenCreated;

    public SlDataSQLiteHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);

        mTools = new MyTools(context);

        sqliteDatabaseHasBeenCreated = mTools.getSharedPreferencesBoolean(DB_CREATED);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        if(!sqliteDatabaseHasBeenCreated)
        {
            try
            {
                db.execSQL("CREATE TABLE "+TABLE_MEDICATIONS+"("+MEDICATIONS_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+MEDICATIONS_COLUMN_PRESCRIPTION_GROUP+" TEXT, "+MEDICATIONS_COLUMN_NAME+" TEXT, "+MEDICATIONS_COLUMN_SUBSTANCE+" TEXT, "+MEDICATIONS_COLUMN_MANUFACTURER+" TEXT, "+MEDICATIONS_COLUMN_BLUE_PRESCRIPTION+" TEXT, "+MEDICATIONS_COLUMN_ATC_CODE+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_SUBSTANCES+"("+SUBSTANCES_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+SUBSTANCES_COLUMN_NAME+" TEXT, "+SUBSTANCES_COLUMN_ATC_CODE+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_ATC_ANATOMICAL_GROUPS+"("+ATC_ANATOMICAL_GROUPS_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ATC_ANATOMICAL_GROUPS_COLUMN_CODE+" TEXT, "+ATC_ANATOMICAL_GROUPS_COLUMN_NAME+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_ATC_PHARMACOLOGIC_GROUPS+"("+ATC_PHARMACOLOGIC_GROUPS_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ATC_PHARMACOLOGIC_GROUPS_COLUMN_CODE+" TEXT, "+ATC_PHARMACOLOGIC_GROUPS_COLUMN_NAME+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_ATC_THERAPEUTIC_GROUPS+"("+ATC_THERAPEUTIC_GROUPS_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ATC_THERAPEUTIC_GROUPS_COLUMN_CODE+" TEXT, "+ATC_THERAPEUTIC_GROUPS_COLUMN_NAME+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_ATC_SUBSTANCES_GROUPS+"("+ATC_SUBSTANCES_GROUPS_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ATC_SUBSTANCES_GROUPS_COLUMN_CODE+" TEXT, "+ATC_SUBSTANCES_GROUPS_COLUMN_NAME+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_ATC_CODES+"("+ATC_CODES_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ATC_CODES_COLUMN_CODE+" TEXT, "+ATC_CODES_COLUMN_NAME+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_ICD_10+"("+ICD_10_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ICD_10_COLUMN_CHAPTER+" TEXT, "+ICD_10_COLUMN_CODES+" TEXT, "+ICD_10_COLUMN_NAME+" TEXT, "+ICD_10_COLUMN_DATA+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_MANUFACTURERS+"("+MANUFACTURERS_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+MANUFACTURERS_COLUMN_NAME+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_MUNICIPALITIES+"("+MUNICIPALITIES_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+MUNICIPALITIES_COLUMN_NAME+" TEXT);");
                db.execSQL("CREATE TABLE "+TABLE_PHARMACIES+"("+PHARMACIES_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+PHARMACIES_COLUMN_NAME+" TEXT, "+PHARMACIES_COLUMN_MUNICIPALITY+" TEXT, "+PHARMACIES_COLUMN_ADDRESS+" TEXT);");
            }
            catch(Exception e)
            {
                Log.e("SlDataSQLiteHelper", Log.getStackTraceString(e));
            }

            mTools.setSharedPreferencesBoolean(DB_CREATED, true);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if(!sqliteDatabaseHasBeenCreated)
        {
            try
            {
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_MEDICATIONS);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_SUBSTANCES);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_ATC_ANATOMICAL_GROUPS);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_ATC_PHARMACOLOGIC_GROUPS);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_ATC_THERAPEUTIC_GROUPS);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_ATC_SUBSTANCES_GROUPS);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_ATC_CODES);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_ICD_10);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_MANUFACTURERS);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_MUNICIPALITIES);
                db.execSQL("DROP TABLE IF EXISTS "+TABLE_PHARMACIES);

                onCreate(db);
            }
            catch(Exception e)
            {
                Log.e("SlDataSQLiteHelper", Log.getStackTraceString(e));
            }
        }
    }
}