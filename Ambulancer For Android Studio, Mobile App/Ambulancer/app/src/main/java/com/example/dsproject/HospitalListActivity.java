// HospitalListActivity.java
package com.example.dsproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class HospitalListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HospitalAdapter adapter;
    private EditText searchField;
    private ProgressBar loadingIndicator;
    private TextView resultsCountLabel, lastUpdatedLabel;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Button emergencyButton;
    private ImageView backButton;
    private List<Hospital> hospitals = new ArrayList<>();
    private SupabaseService supabaseService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_list);

        supabaseService = new SupabaseService(this);
        initializeViews();
        setupRecyclerView();
        setupSearch();
        loadHospitals();

        // Setup auto-refresh
        Handler handler = new Handler();
        Runnable refreshRunnable = new Runnable() {
            @Override
            public void run() {
                refreshHospitalData();
                handler.postDelayed(this, 30000); // 30 seconds
            }
        };
        handler.postDelayed(refreshRunnable, 30000);
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.hospitals_recycler_view);
        searchField = findViewById(R.id.search_field);
        loadingIndicator = findViewById(R.id.loading_indicator);
        resultsCountLabel = findViewById(R.id.results_count_label);
        lastUpdatedLabel = findViewById(R.id.last_updated_label);
        backButton = findViewById(R.id.back_button);
        emergencyButton = findViewById(R.id.emergency_button);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshHospitalData);

        backButton.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        emergencyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EmergencyActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HospitalAdapter(new ArrayList<>(), null);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
    }

    private void setupSearch() {
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchHospitals(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadHospitals() {
        loadingIndicator.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                List<Hospital> fetchedHospitals = supabaseService.fetchAllHospitals();
                runOnUiThread(() -> {
                    hospitals = fetchedHospitals;
                    displayHospitals(fetchedHospitals);
                    loadingIndicator.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    loadingIndicator.setVisibility(View.GONE);
                    showNoResults();
                    Toast.makeText(this, "Failed to load hospitals", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void displayHospitals(List<Hospital> list) {
        if (list.isEmpty()) {
            showNoResults();
            return;
        }

        adapter.updateList(list);
        if(list.size() == 1) resultsCountLabel.setText("📊 " + list.size() + " Hospital Found");
        else resultsCountLabel.setText("📊 " + list.size() + " Hospitals Found");
        updateLastUpdatedTime();

        // Animate results count
        resultsCountLabel.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(200)
                .withEndAction(() -> resultsCountLabel.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start())
                .start();
    }

    private void searchHospitals(String term) {
        if (term.isEmpty()) {
            displayHospitals(hospitals);
            return;
        }

        List<Hospital> filtered = new ArrayList<>();
        for (Hospital h : hospitals) {
            if (h.getName().toLowerCase().contains(term.toLowerCase()) ||
                    h.getBranch().toLowerCase().contains(term.toLowerCase())) {
                filtered.add(h);
            }
        }
        adapter.updateList(filtered);
        resultsCountLabel.setText("📊 " + filtered.size() + " Hospitals Found");
    }

    private void refreshHospitalData() {
        new Thread(() -> {
            try {
                List<Hospital> fetchedHospitals = supabaseService.fetchAllHospitals();
                runOnUiThread(() -> {
                    hospitals = fetchedHospitals;
                    if (searchField.getText().toString().isEmpty()) {
                        displayHospitals(hospitals);
                    } else {
                        searchHospitals(searchField.getText().toString());
                    }

                    // Flash animation
                    lastUpdatedLabel.animate()
                            .alpha(0.3f)
                            .setDuration(150)
                            .withEndAction(() -> lastUpdatedLabel.animate()
                                    .alpha(1f)
                                    .setDuration(150)
                                    .start())
                            .start();

                    swipeRefreshLayout.setRefreshing(false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Refresh failed", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateLastUpdatedTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault());
        String time = sdf.format(new java.util.Date());
        lastUpdatedLabel.setText("Last updated: " + time);
    }

    private void showNoResults() {
        // Implement empty state view
        adapter.updateList(new ArrayList<>());
        resultsCountLabel.setText("0 Hospitals Found");
    }


}