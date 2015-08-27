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
import android.database.Cursor;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class TasksSimpleCursorAdapter extends SimpleCursorAdapter
{
    private final Context mContext;

    private final Cursor mCursor;

    public TasksSimpleCursorAdapter(Context context, Cursor c, String[] from, int[] to)
    {
        super(context, R.layout.activity_tasks_list_item, c, from, to, 0);

        mContext = context;

        mCursor = c;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = super.getView(position, convertView, parent);

        TextView textView = (TextView) view.findViewById(R.id.tasks_list_item_task);

        if(mCursor.moveToPosition(position))
        {
            String completed = mCursor.getString(mCursor.getColumnIndexOrThrow(TasksSQLiteHelper.COLUMN_COMPLETED));

            if(completed.equals("yes"))
            {
                textView.setTextColor(ContextCompat.getColor(mContext, R.color.dark_grey));
                textView.setPaintFlags(textView.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else
            {
                textView.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                textView.setPaintFlags(textView.getPaintFlags()&(~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }

        return view;
    }
}