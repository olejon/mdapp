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
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

class AtcCodesSimpleAdapter extends SimpleAdapter
{
    private final Context mContext;

    private final String mAtcCode;

    public AtcCodesSimpleAdapter(Context context, String atcCode, ArrayList<HashMap<String, String>> data, String[] from, int[] to)
    {
        super(context, data, R.layout.activity_atc_codes_list_item, from, to);

        mContext = context;

        mAtcCode = atcCode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = super.getView(position, convertView, parent);

        TextView textView = (TextView) view.findViewById(R.id.atc_codes_list_item_code);

        String atcCode = (String) textView.getText();

        if(atcCode.equals(mAtcCode))
        {
            textView.setTextColor(mContext.getResources().getColor(R.color.purple));
        }
        else
        {
            textView.setTextColor(mContext.getResources().getColor(R.color.black));
        }

        return view;
    }
}