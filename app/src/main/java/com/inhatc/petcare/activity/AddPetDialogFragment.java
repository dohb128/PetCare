package com.inhatc.petcare.activity;

import com.inhatc.petcare.R;
import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import com.inhatc.petcare.model.Pet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddPetDialogFragment extends DialogFragment implements DecimalNumberPickerDialog.OnDecimalNumberSetListener {

    public interface OnPetAddedListener {
        void onPetAdded(Pet pet);
    }

    private OnPetAddedListener listener;

    private static final int GALLERY_PERMISSION_REQUEST_CODE = 200;

    private ImageView petImageView;
    private TextInputEditText editTextPetName;
    private TextInputEditText editTextPetBirthday;
    private TextInputEditText editTextPetWeight;
    private Button buttonCancel;
    private Button buttonSave;

    private Uri selectedImageUri;

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), selectedImageUri);
                        petImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "이미지를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    public void setOnPetAddedListener(OnPetAddedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_add_pet, container, false);

        petImageView = view.findViewById(R.id.petImageView);
        editTextPetName = view.findViewById(R.id.editTextPetName);
        editTextPetBirthday = view.findViewById(R.id.editTextPetBirthday);
        editTextPetWeight = view.findViewById(R.id.editTextPetWeight);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSave = view.findViewById(R.id.buttonSave);

        petImageView.setOnClickListener(v -> checkGalleryPermissionAndOpenGallery());

        editTextPetBirthday.setOnClickListener(v -> showDatePickerDialog());

        editTextPetWeight.setOnClickListener(v -> showDecimalNumberPickerDialog());

        buttonCancel.setOnClickListener(v -> dismiss());

        buttonSave.setOnClickListener(v -> savePet());

        return view;
    }

    private void checkGalleryPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, GALLERY_PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void openGallery() {
        Intent pickGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(pickGalleryIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(requireContext(), "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    editTextPetBirthday.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showDecimalNumberPickerDialog() {
        double currentValue = 0.0;
        try {
            currentValue = Double.parseDouble(editTextPetWeight.getText().toString());
        } catch (NumberFormatException e) {
            // Default to 0.0 if parsing fails
        }
        DecimalNumberPickerDialog dialog = DecimalNumberPickerDialog.newInstance(currentValue);
        dialog.setOnDecimalNumberSetListener(this);
        dialog.show(getParentFragmentManager(), "DecimalNumberPickerDialog");
    }

    @Override
    public void onDecimalNumberSet(double number) {
        editTextPetWeight.setText(String.format(Locale.getDefault(), "%.1f", number));
    }

    private void savePet() {
        String name = editTextPetName.getText().toString().trim();
        String birthday = editTextPetBirthday.getText().toString().trim();
        String weightStr = editTextPetWeight.getText().toString().trim();

        if (name.isEmpty() || birthday.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(requireContext(), "모든 필드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        double weight = Double.parseDouble(weightStr);

        // Calculate age from birthday
        int age = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthDate = sdf.parse(birthday);
            Calendar dob = Calendar.getInstance();
            dob.setTime(birthDate);
            Calendar today = Calendar.getInstance();
            age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "생년월일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get ownerId
        String ownerId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (ownerId == null) {
            Toast.makeText(requireContext(), "로그인된 사용자를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert Uri to String photoURL (for now, just set to null or a placeholder)
        // In a real application, you would upload selectedImageUri to Firebase Storage
        // and get the download URL here.
        String photoURL = selectedImageUri != null ? selectedImageUri.toString() : null; // Placeholder

        Pet newPet = new Pet(ownerId, name, photoURL, age, weight, birthday);
        if (listener != null) {
            listener.onPetAdded(newPet);
        }
        dismiss();
    }
}