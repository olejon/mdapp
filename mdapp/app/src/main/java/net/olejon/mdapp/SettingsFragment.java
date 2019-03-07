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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment
{
	// Create fragment view
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			Preference notificationChannelsPreference = findPreference("NOTIFICATION_CHANNELS");

			notificationChannelsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
			{
				@Override
				public boolean onPreferenceClick(Preference preference)
				{
					Intent intent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
					intent.putExtra("android.provider.extra.APP_PACKAGE", getActivity().getPackageName());
					startActivity(intent);

					return true;
				}
			});
		}
	}
}