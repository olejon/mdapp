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

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService
{
	public static final int NOTIFICATION_MESSAGE_ID = 1;
	public static final int NOTIFICATION_NOTIFICATIONS_FROM_SLV_ID = 2;

	private final Context mContext = this;

	private final MyTools mTools = new MyTools(mContext);

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage)
	{
		if(remoteMessage.getData().size() > 0 && remoteMessage.getData().containsKey("type") && remoteMessage.getData().containsKey("title") && remoteMessage.getData().containsKey("text") && remoteMessage.getData().containsKey("big_text") && remoteMessage.getData().containsKey("uri_text") && remoteMessage.getData().containsKey("uri"))
		{
			long sentTime = remoteMessage.getSentTime();

			Map<String,String> data = remoteMessage.getData();

			String type = data.get("type");
			String title = data.get("title");
			String text = data.get("text");
			String bigText = data.get("big_text");
			String uri_text = data.get("uri_text");
			String uri = data.get("uri");

			NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

			if(notificationManager != null)
			{
				Notification.Builder notificationBuilder = new Notification.Builder(mContext);

				notificationBuilder.setWhen(sentTime)
						.setAutoCancel(true)
						.setContentTitle(title)
						.setContentText(text)
						.setStyle(new Notification.BigTextStyle().bigText(bigText))
						.setSmallIcon(R.drawable.ic_local_hospital_white_24dp)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_icon));

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) notificationBuilder.setColor(getResources().getColor(R.color.light_blue));

				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
				{
					String notificationChannelId;
					String notificationChannelDescription;

					int notificationImportance = (mTools.getDefaultSharedPreferencesBoolean("NOTIFICATIONS_NOTIFY")) ? NotificationManager.IMPORTANCE_HIGH : NotificationManager.IMPORTANCE_LOW;

					if(type.equals("notifications_from_slv"))
					{
						notificationChannelId = "net.olejon.mdapp.NOTIFICATION_CHANNEL_SLV";
						notificationChannelDescription = getString(R.string.notification_channel_slv_description);
					}
					else
					{
						notificationChannelId = "net.olejon.mdapp.NOTIFICATION_CHANNEL_MESSAGE";
						notificationChannelDescription = getString(R.string.notification_channel_message_description);
					}

					NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, notificationChannelDescription, notificationImportance);
					notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
					notificationChannel.setShowBadge(true);
					notificationChannel.setDescription(text);
					notificationManager.createNotificationChannel(notificationChannel);
					notificationBuilder.setChannelId(notificationChannelId);
				}
				else if(mTools.getDefaultSharedPreferencesBoolean("NOTIFICATIONS_NOTIFY"))
				{
					notificationBuilder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE|Notification.DEFAULT_LIGHTS).setPriority(Notification.PRIORITY_HIGH);
				}

				Intent actionIntent;

				if(uri.equals(""))
				{
					actionIntent = new Intent(mContext, MainActivity.class);
					actionIntent.setAction("android.intent.action.MAIN");
					actionIntent.addCategory("android.intent.category.LAUNCHER");
				}
				else
				{
					actionIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
				}

				PendingIntent actionPendingIntent = PendingIntent.getActivity(mContext, 0, actionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

				if(type.equals("notifications_from_slv"))
				{
					String notificationActionSettings = getString(R.string.notification_action_settings);

					Intent settingsActionIntent = new Intent(mContext, SettingsActivity.class);
					settingsActionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					PendingIntent settingsActionPendingIntent = PendingIntent.getActivity(mContext, 0, settingsActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

					notificationBuilder.setContentIntent(actionPendingIntent).addAction(R.drawable.ic_notifications_white_24dp, uri_text, actionPendingIntent).addAction(R.drawable.ic_settings_white_24dp, notificationActionSettings, settingsActionPendingIntent);

					if(mTools.getDefaultSharedPreferencesBoolean("NOTIFICATIONS_NOTIFY_CHANNEL_SLV"))
					{
						notificationManager.notify(NOTIFICATION_NOTIFICATIONS_FROM_SLV_ID, notificationBuilder.build());
					}
				}
				else
				{
					notificationBuilder.setContentIntent(actionPendingIntent).addAction(R.drawable.ic_local_hospital_white_24dp, uri_text, actionPendingIntent);

					notificationManager.notify(NOTIFICATION_MESSAGE_ID, notificationBuilder.build());
				}
			}
		}
	}

	@Override
	public void onDeletedMessages()
	{
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if(notificationManager != null)
		{
			notificationManager.cancel(NOTIFICATION_MESSAGE_ID);
			notificationManager.cancel(NOTIFICATION_NOTIFICATIONS_FROM_SLV_ID);
		}
	}
}