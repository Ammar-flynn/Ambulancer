package com.example.dsproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EmergencyActivity extends AppCompatActivity {

    // UI Components
    private EditText nameField, addressField, symptomsField;
    private MaterialCardView accidentButton, symptomsButton, fahadButton;
    private RelativeLayout overlayLayout;
    private MaterialCardView formLayout;
    private RecyclerView assignedCardsRecyclerView;
    private ImageView backButton;  // CHANGED: From Button to ImageView

    // Adapters and Services
    private AssignedCardsAdapter assignedCardsAdapter;
    private SupabaseService supabaseService;
    private TrackManager trackManager;

    // Data
    private Set<String> notifiedTracks = new HashSet<>();
    private Handler notificationHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // Initialize services
        supabaseService = new SupabaseService(this);
        trackManager = new TrackManager(this);

        // Setup UI
        initializeViews();
        setupRecyclerView();
        setupAnimations();
        startNotificationPolling();
    }

    private void initializeViews() {
        nameField = findViewById(R.id.name_field);
        addressField = findViewById(R.id.address_field);
        symptomsField = findViewById(R.id.symptoms_field);

        // Emergency type buttons
        accidentButton = findViewById(R.id.accident_button);
        symptomsButton = findViewById(R.id.symptoms_button);
        fahadButton = findViewById(R.id.fahad_button);

        // Popup overlay and form
        overlayLayout = findViewById(R.id.overlay_layout);
        formLayout = findViewById(R.id.form_layout);

        // RecyclerView for assigned emergencies
        assignedCardsRecyclerView = findViewById(R.id.assigned_cards_recycler);

        // Buttons
        backButton = findViewById(R.id.back_button);  // FIXED: This is ImageView, not Button
        Button closeFormButton = findViewById(R.id.close_form_button);
        Button sendSymptomsButton = findViewById(R.id.send_symptoms_button);
        Button cancelButton = findViewById(R.id.cancel_button);

        // Set click listeners
        accidentButton.setOnClickListener(v -> onAccidentClicked());
        symptomsButton.setOnClickListener(v -> onSymptomsClicked());
        fahadButton.setOnClickListener(v -> onFahadClicked());

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        });

        closeFormButton.setOnClickListener(v -> closeForm());
        sendSymptomsButton.setOnClickListener(v -> sendSymptoms());
        cancelButton.setOnClickListener(v -> closeForm());

        // Close form when overlay background is clicked
        overlayLayout.setOnClickListener(v -> {
            if (v.getId() == R.id.overlay_layout) {
                closeForm();
            }
        });

        // Prevent the card from closing when clicked
        formLayout.setOnClickListener(v -> {
            // Do nothing - prevents closing when clicking inside the form
        });
    }

    private void setupRecyclerView() {
        assignedCardsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        assignedCardsAdapter = new AssignedCardsAdapter(new ArrayList<>(), this::onMarkArrived);
        assignedCardsRecyclerView.setAdapter(assignedCardsAdapter);
    }

    private void setupAnimations() {
        // Setup button hover animations for emergency type buttons
        MaterialCardView[] buttons = {accidentButton, symptomsButton, fahadButton};
        for (MaterialCardView button : buttons) {
            button.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        scaleView(v, 1.05f);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        scaleView(v, 1.0f);
                        v.performClick();
                        break;
                }
                return true;
            });
        }
    }

    private void scaleView(View view, float scale) {
        view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(200)
                .start();
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(nameField.getText().toString().trim()) ||
                TextUtils.isEmpty(addressField.getText().toString().trim())) {
            Toast.makeText(this, "Please fill in both Name and Address!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private int calculateSymptomsPriority(String symptoms) {
        symptoms = symptoms.toLowerCase();
        if (symptoms.contains("heart") || symptoms.contains("brain")) return 3;
        if (symptoms.contains("chest") || symptoms.contains("stroke")) return 2;
        if (symptoms.contains("stomach") || symptoms.contains("headache")) return 1;
        return 0;
    }

    private void onAccidentClicked() {
        if (!validateFields()) return;

        String track = trackManager.generateTrack();
        trackManager.saveTrackLocally(track);

        Emergency emergency = new Emergency(
                null,
                nameField.getText().toString().trim(),
                addressField.getText().toString().trim(),
                null,
                "ACCIDENT",
                0,
                "pending",
                track,
                "none"
        );

        supabaseService.saveEmergency(emergency);
        Toast.makeText(this, "Accident reported successfully! Track ID: " + track, Toast.LENGTH_LONG).show();
        clearFields();
    }

    private void onSymptomsClicked() {
        if (!validateFields()) return;

        // Show symptoms form
        overlayLayout.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        fadeIn.setDuration(300);
        overlayLayout.startAnimation(fadeIn);

        // Clear previous symptoms text
        symptomsField.setText("");
    }

    private void sendSymptoms() {
        String symptomsText = symptomsField.getText().toString().trim();
        if (TextUtils.isEmpty(symptomsText)) {
            Toast.makeText(this, "Please enter symptoms", Toast.LENGTH_SHORT).show();
            return;
        }

        int priority = calculateSymptomsPriority(symptomsText);
        String track = trackManager.generateTrack();
        trackManager.saveTrackLocally(track);

        Emergency emergency = new Emergency(
                null,
                nameField.getText().toString().trim(),
                addressField.getText().toString().trim(),
                symptomsText,
                "SYMPTOMS",
                priority,
                "pending",
                track,
                "none"
        );

        supabaseService.saveEmergency(emergency);
        closeForm();
        Toast.makeText(this, "Symptoms submitted successfully! Track ID: " + track, Toast.LENGTH_LONG).show();
        clearFields();
    }

    private void onFahadClicked() {
        Toast.makeText(this, "Fire emergency feature is under development", Toast.LENGTH_SHORT).show();
    }

    private void closeForm() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOut.setDuration(200);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                overlayLayout.setVisibility(View.GONE);
                symptomsField.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        overlayLayout.startAnimation(fadeOut);
    }

    private void clearFields() {
        nameField.setText("");
        addressField.setText("");
        symptomsField.setText("");
    }

    private void startNotificationPolling() {
        Runnable notificationRunnable = new Runnable() {
            @Override
            public void run() {
                checkForAssignedEmergencies();
                notificationHandler.postDelayed(this, 5000); // Check every 5 seconds
            }
        };
        notificationHandler.postDelayed(notificationRunnable, 5000);
    }

    private void checkForAssignedEmergencies() {
        new Thread(() -> {
            List<String> activeTracks = trackManager.loadActiveTracks();
            if (activeTracks.isEmpty()) return;

            List<Emergency> newAssignedEmergencies = new ArrayList<>();

            for (String track : activeTracks) {
                // Skip already notified emergencies
                if (notifiedTracks.contains(track)) continue;

                Emergency e = supabaseService.fetchEmergencyByTrack(track);
                if (e != null && "assigned".equalsIgnoreCase(e.status)) {
                    // Add to new emergencies list
                    newAssignedEmergencies.add(e);
                    // Mark this track as notified
                    notifiedTracks.add(track);
                }
            }

            if (!newAssignedEmergencies.isEmpty()) {
                runOnUiThread(() -> {
                    // Add only the new emergencies to the adapter
                    for (Emergency em : newAssignedEmergencies) {
                        assignedCardsAdapter.addItem(em);  // <-- addItem method
                    }

                    // Show RecyclerView if hidden
                    if (assignedCardsAdapter.getItemCount() > 0) {
                        findViewById(R.id.placeholder_box).setVisibility(View.GONE);
                        assignedCardsRecyclerView.setVisibility(View.VISIBLE);
                    }

                    // Show toast notifications for the new emergencies
                    for (Emergency em : newAssignedEmergencies) {
                        Toast.makeText(EmergencyActivity.this,
                                "Ambulance assigned for " + em.name + " (" + em.type + ")\nTrack: " + em.track,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }


    private void onMarkArrived(String track) {
        new Thread(() -> {
            try {
                supabaseService.MarkArrived(track);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Marked as arrived for track: " + track, Toast.LENGTH_SHORT).show();

                    // Remove from notified tracks
                    notifiedTracks.remove(track);
                    assignedCardsAdapter.removeTrack(track);

                    //show placeholder if list is empty
                    if (assignedCardsAdapter.getItemCount() == 0) {
                        findViewById(R.id.placeholder_box).setVisibility(View.VISIBLE);
                        assignedCardsRecyclerView.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Failed to mark as arrived: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Check for updates when activity resumes
        checkForAssignedEmergencies();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler to prevent memory leaks
        notificationHandler.removeCallbacksAndMessages(null);
    }
}