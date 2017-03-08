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

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationsFromSlvIntentService extends IntentService
{
    public static final int NOTIFICATION_ID = 2;

    public NotificationsFromSlvIntentService()
    {
        super("NotificationsFromSlvIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        final Context mContext = this;

        final MyTools mTools = new MyTools(mContext);

        if(mTools.getDefaultSharedPreferencesBoolean("NOTIFICATIONS_FROM_SLV_NOTIFY") && mTools.isDeviceConnected())
        {
            final Cache cache = new DiskBasedCache(getCacheDir(), 0);

            final Network network = new BasicNetwork(new HurlStack());

            final RequestQueue requestQueue = new RequestQueue(cache, network);

            requestQueue.start();

            int projectVersionCode = mTools.getProjectVersionCode();

            String device = mTools.getDevice();

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(getString(R.string.project_website_uri)+"api/1/notifications-from-slv/?first&version_code="+projectVersionCode+"&device="+device, new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
                    requestQueue.stop();

                    try
                    {
                        final JSONObject notifications = response.getJSONObject(0);

                        final String title = notifications.getString("title");
                        final String message = notifications.getString("message");

                        final String lastTitle = mTools.getSharedPreferencesString("NOTIFICATIONS_FROM_SLV_LAST_TITLE");

                        if(!title.equals(lastTitle))
                        {
                            if(!lastTitle.equals(""))
                            {
                                Intent readMoreIntent = new Intent(mContext, NotificationsFromSlvActivity.class);
                                readMoreIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                PendingIntent readMorePendingIntent = PendingIntent.getActivity(mContext, 0, readMoreIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                Intent settingsIntent = new Intent(mContext, SettingsActivity.class);
                                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                PendingIntent settingsPendingIntent = PendingIntent.getActivity(mContext, 0, settingsIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext);

                                notificationBuilder.setWhen(mTools.getCurrentTime())
                                        .setLargeIcon(bitmap)
                                        .setSmallIcon(R.drawable.ic_local_hospital_white_24dp)
                                        .setTicker(title)
                                        .setContentTitle(title)
                                        .setContentText(message)
                                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                        .setContentIntent(readMorePendingIntent)
                                        .addAction(R.drawable.ic_notifications_white_24dp, getString(R.string.service_notifications_from_slv_read_more), readMorePendingIntent)
                                        .addAction(R.drawable.ic_settings_white_24dp, getString(R.string.service_notifications_from_slv_settings), settingsPendingIntent);

                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) notificationBuilder.setPriority(Notification.PRIORITY_HIGH);

                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC).setCategory(Notification.CATEGORY_MESSAGE);

                                if(mTools.getDefaultSharedPreferencesBoolean("NOTIFICATIONS_FROM_SLV_NOTIFY_SOUND")) notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                if(mTools.getDefaultSharedPreferencesBoolean("NOTIFICATIONS_FROM_SLV_NOTIFY_LED")) notificationBuilder.setLights(Color.BLUE, 1000, 2000);

                                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                            }

                            mTools.setSharedPreferencesString("NOTIFICATIONS_FROM_SLV_LAST_TITLE", title);
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e("NotificationsFromSlv", Log.getStackTraceString(e));
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    requestQueue.stop();

                    Log.e("NotificationsFromSlv", error.toString());
                }
            });

            jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            requestQueue.add(jsonArrayRequest);
        }
    }
}