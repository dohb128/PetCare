package com.inhatc.petcare.activity;

import com.inhatc.petcare.R;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class DecimalNumberPickerDialog extends DialogFragment {

    public interface OnDecimalNumberSetListener {
        void onDecimalNumberSet(double number);
    }

    private OnDecimalNumberSetListener listener;
    private double initialValue;

    public static DecimalNumberPickerDialog newInstance(double initialValue) {
        DecimalNumberPickerDialog fragment = new DecimalNumberPickerDialog();
        Bundle args = new Bundle();
        args.putDouble("initialValue", initialValue);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnDecimalNumberSetListener(OnDecimalNumberSetListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_number_picker_decimal, null);

        NumberPicker numberPickerInteger = view.findViewById(R.id.numberPickerInteger);
        NumberPicker numberPickerDecimal = view.findViewById(R.id.numberPickerDecimal);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonOk = view.findViewById(R.id.buttonOk);

        if (getArguments() != null) {
            initialValue = getArguments().getDouble("initialValue", 0.0);
        }

        int initialInteger = (int) initialValue;
        int initialDecimal = (int) ((initialValue - initialInteger) * 10);

        numberPickerInteger.setMinValue(0);
        numberPickerInteger.setMaxValue(200); // Max weight, adjust as needed
        numberPickerInteger.setValue(initialInteger);

        numberPickerDecimal.setMinValue(0);
        numberPickerDecimal.setMaxValue(9);
        numberPickerDecimal.setValue(initialDecimal);

        buttonCancel.setOnClickListener(v -> dismiss());

        buttonOk.setOnClickListener(v -> {
            double selectedNumber = numberPickerInteger.getValue() + (double) numberPickerDecimal.getValue() / 10;
            if (listener != null) {
                listener.onDecimalNumberSet(selectedNumber);
            }
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }
}