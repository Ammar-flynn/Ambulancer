package com.example.dsproject;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.*;
import okhttp3.*;

public class SupabaseService {

    private static final String BASE_URL = "https://kyipxxbxoipghwkoxwhs.supabase.co/rest/v1/";
    private static final String KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt5aXB4eGJ4b2lwZ2h3a294d2hzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjY3NjI2ODYsImV4cCI6MjA4MjMzODY4Nn0.uWIg7zYzEtIBXAYb69n0_FpBGt7VMasje9b_D9gfQtA";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Context context;

    public SupabaseService(Context context) {
        this.context = context;
    }

    // ---------------- Emergency operations ----------------

    public void saveEmergency(Emergency e) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", e.name);
            json.put("address", e.address);
            json.put("type", e.type);
            json.put("symptoms", e.symptoms);
            json.put("sub_priority", e.subPriority);
            json.put("timestamp", e.timestamp);
            json.put("status", e.status);
            json.put("track", e.track);
            json.put("HospitalAssigned", e.HospitalAssigned);

            Request request = new Request.Builder()
                    .url(BASE_URL + "emergencies")
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("SupabaseService", "Failed to save emergency", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("SupabaseService", "Saved emergency: " + response.code());
                    response.close();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ---------------- User authentication ----------------

    // Send Assigned notification to user
    public Emergency fetchEmergencyByTrack(String track) {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "emergencies?track=eq." + track)
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonString = response.body().string();
                    JSONArray array = new JSONArray(jsonString);
                    if (array.length() == 0) return null;

                    JSONObject obj = array.getJSONObject(0);
                    Emergency e = new Emergency(
                            obj.getString("id"),
                            obj.getString("name"),
                            obj.getString("address"),
                            obj.has("symptoms") ? obj.getString("symptoms") : "",
                            obj.getString("type"),
                            obj.getInt("sub_priority"),
                            obj.getString("status"),
                            obj.getString("track"),
                            obj.getString("HospitalAssigned")
                    );
                    e.timestamp = obj.getLong("timestamp");
                    return e;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void MarkArrived(String track) {
        try {
            JSONObject json = new JSONObject();
            json.put("status", "Arrived");

            Request request = new Request.Builder()
                    .url(BASE_URL + "emergencies?track=eq." + track)
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .header("Content-Type", "application/json")
                    .patch(RequestBody.create(json.toString(), MediaType.parse("application/json")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("SupabaseService", "Failed to mark as arrived", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("SupabaseService", "Marked as arrived: " + response.code());
                    response.close();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public List<Hospital> fetchAllHospitals() {
        List<Hospital> hospitals = new ArrayList<>();

        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "users?select=name,Branch")
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .header("Content-Type", "application/json")
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonString = response.body().string();
                    JsonArray array = JsonParser.parseString(jsonString).getAsJsonArray();

                    for (com.google.gson.JsonElement el : array) {
                        JsonObject obj = el.getAsJsonObject();

                        String name = "Unknown Hospital";
                        String branch = "Main Branch";

                        if (obj.has("name") && !obj.get("name").isJsonNull()) {
                            name = obj.get("name").getAsString();
                        }

                        if (obj.has("Branch") && !obj.get("Branch").isJsonNull()) {
                            branch = obj.get("Branch").getAsString();
                        }

                        hospitals.add(new Hospital(name, branch));
                    }
                } else {
                    Log.e("SupabaseService", "Error fetching hospitals: " + response.code());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("SupabaseService", "Failed to fetch hospitals: " + e.getMessage());
        }

        return hospitals;
    }

}