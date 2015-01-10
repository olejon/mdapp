package net.olejon.mdapp;

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
            // Start alarms
            FelleskatalogenAlarm felleskatalogenAlarm = new FelleskatalogenAlarm();
            felleskatalogenAlarm.setAlarm(context.getApplicationContext());

            NotificationsFromSlvAlarm notificationsFromSlvAlarm = new NotificationsFromSlvAlarm();
            notificationsFromSlvAlarm.setAlarm(context.getApplicationContext());
        }
    }
}