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
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class FelleskatalogenSQLiteCopyHelper
{
    private final Context mContext;

    public FelleskatalogenSQLiteCopyHelper(Context context)
    {
        mContext = context;
    }

    public void copy(InputStream inputStream)
    {
        SQLiteDatabase sqLiteDatabase = new FelleskatalogenSQLiteHelper(mContext).getReadableDatabase();

        try
        {
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            ZipEntry zipEntry;

            int count;
            byte[] buffer = new byte[1024];

            while((zipEntry = zipInputStream.getNextEntry()) != null)
            {
                Log.w("FelleskatalogenSQLiteCopyHelper", "Unzip: "+zipEntry.getName());

                FileOutputStream fileOutputStream = new FileOutputStream(mContext.getDatabasePath(FelleskatalogenSQLiteHelper.DB_NAME));

                while((count = zipInputStream.read(buffer)) != -1)
                {
                    fileOutputStream.write(buffer, 0, count);
                }

                fileOutputStream.close();
                zipInputStream.closeEntry();
            }

            zipInputStream.close();
            inputStream.close();
        }
        catch(Exception e)
        {
            Log.e("FelleskatalogenSQLiteCopyHelper", Log.getStackTraceString(e));
        }

        sqLiteDatabase.close();
    }
}