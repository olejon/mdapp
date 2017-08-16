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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

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

			Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext);

			notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH).setWhen(sentTime).setAutoCancel(true).setContentTitle(title).setContentText(text).setStyle(new NotificationCompat.BigTextStyle().bigText(bigText)).setSmallIcon(R.drawable.ic_local_hospital_white_24dp).setLargeIcon(bitmap).setColor(ContextCompat.getColor(mContext, R.color.light_blue));

			Intent uriActionIntent;

			if(uri.equals(""))
			{
				uriActionIntent = new Intent(mContext, MainActivity.class);
				uriActionIntent.setAction("android.intent.action.MAIN");
				uriActionIntent.addCategory("android.intent.category.LAUNCHER");
			}
			else
			{
				uriActionIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
			}

			PendingIntent uriActionPendingIntent = PendingIntent.getActivity(mContext, 0, uriActionIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			if(type.equals("notifications_from_slv"))
			{
				Intent settingsActionIntent = new Intent(mContext, SettingsActivity.class);
				settingsActionIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				PendingIntent settingsActionPendingIntent = PendingIntent.getActivity(mContext, 0, settingsActionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

				notificationBuilder.setContentIntent(uriActionPendingIntent).addAction(R.drawable.ic_notifications_white_24dp, uri_text, uriActionPendingIntent).addAction(R.drawable.ic_settings_white_24dp, getString(R.string.service_notifications_from_slv_settings), settingsActionPendingIntent);

				if(mTools.getDefaultSharedPreferencesBoolean("NOTIFICATIONS_FROM_SLV_NOTIFY_SOUND")) notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
				if(mTools.getDefaultSharedPreferencesBoolean("NOTIFICATIONS_FROM_SLV_NOTIFY_LED")) notificationBuilder.setLights(Color.BLUE, 1000, 2000);

				NotificationManagerCompat.from(mContext).notify(NOTIFICATION_NOTIFICATIONS_FROM_SLV_ID, notificationBuilder.build());
			}
			else
			{
				notificationBuilder.setContentIntent(uriActionPendingIntent).addAction(R.drawable.ic_local_hospital_white_24dp, uri_text, uriActionPendingIntent).setLights(Color.BLUE, 1000, 2000).setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

				NotificationManagerCompat.from(mContext).notify(NOTIFICATION_MESSAGE_ID, notificationBuilder.build());
			}
		}
	}

	@Override
	public void onDeletedMessages()
	{
		NotificationManagerCompat.from(mContext).cancel(NOTIFICATION_MESSAGE_ID);
		NotificationManagerCompat.from(mContext).cancel(NOTIFICATION_NOTIFICATIONS_FROM_SLV_ID);
	}
}