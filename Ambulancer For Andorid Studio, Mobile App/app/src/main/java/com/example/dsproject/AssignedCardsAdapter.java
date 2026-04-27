package com.example.dsproject;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;

public class AssignedCardsAdapter extends RecyclerView.Adapter<AssignedCardsAdapter.ViewHolder> {

    private List<Emergency> emergencies;
    private final MarkArrivedClickListener clickListener;

    public interface MarkArrivedClickListener {
        void onMarkArrived(String track);
    }

    public AssignedCardsAdapter(List<Emergency> emergencies, MarkArrivedClickListener clickListener) {
        this.emergencies = emergencies;
        this.clickListener = clickListener;
    }

    public void updateList(List<Emergency> newList) {
        this.emergencies = newList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assigned_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Emergency emergency = emergencies.get(position);
        holder.bind(emergency, clickListener);
    }

    @Override
    public int getItemCount() {
        return emergencies.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView patientText, emergencyText,symtomsText, trackText, hospitalText, statusText;
        Button markArrivedButton;

        ViewHolder(View itemView) {
            super(itemView);
            patientText = itemView.findViewById(R.id.patient_text);
            emergencyText = itemView.findViewById(R.id.emergency_text);
            symtomsText = itemView.findViewById((R.id.symptoms_text));
            trackText = itemView.findViewById(R.id.track_text);
            hospitalText = itemView.findViewById(R.id.hospital_text);
            statusText = itemView.findViewById(R.id.status_text);
            markArrivedButton = itemView.findViewById(R.id.mark_arrived_button);
        }

        void bind(Emergency emergency, MarkArrivedClickListener listener) {
            patientText.setText(emergency.name);
            emergencyText.setText(emergency.type);
            trackText.setText(emergency.track);
            hospitalText.setText(emergency.HospitalAssigned);
            statusText.setText("Ambulance is en route to your location");

            if (listener != null) {
                markArrivedButton.setOnClickListener(v -> listener.onMarkArrived(emergency.track));

                // Button animation
                markArrivedButton.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case android.view.MotionEvent.ACTION_DOWN:
                            scaleView(v, 0.95f);
                            break;
                        case android.view.MotionEvent.ACTION_UP:
                        case android.view.MotionEvent.ACTION_CANCEL:
                            scaleView(v, 1.0f);
                            break;
                    }
                    return false;
                });
            }
        }


        private void scaleView(View view, float scale) {
            view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .setDuration(100)
                    .start();
        }
    }
    public void removeTrack(String track) {
        for (int i = 0; i < emergencies.size(); i++) {
            if (emergencies.get(i).track.equals(track)) {
                emergencies.remove(i);
                notifyItemRemoved(i);
                notifyItemRangeChanged(i, emergencies.size()); // Keeps RecyclerView in sync
                break;
            }
        }
    }
    public void addItem(Emergency emergency) {
        emergencies.add(emergency);
        notifyItemInserted(emergencies.size() - 1);
    }

}