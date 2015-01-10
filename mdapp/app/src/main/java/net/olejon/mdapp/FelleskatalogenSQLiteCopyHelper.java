package net.olejon.mdapp;

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