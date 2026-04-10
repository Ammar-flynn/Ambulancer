package com.example.Ambulancer.services;

import java.util.*;
import java.util.prefs.Preferences;

public class TrackManager {

    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000; // 1 day

    //save track no locally in local storage ( hidden storage in system no in file)
    public static void saveTrackLocally(String track) {
        Preferences prefs = Preferences.userNodeForPackage(TrackManager.class);
        long now = System.currentTimeMillis();
        prefs.put(track, String.valueOf(now));
    }


    //Load all tracks that are not older than 1 day to overcome time complexity Expired track are also removed after 1 day
    public static List<String> loadActiveTracks() {
        Preferences prefs = Preferences.userNodeForPackage(TrackManager.class);
        List<String> activeTracks = new ArrayList<>();
        long now = System.currentTimeMillis();

        try {
            for (String key : prefs.keys()) {
                try {
                    long timestamp = Long.parseLong(prefs.get(key, "0"));
                    if (now - timestamp <= ONE_DAY_MILLIS) {
                        activeTracks.add(key);
                    } else {
                        prefs.remove(key);
                    }
                } catch (NumberFormatException ex) {
                    prefs.remove(key); // remove corrupted entry
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return activeTracks;
    }

   //Generate a New Track in local
    public static String generateTrack() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}
