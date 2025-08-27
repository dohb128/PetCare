package com.inhatc.petcare.adapter;

import com.inhatc.petcare.R;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.petcare.model.Pet;

import java.util.List;
import java.util.Locale;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import de.hdodenhof.circleimageview.CircleImageView;

public class PetAdapter extends RecyclerView.Adapter<PetAdapter.PetViewHolder> {

    private List<Pet> petList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public PetAdapter(List<Pet> petList) {
        this.petList = petList;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registered_pet, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet currentPet = petList.get(position);
        holder.petItemNameTextView.setText(currentPet.getName());
        holder.petItemDetailsTextView.setText(String.format(Locale.getDefault(), "나이: %d세, 체중: %.1fkg", currentPet.getAge(), currentPet.getWeight()));

        String photoURL = currentPet.getPhotoURL();
        if (photoURL != null && !photoURL.isEmpty()) {
            // photoURL이 Base64 문자열인지 확인하고 디코딩하여 이미지 설정
            try {
                byte[] decodedString = Base64.decode(photoURL, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.petItemImageView.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                // Base64 디코딩 실패 시 (일반 URL일 경우)
                holder.petItemImageView.setImageResource(R.drawable.pet);
                Log.e("PetAdapter", "Base64 string is invalid. Using default image.", e);
            }
        } else {
            holder.petItemImageView.setImageResource(R.drawable.pet); // 기본 이미지
        }

        holder.petItemEditButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(position);
            }
        });

        holder.petItemDeleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return petList.size();
    }

    public void addPet(Pet pet) {
        petList.add(pet);
        notifyItemInserted(petList.size() - 1);
    }

    public void removePet(int position) {
        petList.remove(position);
        notifyItemRemoved(position);
    }

    public static class PetViewHolder extends RecyclerView.ViewHolder {
        // ImageView를 CircleImageView로 변경
        CircleImageView petItemImageView;
        TextView petItemNameTextView;
        TextView petItemDetailsTextView;
        ImageButton petItemEditButton;
        ImageButton petItemDeleteButton;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            petItemImageView = itemView.findViewById(R.id.petItemImageView);
            petItemNameTextView = itemView.findViewById(R.id.petItemNameTextView);
            petItemDetailsTextView = itemView.findViewById(R.id.petItemDetailsTextView);
            petItemEditButton = itemView.findViewById(R.id.petItemEditButton);
            petItemDeleteButton = itemView.findViewById(R.id.petItemDeleteButton);
        }
    }
}