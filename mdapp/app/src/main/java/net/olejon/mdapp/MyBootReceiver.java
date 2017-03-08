package net.olejon.mdapp;

/*

Copyright 2017 Ole Jon Bjørkum

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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            final Context applicationContext = context.getApplicationContext();

            final MessageAlarm messageAlarm = new MessageAlarm();
            messageAlarm.setAlarm(applicationContext);

            final NotificationsFromSlvAlarm notificationsFromSlvAlarm = new NotificationsFromSlvAlarm();
            notificationsFromSlvAlarm.setAlarm(applicationContext);
        }
    }
}