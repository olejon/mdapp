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
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MedicationsSimpleCursorAdapter extends SimpleCursorAdapter
{
    private final MyTools mTools;

    public MedicationsSimpleCursorAdapter(Context context, Cursor c, String[] from, int[] to)
    {
        super(context, R.layout.fragment_medications_list_item, c, from, to, 0);

        mTools = new MyTools(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = super.getView(position, convertView, parent);

        TextView textView = (TextView) view.findViewById(R.id.main_medications_list_item_prescription_group);

        String prescriptionGroup = (String) textView.getText();

        switch(prescriptionGroup)
        {
            case "A":
            {
                mTools.setBackgroundDrawable(textView, R.drawable.main_medications_list_item_circle_red);
                break;
            }
            case "B":
            {
                mTools.setBackgroundDrawable(textView, R.drawable.main_medications_list_item_circle_orange);
                break;
            }
            case "C":
            {
                mTools.setBackgroundDrawable(textView, R.drawable.main_medications_list_item_circle_green);
                break;
            }
            case "F":
            {
                mTools.setBackgroundDrawable(textView, R.drawable.main_medications_list_item_circle_green);
                break;
            }
        }

        return view;
    }
}