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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class FelleskatalogenSQLiteHelper extends SQLiteOpenHelper
{
    private static final int DB_VERSION = 342;

    public static final String DB_NAME = "felleskatalogen.db";
    public static final String DB_ZIPPED_NAME = "felleskatalogen.db.zip";

    public static final String DB_HAS_BEEN_CREATED = "FELLESKATALOGEN_DATABASE_HAS_BEEN_CREATED_"+DB_VERSION;

    public static final String TABLE_MEDICATIONS = "medications";
    public static final String TABLE_SUBSTANCES = "substances";
    public static final String TABLE_MANUFACTURERS = "manufacturers";
    public static final String TABLE_PHARMACIES = "pharmacies";
    public static final String TABLE_ATC = "atc";
    public static final String TABLE_ATC_CODES = "atc_codes";

    public static final String MEDICATIONS_COLUMN_ID = "_id";
    public static final String MEDICATIONS_COLUMN_NAME = "name";
    public static final String MEDICATIONS_COLUMN_MANUFACTURER = "manufacturer";
    public static final String MEDICATIONS_COLUMN_MANUFACTURER_URI = "manufacturer_uri";
    public static final String MEDICATIONS_COLUMN_TYPE = "type";
    public static final String MEDICATIONS_COLUMN_ATC_CODES = "atc_codes";
    public static final String MEDICATIONS_COLUMN_PRESCRIPTION_GROUP = "prescription_group";
    public static final String MEDICATIONS_COLUMN_PRESCRIPTION_GROUP_DESCRIPTION = "prescription_group_description";
    public static final String MEDICATIONS_COLUMN_BLUE_PRESCRIPTION = "blue_prescription";
    public static final String MEDICATIONS_COLUMN_TRIANGLE = "triangle";
    public static final String MEDICATIONS_COLUMN_BLACK_TRIANGLE = "black_triangle";
    public static final String MEDICATIONS_COLUMN_DOPING_STATUS = "doping_status";
    public static final String MEDICATIONS_COLUMN_DOPING_STATUS_DESCRIPTION = "doping_status_description";
    public static final String MEDICATIONS_COLUMN_DOPING_STATUS_URI = "doping_status_uri";
    public static final String MEDICATIONS_COLUMN_SCHENGEN_CERTIFICATE = "schengen_certificate";
    public static final String MEDICATIONS_COLUMN_CONTENT = "content";
    public static final String MEDICATIONS_COLUMN_CONTENT_SECTIONS = "content_sections";
    public static final String MEDICATIONS_COLUMN_FULL_CONTENT_REFERENCE = "full_content_reference";
    public static final String MEDICATIONS_COLUMN_URI = "uri";
    public static final String MEDICATIONS_COLUMN_PICTURES_URI = "pictures_uri";
    public static final String MEDICATIONS_COLUMN_PATIENT_URI = "patient_uri";
    public static final String MEDICATIONS_COLUMN_SPC_URI = "spc_uri";
    public static final String MEDICATIONS_COLUMN_POISONING_URIS = "poisoning_uris";
    public static final String MEDICATIONS_COLUMN_SUBSTANCES = "substances";

    public static final String SUBSTANCES_COLUMN_ID = "_id";
    public static final String SUBSTANCES_COLUMN_NAME = "name";
    public static final String SUBSTANCES_COLUMN_MEDICATIONS = "medications";
    public static final String SUBSTANCES_COLUMN_MEDICATIONS_COUNT = "medications_count";
    public static final String SUBSTANCES_COLUMN_ATC_CODES = "atc_codes";
    public static final String SUBSTANCES_COLUMN_URI = "uri";

    public static final String MANUFACTURERS_COLUMN_ID = "_id";
    public static final String MANUFACTURERS_COLUMN_NAME = "name";
    public static final String MANUFACTURERS_COLUMN_INFORMATION = "information";
    public static final String MANUFACTURERS_COLUMN_MEDICATIONS = "medications";
    public static final String MANUFACTURERS_COLUMN_MEDICATIONS_COUNT = "medications_count";
    public static final String MANUFACTURERS_COLUMN_URI = "uri";

    public static final String PHARMACIES_COLUMN_ID = "_id";
    public static final String PHARMACIES_COLUMN_LOCATION = "location";
    private static final String PHARMACIES_COLUMN_DETAILS = "details";

    private static final String ATC_COLUMN_ID = "_id";
    public static final String ATC_COLUMN_ANATOMICAL_GROUP_LETTER = "anatomical_group_letter";
    public static final String ATC_COLUMN_ANATOMICAL_GROUP_NAME = "anatomical_group_name";
    private static final String ATC_COLUMN_ANATOMICAL_GROUP_DETAILS = "anatomical_group_details";
    private static final String ATC_COLUMN_ANATOMICAL_GROUP_URI = "anatomical_group_uri";

    private static final String ATC_CODES_COLUMN_ID = "_id";
    public static final String ATC_CODES_COLUMN_CODE = "code";
    private static final String ATC_CODES_COLUMN_SUBSTANCES = "substances";

    private final MyTools mTools;

    private final boolean dbHasBeenCreated;

    public FelleskatalogenSQLiteHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);

        mTools = new MyTools(context);

        dbHasBeenCreated = mTools.getSharedPreferencesBoolean(DB_HAS_BEEN_CREATED);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        if(!dbHasBeenCreated)
        {
            sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_MEDICATIONS+"("+MEDICATIONS_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+MEDICATIONS_COLUMN_NAME+" TEXT, "+MEDICATIONS_COLUMN_MANUFACTURER+" TEXT, "+MEDICATIONS_COLUMN_MANUFACTURER_URI+" TEXT, "+MEDICATIONS_COLUMN_TYPE+" TEXT, "+MEDICATIONS_COLUMN_ATC_CODES+" TEXT, "+MEDICATIONS_COLUMN_PRESCRIPTION_GROUP+" TEXT, "+MEDICATIONS_COLUMN_PRESCRIPTION_GROUP_DESCRIPTION+" TEXT, "+MEDICATIONS_COLUMN_BLUE_PRESCRIPTION+" TEXT, "+MEDICATIONS_COLUMN_TRIANGLE+" TEXT, "+MEDICATIONS_COLUMN_BLACK_TRIANGLE+" TEXT, "+MEDICATIONS_COLUMN_DOPING_STATUS+" TEXT, "+MEDICATIONS_COLUMN_DOPING_STATUS_DESCRIPTION+" TEXT, "+MEDICATIONS_COLUMN_DOPING_STATUS_URI+" TEXT, "+MEDICATIONS_COLUMN_SCHENGEN_CERTIFICATE+" TEXT, "+MEDICATIONS_COLUMN_CONTENT+" TEXT, "+MEDICATIONS_COLUMN_CONTENT_SECTIONS+" TEXT, "+MEDICATIONS_COLUMN_FULL_CONTENT_REFERENCE+" TEXT, "+MEDICATIONS_COLUMN_URI+" TEXT, "+MEDICATIONS_COLUMN_PICTURES_URI+" TEXT, "+MEDICATIONS_COLUMN_PATIENT_URI+" TEXT, "+MEDICATIONS_COLUMN_SPC_URI+" TEXT, "+MEDICATIONS_COLUMN_POISONING_URIS+" TEXT, "+MEDICATIONS_COLUMN_SUBSTANCES+" TEXT);");
            sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_SUBSTANCES+"("+SUBSTANCES_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+SUBSTANCES_COLUMN_NAME+" TEXT, "+SUBSTANCES_COLUMN_MEDICATIONS+" TEXT, "+SUBSTANCES_COLUMN_MEDICATIONS_COUNT+" TEXT, "+SUBSTANCES_COLUMN_ATC_CODES+" TEXT, "+SUBSTANCES_COLUMN_URI+" TEXT);");
            sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_MANUFACTURERS+"("+MANUFACTURERS_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+MANUFACTURERS_COLUMN_NAME+" TEXT, "+MANUFACTURERS_COLUMN_INFORMATION+" TEXT, "+MANUFACTURERS_COLUMN_MEDICATIONS+" TEXT, "+MANUFACTURERS_COLUMN_MEDICATIONS_COUNT+" TEXT, "+MANUFACTURERS_COLUMN_URI+" TEXT);");
            sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_PHARMACIES+"("+PHARMACIES_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+PHARMACIES_COLUMN_LOCATION+" TEXT, "+PHARMACIES_COLUMN_DETAILS+" TEXT);");
            sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_ATC+"("+ATC_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ATC_COLUMN_ANATOMICAL_GROUP_LETTER+" TEXT, "+ATC_COLUMN_ANATOMICAL_GROUP_NAME+" TEXT, "+ATC_COLUMN_ANATOMICAL_GROUP_DETAILS+" TEXT, "+ATC_COLUMN_ANATOMICAL_GROUP_URI+" TEXT);");
            sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_ATC_CODES+"("+ATC_CODES_COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ATC_CODES_COLUMN_CODE+" TEXT, "+ATC_CODES_COLUMN_SUBSTANCES+" TEXT);");

            mTools.setSharedPreferencesBoolean(DB_HAS_BEEN_CREATED, true);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2)
    {
        if(!dbHasBeenCreated)
        {
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_MEDICATIONS);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_SUBSTANCES);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_MANUFACTURERS);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_PHARMACIES);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_ATC);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+TABLE_ATC_CODES);

            onCreate(sqLiteDatabase);
        }
    }
}