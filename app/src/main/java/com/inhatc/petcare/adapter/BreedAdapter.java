package com.inhatc.petcare.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.petcare.R;

import java.util.List;

public class BreedAdapter extends RecyclerView.Adapter<BreedAdapter.BreedViewHolder> {

    public interface OnBreedSelectedListener {
        void onBreedSelected(String breed);
    }

    private List<String> breeds;
    private OnBreedSelectedListener listener;

    public BreedAdapter(List<String> breeds, OnBreedSelectedListener listener) {
        this.breeds = breeds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BreedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_breed_chip, parent, false);
        return new BreedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BreedViewHolder holder, int position) {
        String breed = breeds.get(position);
        holder.breedNameTextView.setText(breed);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBreedSelected(breed);
            }
        });
    }

    @Override
    public int getItemCount() {
        return breeds.size();
    }

    static class BreedViewHolder extends RecyclerView.ViewHolder {
        TextView breedNameTextView;

        public BreedViewHolder(@NonNull View itemView) {
            super(itemView);
            breedNameTextView = itemView.findViewById(R.id.breedNameTextView);
        }
    }
}