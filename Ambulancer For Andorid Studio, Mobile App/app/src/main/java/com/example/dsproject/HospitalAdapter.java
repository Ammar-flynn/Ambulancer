// HospitalAdapter.java
package com.example.dsproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {

    private List<Hospital> hospitals;
    private final HospitalClickListener clickListener;

    public interface HospitalClickListener {
        void onHospitalClick(Hospital hospital, int actionType);
    }

    public HospitalAdapter(List<Hospital> hospitals, HospitalClickListener clickListener) {
        this.hospitals = hospitals;
        this.clickListener = clickListener;
    }

    public void updateList(List<Hospital> newList) {
        this.hospitals = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hospital_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hospital hospital = hospitals.get(position);
        holder.bind(hospital, clickListener);

        // Staggered animation
        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(),
                android.R.anim.fade_in);
        animation.setDuration(300);
        animation.setStartOffset(position * 100L);
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return hospitals.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView hospitalName;
        TextView hospitalBranch;
        TextView hospitalStatus;
        Button viewButton, callButton, emergencyButton;

        ViewHolder(View itemView) {
            super(itemView);
            hospitalName = itemView.findViewById(R.id.hospital_name);
            hospitalBranch = itemView.findViewById(R.id.hospital_branch);
            hospitalStatus = itemView.findViewById(R.id.hospital_status);
            viewButton = itemView.findViewById(R.id.view_button);
            callButton = itemView.findViewById(R.id.call_button);
            emergencyButton = itemView.findViewById(R.id.emergency_button);
        }

        void bind(Hospital hospital, HospitalClickListener listener) {
            hospitalName.setText(hospital.getName());
            hospitalBranch.setText("📍 " + hospital.getBranch());
            hospitalStatus.setText("● AVAILABLE");


            itemView.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        scaleView(itemView, 1.02f);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        scaleView(itemView, 1.0f);
                        if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                            v.performClick();
                        }
                        break;
                }
                return true;
            });
        }

        private void setupButtonAnimation(Button button) {
            button.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        scaleView(v, 1.05f);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        scaleView(v, 1.0f);
                        break;
                }
                return false;
            });
        }

        private void scaleView(View view, float scale) {
            view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(150)
                    .start();
        }
    }
}