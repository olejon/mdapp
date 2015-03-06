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

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MessageIntentService extends IntentService
{
    private static final int NOTIFICATION_ID = 1;

    public MessageIntentService()
    {
        super("MessageIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        final Context mContext = this;

        final MyTools mTools = new MyTools(mContext);

        RequestQueue requestQueue = Volley.newRequestQueue(mContext);

        int projectVersionCode = mTools.getProjectVersionCode();

        String device = mTools.getDevice();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, getString(R.string.project_website_uri)+"api/1/message/?version_code="+projectVersionCode+"&device="+device, null, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {
                    final long id = response.getLong("id");
                    final String title = response.getString("title");
                    final String message = response.getString("message");
                    final String bigMessage = response.getString("big_message");
                    final String button = response.getString("button");
                    final String uri = response.getString("uri");

                    final long lastId = mTools.getSharedPreferencesLong("MESSAGE_LAST_ID");

                    mTools.setSharedPreferencesLong("MESSAGE_LAST_ID", id);

                    if(lastId != 0 && id != lastId)
                    {
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext);

                        notificationBuilder.setWhen(mTools.getCurrentTime())
                                .setAutoCancel(true)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setVisibility(Notification.VISIBILITY_PUBLIC)
                                .setCategory(Notification.CATEGORY_MESSAGE)
                                .setLargeIcon(bitmap)
                                .setSmallIcon(R.drawable.ic_local_hospital_white_24dp)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setLights(Color.BLUE, 1000, 2000)
                                .setTicker(message)
                                .setContentTitle(title)
                                .setContentText(message)
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigMessage));

                        if(!uri.equals(""))
                        {
                            Intent launchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            PendingIntent launchPendingIntent = PendingIntent.getActivity(mContext, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            notificationBuilder.setContentIntent(launchPendingIntent).addAction(R.drawable.ic_local_hospital_white_24dp, button, launchPendingIntent);
                        }

                        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
                    }
                }
                catch(Exception e)
                {
                    Log.e("MessageIntentService", Log.getStackTraceString(e));
                }
            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("MessageIntentService", error.toString());
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(jsonObjectRequest);
    }
}