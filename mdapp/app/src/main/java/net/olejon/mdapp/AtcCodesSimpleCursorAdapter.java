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
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

class AtcCodesSimpleCursorAdapter extends SimpleCursorAdapter
{
    private final Context mContext;

    private final MyTools mTools;

    private final String mAtcCode;

    public AtcCodesSimpleCursorAdapter(String atcCode, Context context, Cursor c, String[] from, int[] to)
    {
        super(context, R.layout.activity_atc_codes_list_item, c, from, to, 0);

        mContext = context;

        mTools = new MyTools(mContext);

        mAtcCode = atcCode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = super.getView(position, convertView, parent);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.atc_codes_list_item_layout);

        TextView atcCodeTextView = (TextView) view.findViewById(R.id.atc_codes_list_item_code);
        TextView atcNameTextView = (TextView) view.findViewById(R.id.atc_codes_list_item_name);

        String atcCode = atcCodeTextView.getText().toString();

        if(atcCode.equals(mAtcCode))
        {
            mTools.setBackgroundDrawable(linearLayout, R.drawable.atc_codes_code);

            atcCodeTextView.setTextColor(mContext.getResources().getColor(R.color.purple));

            atcNameTextView.setTextColor(mContext.getResources().getColor(R.color.purple));
            atcNameTextView.setTypeface(Typeface.DEFAULT_BOLD);
        }
        else
        {
            linearLayout.setBackgroundColor(mContext.getResources().getColor(android.R.color.transparent));

            atcCodeTextView.setTextColor(mContext.getResources().getColor(android.R.color.black));

            atcNameTextView.setTextColor(mContext.getResources().getColor(android.R.color.black));
            atcNameTextView.setTypeface(Typeface.DEFAULT);
        }

        return view;
    }
}