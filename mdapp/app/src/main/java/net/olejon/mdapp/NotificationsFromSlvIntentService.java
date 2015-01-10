package net.olejon.mdapp;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

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

        if(mTools.getDefaultSharedPreferencesBoolean("NOTIFY_WHEN_NEW_NOTIFICATION_FROM_SLV") && mTools.isDeviceConnected())
        {
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(getString(R.string.project_website)+"api/1/notifications-from-slv/?first", new Response.Listener<JSONArray>()
            {
                @Override
                public void onResponse(JSONArray response)
                {
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
                                Intent launchIntent = new Intent(mContext, NotificationsFromSlvActivity.class);
                                PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

                                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext);

                                notificationBuilder.setWhen(mTools.getCurrentTime())
                                        .setPriority(Notification.PRIORITY_HIGH)
                                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                                        .setCategory(Notification.CATEGORY_MESSAGE)
                                        .setLargeIcon(bitmap)
                                        .setSmallIcon(R.drawable.ic_local_hospital_white_24dp)
                                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                        .setLights(Color.BLUE, 1000, 2000)
                                        .setTicker(title)
                                        .setContentTitle(title)
                                        .setContentText(message)
                                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                                        .setContentIntent(launchPendingIntent)
                                        .addAction(R.drawable.ic_notifications_white_24dp, getString(R.string.service_notifications_from_slv_read_more), launchPendingIntent);

                                notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                            }

                            mTools.setSharedPreferencesString("NOTIFICATIONS_FROM_SLV_LAST_TITLE", title);
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e("NotificationsFromSlvIntentService", Log.getStackTraceString(e));
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.e("NotificationsFromSlvIntentService", error.toString());
                }
            });

            requestQueue.add(jsonArrayRequest);
        }
    }
}