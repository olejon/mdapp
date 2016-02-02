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
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MedicationsSimpleCursorAdapter extends SimpleCursorAdapter
{
    public MedicationsSimpleCursorAdapter(Context context, Cursor c, String[] from, int[] to)
    {
        super(context, R.layout.fragment_medications_list_item, c, from, to, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = super.getView(position, convertView, parent);

        TextView textView = (TextView) view.findViewById(R.id.main_medications_list_item_prescription_group);

        String prescriptionGroup = textView.getText().toString();

        switch(prescriptionGroup)
        {
            case "A":
            {
                textView.setBackgroundResource(R.drawable.main_medications_list_item_circle_red);
                break;
            }
            case "B":
            {
                textView.setBackgroundResource(R.drawable.main_medications_list_item_circle_orange);
                break;
            }
            case "C":
            {
                textView.setBackgroundResource(R.drawable.main_medications_list_item_circle_green);
                break;
            }
            case "F":
            {
                textView.setBackgroundResource(R.drawable.main_medications_list_item_circle_green);
                break;
            }
        }

        return view;
    }
}