package net.olejon.mdapp;

/*

Copyright 2018 Ole Jon Bjørkum

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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class WelcomeFourthFragment extends Fragment
{
	private Activity mActivity;

	private MyTools mTools;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		mActivity = getActivity();

		mTools = new MyTools(mActivity);

		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_welcome_fourth, container, false);

		Button button = viewGroup.findViewById(R.id.welcome_page_4_button);

		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				mTools.setSharedPreferencesBoolean("WELCOME_ACTIVITY_HAS_BEEN_SHOWN", true);

				mActivity.finish();
				mActivity.overridePendingTransition(0, R.anim.welcome_finish);
			}
		});

		return viewGroup;
	}
}