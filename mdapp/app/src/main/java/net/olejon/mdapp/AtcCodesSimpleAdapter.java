package net.olejon.mdapp;

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