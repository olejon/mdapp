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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider
{
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        final MyTools mTools = new MyTools(context);

        for(int appWidgetId : appWidgetIds)
        {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra("time", mTools.getCurrentTime());
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            remoteViews.setRemoteAdapter(R.id.widget_list, intent);
            remoteViews.setEmptyView(R.id.widget_list, R.id.widget_list_empty);

            Intent launchMainActivityIntent = new Intent(context, MainActivity.class);
            launchMainActivityIntent.setAction("android.intent.action.MAIN");
            launchMainActivityIntent.addCategory("android.intent.category.LAUNCHER");
            PendingIntent launchMainActivityPendingIntent = PendingIntent.getActivity(context, appWidgetId, launchMainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            Intent launchMedicationActivityIntent = new Intent(context, MedicationActivity.class);
            PendingIntent launchMedicationActivityPendingIntent = PendingIntent.getActivity(context, 0, launchMedicationActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            remoteViews.setOnClickPendingIntent(R.id.widget_toolbar, launchMainActivityPendingIntent);
            remoteViews.setPendingIntentTemplate(R.id.widget_list, launchMedicationActivityPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}