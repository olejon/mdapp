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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WelcomeFourthFragment extends Fragment
{
    private Activity mActivity;

    private MyTools mTools;

    // Create fragment view
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mActivity = getActivity();

        mTools = new MyTools(mActivity);

        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_welcome_fourth, container, false);

        TextView textView = (TextView) viewGroup.findViewById(R.id.welcome_page_4_button);

        textView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mTools.setSharedPreferencesBoolean("WELCOME_ACTIVITY_HAS_BEEN_SHOWN", true);

                mActivity.finish();
            }
        });

        return viewGroup;
    }
}