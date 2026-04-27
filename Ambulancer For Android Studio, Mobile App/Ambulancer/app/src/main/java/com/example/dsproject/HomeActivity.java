// HomeActivity.java
package com.example.dsproject;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.card.MaterialCardView;


public class HomeActivity extends AppCompatActivity {

    private MaterialCardView callButton, viewButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize views - CORRECT IDs
        callButton = findViewById(R.id.call_button);
        viewButton = findViewById(R.id.view_button);

        setupHoverAnimations();
        setupClickListeners();
    }

    private void setupHoverAnimations() {
        // Setup for CardViews
        View.OnTouchListener touchListener = (v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    scaleView(v, 1.05f);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                    scaleView(v, 1.0f);
                    v.performClick();
                    break;
                case android.view.MotionEvent.ACTION_CANCEL:
                    scaleView(v, 1.0f);
                    break;
            }
            return true;
        };

        // Apply to card buttons
        callButton.setOnTouchListener(touchListener);
        viewButton.setOnTouchListener(touchListener);

    }

    private void scaleView(View view, float scale) {
        view.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(200)
                .start();
    }

    private void setupClickListeners() {
        callButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, EmergencyActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HospitalListActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

    }
}