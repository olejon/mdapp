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

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class MDapp extends Application
{
    private final HashMap<TrackerName, Tracker> mTrackers = new HashMap<>();

    public enum TrackerName
    {
        APP_TRACKER
    }

    synchronized Tracker getTracker()
    {
        if(!mTrackers.containsKey(TrackerName.APP_TRACKER))
        {
            GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(this);

            Tracker tracker = googleAnalytics.newTracker(R.xml.app_tracker);

            mTrackers.put(TrackerName.APP_TRACKER, tracker);
        }

        return mTrackers.get(TrackerName.APP_TRACKER);
    }
}