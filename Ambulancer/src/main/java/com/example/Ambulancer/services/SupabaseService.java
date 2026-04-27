package com.example.Ambulancer.services;

import com.example.Ambulancer.model.Emergency;
import com.example.Ambulancer.model.EmergencyComparator;
import com.google.gson.*;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.scene.control.Alert;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.*;
import java.security.MessageDigest;
import java.util.*;

public class SupabaseService {

    private static final String BASE_URL = "https://kyipxxbxoipghwkoxwhs.supabase.co/rest/v1/";
    private static final String KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt5aXB4eGJ4b2lwZ2h3a294d2hzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjY3NjI2ODYsImV4cCI6MjA4MjMzODY4Nn0.uWIg7zYzEtIBXAYb69n0_FpBGt7VMasje9b_D9gfQtA";
    ;



    private final HttpClient client = HttpClient.newHttpClient();

    // ---------------- Emergency operations ----------------

    public void saveEmergency(Emergency e) {
        try {
            String url = BASE_URL + "emergencies";
            String json = String.format("""
                    {
                        "name": "%s",
                        "address": "%s",
                        "type": "%s",
                        "symptoms": "%s",
                        "sub_priority": %d,
                        "timestamp": %d,
                        "status": "%s",
                        "track": "%s",
                        "HospitalAssigned": "%s"
                    }
                    """, e.name, e.address, e.type, e.symptoms, e.subPriority, e.timestamp, e.status, e.track, e.HospitalAssigned);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Saved emergency: " + response.statusCode());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public PriorityQueue<Emergency> fetchEmergencies() {
        PriorityQueue<Emergency> queue = new PriorityQueue<>(new EmergencyComparator());
        try {
            String url = BASE_URL + "emergencies";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();

            for (JsonElement el : array) {
                JsonObject obj = el.getAsJsonObject();

                Emergency e = new Emergency(
                        safe(obj, "id"),
                        safe(obj, "name"),
                        safe(obj, "address"),
                        safe(obj, "symptoms"),
                        safe(obj, "type"),
                        obj.has("sub_priority") && !obj.get("sub_priority").isJsonNull()
                                ? obj.get("sub_priority").getAsInt() : 0,
                        safe(obj, "status"),
                        safe(obj, "track"),
                        safe(obj, "HospitalAssigned")
                );

                e.timestamp = obj.has("timestamp") && !obj.get("timestamp").isJsonNull()
                        ? obj.get("timestamp").getAsLong()
                        : 0;

                queue.add(e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return queue;
    }

    public void dispatchEmergency(Emergency e) {
        try {
            String status = "assigned";
            String HospitalNameandBranch = SessionManager.getHospitalName() + " "+ SessionManager.getHospitalBranch();
            String url = BASE_URL + "emergencies?id=eq." + e.id;
            String json = String.format("{\"status\":\"%s\",\"HospitalAssigned\":\"%s\"}", status, HospitalNameandBranch);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ---------------- User authentication ----------------

    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public boolean signup(String Name, String Branch, String ID, String password) {
        try {
            String passwordHash = hashPassword(password);
            String url = BASE_URL + "users";
            String json = String.format("{\"id\":\"%s\",\"name\":\"%s\",\"password_hash\":\"%s\",\"Branch\":\"%s\"}", ID, Name, passwordHash, Branch);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String ID, String password) {
        try {
            String passwordHash = hashPassword(password);
            String url = BASE_URL + "users?id=eq." + ID + "&password_hash=eq." + passwordHash;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body().length() > 2;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //Send Assigned notification to user
    public Emergency fetchEmergencyByTrack(String track) {
        try {
            String url = BASE_URL + "emergencies?track=eq." + track; // filter by track

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray array = new JSONArray(response.body());
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

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void MarkArrived(String track) {
        try {
            String url = BASE_URL + "emergencies?track=eq." + track;
            String json = "{\"status\": \"Arrived\"}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getBranch(String ID) {
        try {
            String url = BASE_URL + "users?id=eq." + ID + "&select=Branch";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray arr = new JSONArray(response.body());
            if (arr.length() > 0) {
                return arr.getJSONObject(0).getString("Branch");
            } else {
                throw new RuntimeException("No hospital found for ID: " + ID);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to fetch branch", ex);
        }
    }

    public List<Map<String, Object>> fetchAllHospitals() {
        List<Map<String, Object>> hospitals = new ArrayList<>();

        try {
                String url = BASE_URL + "users?select=name,Branch";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("apikey", KEY)
                    .header("Authorization", "Bearer " + KEY)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonArray array = JsonParser.parseString(response.body()).getAsJsonArray();

                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();
                    Map<String, Object> hospital = new HashMap<>();

                    // Get Name (handle null/missing)
                    if (obj.has("name") && !obj.get("name").isJsonNull()) {
                        hospital.put("name", obj.get("name").getAsString());
                    } else {
                        hospital.put("name", "Unknown Hospital");
                    }

                    // Get Branch (handle null/missing)
                    if (obj.has("Branch") && !obj.get("Branch").isJsonNull()) {
                        hospital.put("branch", obj.get("Branch").getAsString());
                    } else {
                        hospital.put("branch", "Main Branch");
                    }

                    hospitals.add(hospital);
                }
            } else {
                System.err.println("Error fetching hospitals: " + response.statusCode());
                System.err.println("Response: " + response.body());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch hospitals: " + e.getMessage());
        }

        return hospitals;
    }

    private String safe(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull())
                ? obj.get(key).getAsString()
                : "";
    }
}

