package com.inhatc.petcare.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inhatc.petcare.R;
import com.inhatc.petcare.adapter.BreedAdapter;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BreedSelectionDialogFragment extends DialogFragment implements BreedAdapter.OnBreedSelectedListener {

    public interface OnBreedSelectedListener {
        void onBreedSelected(String breed);
    }

    private OnBreedSelectedListener listener;

    private EditText searchEditText;
    private RecyclerView popularBreedsRecyclerView;
    private RecyclerView allBreedsRecyclerView;

    private BreedAdapter popularBreedAdapter;
    private BreedAdapter allBreedsAdapter;

    private List<String> originalPopularBreeds; // 원본 인기 견종 목록
    private List<String> originalAllBreeds;     // 원본 전체 견종 목록

    private List<String> currentPopularBreeds;  // 현재 표시될 인기 견종 목록
    private List<String> currentAllBreeds;      // 현재 표시될 전체 견종 목록

    public void setOnBreedSelectedListener(OnBreedSelectedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_breed_selection, container, false);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        searchEditText = view.findViewById(R.id.searchEditText);
        popularBreedsRecyclerView = view.findViewById(R.id.popularBreedsRecyclerView);
        allBreedsRecyclerView = view.findViewById(R.id.allBreedsRecyclerView);

        initBreedData();
        setupPopularBreedsRecyclerView();
        setupAllBreedsRecyclerView();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBreeds(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        return view;
    }

    private void initBreedData() {
        // 인기 견종
        originalPopularBreeds = Arrays.asList(
                "말티즈", "푸들", "포메라니안", "치와와", "시츄", "비숑 프리제", "골든 리트리버",
                "진돗개", "웰시 코기 (펨브룩)", "요크셔 테리어", "닥스훈트", "프렌치 불도그",
                "래브라도 리트리버", "시바 이누", "스피츠 (재패니즈)",
                "믹스견", "기타", "모름"
        );
        currentPopularBreeds = new ArrayList<>(originalPopularBreeds);

        // 전체 견종 (그룹 제거 → 이름만 리스트)
        originalAllBreeds = Arrays.asList(
                "보더 콜리", "셰틀랜드 쉽독", "웰시 코기 (펨브룩)", "웰시 코기 (카디건)", "저먼 셰퍼드 독", "오스트레일리안 셰퍼드",
                "도베르만", "미니어처 슈나우저", "로트와일러", "버니즈 마운틴 독", "복서", "케인 코르소",
                "요크셔 테리어", "잭 러셀 테리어", "불테리어", "화이트 테리어 (웨스트 하이랜드)", "베들링턴 테리어",
                "닥스훈트 (스탠다드)", "닥스훈트 (미니어처)", "닥스훈트 (카닌헨)", "닥스훈트 (스무스)", "닥스훈트 (롱)", "닥스훈트 (와이어)",
                "포메라니안", "진돗개", "시바 이누", "사모예드", "시베리안 허스키", "재패니즈 스피츠", "알래스칸 맬러뮤트",
                "비글", "바셋 하운드", "달마시안",
                "아이리시 세터", "잉글리시 포인터",
                "골든 리트리버", "래브라도 리트리버", "코카 스패니얼 (아메리칸)", "코카 스패니얼 (잉글리시)",
                "말티즈", "푸들 (토이)", "푸들 (미니어처)", "푸들 (미디엄)", "푸들 (스탠다드)", "치와와", "시츄", "비숑 프리제", "파피용", "퍼그", "프렌치 불도그",
                "이탈리안 그레이하운드", "휘핏", "아프간 하운드"
        );
        currentAllBreeds = new ArrayList<>(originalAllBreeds);
    }

    private void setupPopularBreedsRecyclerView() {
        popularBreedsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        popularBreedAdapter = new BreedAdapter(currentPopularBreeds, this);
        popularBreedsRecyclerView.setAdapter(popularBreedAdapter);
    }

    private void setupAllBreedsRecyclerView() {
        allBreedsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        allBreedsAdapter = new BreedAdapter(currentAllBreeds, this);
        allBreedsRecyclerView.setAdapter(allBreedsAdapter);
    }

    private void filterBreeds(String query) {
        currentPopularBreeds.clear();
        currentAllBreeds.clear();

        if (query.isEmpty()) {
            currentPopularBreeds.addAll(originalPopularBreeds);
            currentAllBreeds.addAll(originalAllBreeds);
        } else {
            String lowerCaseQuery = query.toLowerCase();

            // 인기 견종 필터링
            for (String breed : originalPopularBreeds) {
                if (breed.toLowerCase().contains(lowerCaseQuery)) {
                    currentPopularBreeds.add(breed);
                }
            }

            // 전체 견종 필터링
            currentAllBreeds.addAll(originalAllBreeds.stream()
                    .filter(breed -> breed.toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList()));
        }

        popularBreedAdapter.notifyDataSetChanged();
        allBreedsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBreedSelected(String breed) {
        if (breed.equals("기타")) {
            showOtherBreedInputDialog();
        } else {
            if (listener != null) {
                listener.onBreedSelected(breed);
            }
            dismiss();
        }
    }

    private void showOtherBreedInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("견종 직접 입력");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("견종 이름을 입력하세요");
        builder.setView(input);

        builder.setPositiveButton("확인", (dialog, which) -> {
            String customBreed = input.getText().toString().trim();
            if (!customBreed.isEmpty()) {
                if (listener != null) {
                    listener.onBreedSelected(customBreed);
                }
                dismiss();
            } else {
                Toast.makeText(requireContext(), "견종 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
