// TrackManager.java - Android version (uses SharedPreferences)
package com.example.dsproject;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.*;

public class TrackManager {

    private static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000; // 1 day
    private static final String PREFS_NAME = "TrackPrefs";

    private final SharedPreferences preferences;

    public TrackManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Save track no locally in SharedPreferences
    public void saveTrackLocally(String track) {
        SharedPreferences.Editor editor = preferences.edit();
        long now = System.currentTimeMillis();
        editor.putLong(track, now);
        editor.apply();
    }

    // Load all tracks that are not older than 1 day
    public List<String> loadActiveTracks() {
        List<String> activeTracks = new ArrayList<>();
        long now = System.currentTimeMillis();

        Map<String, ?> allEntries = preferences.getAll();
        SharedPreferences.Editor editor = preferences.edit();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            try {
                long timestamp = (Long) entry.getValue();
                if (now - timestamp <= ONE_DAY_MILLIS) {
                    activeTracks.add(entry.getKey());
                } else {
                    editor.remove(entry.getKey()); // Remove expired
                }
            } catch (ClassCastException ex) {
                editor.remove(entry.getKey()); // Remove corrupted entry
            }
        }
        editor.apply();

        return activeTracks;
    }

    // Generate a New Track
    public String generateTrack() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}