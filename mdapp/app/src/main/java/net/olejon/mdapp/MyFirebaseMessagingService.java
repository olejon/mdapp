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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	@Override
	public void onNewToken(String firebaseToken)
	{
		mTools.setSharedPreferencesString("FIREBASE_TOKEN", firebaseToken);

		Log.w("NewFirebaseToken", firebaseToken);
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage)
	{
		if(remoteMessage.getData().size() > 0 && remoteMessage.getData().containsKey("id") && remoteMessage.getData().containsKey("type") && remoteMessage.getData().containsKey("title") && remoteMessage.getData().containsKey("text") && remoteMessage.getData().containsKey("big_text") && remoteMessage.getData().containsKey("uri_text") && remoteMessage.getData().containsKey("uri"))
		{
			long sentTime = remoteMessage.getSentTime();

			Map<String,String> data = remoteMessage.getData();

			String id = data.get("id");
			String type = data.get("type");
			String title = data.get("title");
			String text = data.get("text");
			String bigText = data.get("big_text");
			String uri_text = data.get("uri_text");
			String uri = data.get("uri");

			NotificationManagerCompat notificationChannelManager = NotificationManagerCompat.from(mContext);

			NotificationCompat.Builder notificationChannelMessageBuilder = new NotificationCompat.Builder(mContext, MainActivity.NOTIFICATION_CHANNEL_MESSAGE_ID);
			NotificationCompat.Builder notificationChannelSlvBuilder = new NotificationCompat.Builder(mContext, MainActivity.NOTIFICATION_CHANNEL_SLV_ID);

			Intent firstActionIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			PendingIntent firstActionPendingIntent = PendingIntent.getActivity(mContext, 0, firstActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			Intent secondActionIntent = new Intent(mContext, NotificationsFromSlvActivity.class);
			secondActionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent secondActionPendingIntent = PendingIntent.getActivity(mContext, 0, secondActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			Intent thirdActionIntent = new Intent(mContext, SettingsActivity.class);
			thirdActionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent thirdActionPendingIntent = PendingIntent.getActivity(mContext, 0, thirdActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			notificationChannelMessageBuilder.setWhen(sentTime)
					.setAutoCancel(true)
					.setContentTitle(title)
					.setContentText(text)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
					.setSmallIcon(R.drawable.ic_local_hospital_white_24dp)
					.setColor(ContextCompat.getColor(mContext, R.color.light_blue))
					.setContentIntent(firstActionPendingIntent)
					.addAction(R.drawable.ic_notifications_white_24dp, uri_text, firstActionPendingIntent)
					.addAction(R.drawable.ic_settings_white_24dp, getString(R.string.notification_third_action), thirdActionPendingIntent);

			notificationChannelSlvBuilder.setWhen(sentTime)
					.setAutoCancel(true)
					.setContentTitle(title)
					.setContentText(text)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
					.setSmallIcon(R.drawable.ic_local_hospital_white_24dp)
					.setColor(ContextCompat.getColor(mContext, R.color.light_blue))
					.setContentIntent(firstActionPendingIntent)
					.addAction(R.drawable.ic_notifications_white_24dp, uri_text, firstActionPendingIntent)
					.addAction(R.drawable.ic_notifications_white_24dp, getString(R.string.notification_second_action), secondActionPendingIntent)
					.addAction(R.drawable.ic_settings_white_24dp, getString(R.string.notification_third_action), thirdActionPendingIntent);

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				notificationChannelMessageBuilder.setChannelId(MainActivity.NOTIFICATION_CHANNEL_MESSAGE_ID);
				notificationChannelSlvBuilder.setChannelId(MainActivity.NOTIFICATION_CHANNEL_SLV_ID);
			}
			else if(mTools.getDefaultSharedPreferencesBoolean("NOTIFICATIONS_NOTIFY"))
			{
				notificationChannelMessageBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND|NotificationCompat.DEFAULT_VIBRATE|NotificationCompat.DEFAULT_LIGHTS).setPriority(NotificationCompat.PRIORITY_HIGH);
				notificationChannelSlvBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND|NotificationCompat.DEFAULT_VIBRATE|NotificationCompat.DEFAULT_LIGHTS).setPriority(NotificationCompat.PRIORITY_HIGH);
			}

			switch(type)
			{
				case "notifications_from_slv":
				{
					notificationChannelManager.notify(Integer.valueOf(id), notificationChannelSlvBuilder.build());
					break;
				}
				default:
				{
					notificationChannelManager.notify(Integer.valueOf(id), notificationChannelMessageBuilder.build());
					break;
				}
			}
		}
	}
}