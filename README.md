# 🐾 PetCare — 반려동물 건강 관리 솔루션  
AI 기반 눈·피부 질환 검사, 의료 기록 관리, 챗봇 상담, 병원 찾기까지 제공하는 **종합 반려동물 헬스케어 앱**입니다.

<img width="500" src="https://github.com/user-attachments/assets/c7d4bfc1-85d4-4a60-a511-017db42a22a1" />

---

## 📌 프로젝트 정보
- **프로젝트 이름**: PetCare  
- **개발 기간**: 2025.03 ~ 2025.12  
- **앱 다운로드**: https://drive.google.com/file/d/1-BWLjTZAXh5YIi9B-rsrIhH1KU514O-Z/view?usp=sharing  
- **GitHub (Android App)**: https://github.com/dohb128/PetCare  
- **GitHub (AI Model)**: https://github.com/SW  ](https://github.com/dohb128/SW2025
- **개발자 소개**: https://github.com/dohb128  

---

## ✨ 프로젝트 개요

PetCare는 반려동물 양육가구 증가, 높은 진료비, 건강 정보 부족 문제를 해결하기 위해 개발된  
**AI 기반 반려동물 건강 관리 애플리케이션**입니다.

주요 기능:
- 반려동물 눈 질환 검사 (AI)
- 반려동물 피부 질환 검사 (AI)
- 반려동물 정보·의료 기록 관리
- 챗봇 상담 (OpenAI API)
- 주변 동물병원 검색 (Google Maps)
- 사용자 프로필 관리

---

# 🚀 주요 기능 

## 👁️ 눈 건강 검사
- 카메라/갤러리 이미지 선택 → 서버 전송  
- MobileNetV2 기반 **4-Class AI 모델**  
  (결막염 / 백내장 / 종양 / 무증상)
- 정확도 **84%**, 응답 속도 **1~2초**
- 질환명 + 확률 표시

## 🐶 피부 질환 검사
- 두 모델(P 그룹 / I 그룹) 기반 3-Class 분류  
- 실시간 결과 제공  
- 약 **70% 정확도**

## 🧬 반려동물 관리
- 기본 정보 등록/수정/삭제  
- 여러 마리 동시 관리  
- Firebase로 실시간 동기화

## 💬 AI 챗봇 상담
- OpenAI API 연동  
- 반려동물 정보 기반 개인화 응답  
- 건강/사료/관리 관련 질문 처리

## 📖 의료 기록 관리
- 접종/진료/투약 기록 관리  
- 최근 진단 기록 자동 저장

## 🏥 주변 병원 찾기
- Google Maps SDK 기반  
- 현재 위치 기준 병원 마커 표시  
- 병원 정보(주소/전화번호) 제공

---

# 🧱 시스템 아키텍처 

```
Android App (Java)
│
HTTPS API
│
Flask Server (AWS EC2)
│
AI Model (MobileNetV2 - Eye/Skin)
```

- App → Server: 이미지 업로드  
- Server → AI Model: 예측 수행  
- 결과를 JSON으로 반환  
- Firebase / OpenAI / Google Maps와 통합 운영

---

# 🧠 AI 모델 (핵심)

### ✔ 공통 구조
- MobileNetV2 기반 Transfer Learning  
- Custom Classifier Layer 덧붙여 Fine-tuning  
- AI Hub 반려동물 이미지 데이터(30만장) 활용  
- Augmentation + 전처리 포함  

---

# 👁️ 안구 질환 모델 (Eye Model)

### ✔ 성능 요약
| Class | Precision | Recall | F1 | Support |
|-------|-----------|--------|-----|----------|
| 결막염 | 0.89 | 0.77 | 0.83 | 239 |
| 백내장 | 0.80 | 0.71 | 0.76 | 168 |
| 안검종양 | 0.90 | 0.90 | 0.90 | 114 |
| 무증상 | 0.77 | 1.00 | 0.87 | 172 |
| **Accuracy** | | | **0.84** | **693** |

### ✔ 최종 테스트
- 테스트 이미지: 13  
- 정확도: **84.62% (11/13)**  
- 실서비스 환경에서도 안정적인 성능

---

# 🐶 피부 질환 모델

## 🔥 A. Protruding_P (돌출형)

| Class | Precision | Recall | F1 | Support |
|--------|----------|--------|-----|----------|
| 구진/플라그 | 0.729 | 0.693 | 0.710 | 450 |
| 태선화/색소침착 | 0.684 | 0.626 | 0.654 | 450 |
| 결절/종괴 | 0.671 | 0.773 | 0.719 | 397 |
| **Accuracy** | | | **0.6947** | **1297** |

---

## 🔥 B. Inflammatory_I (염증형)

| Class | Precision | Recall | F1 | Support |
|--------|----------|--------|-----|----------|
| 농포/여드름 | 0.723 | 0.655 | 0.687 | 296 |
| 미란/궤양 | 0.515 | 0.666 | 0.581 | 150 |
| 결절/종괴 | 0.739 | 0.694 | 0.716 | 265 |
| **Accuracy** | | | **0.6723** | **711** |

---

# 🧪 성능 요약

| 모델 | 정확도 | 특징 |
|------|---------|--------|
| 눈 질환 모델 | **84%** | 4-Class, 안정적 성능 |
| 피부 질환(P) | **69%** | 돌출형 병변 |
| 피부 질환(I) | **67%** | 염증형 병변 |
| 서버 응답 속도 | **1~2초** | 실시간 inference 가능 |

---

# 🔧 기술 스택

### Mobile
- Android (Java)  
- Android Studio  
- Firebase (Auth, Realtime DB, Firestore, Storage)  
- OkHttp  
- Material Design  

### Backend & AI
- Python (Flask API)  
- TensorFlow / scikit-learn  
- AWS EC2  
- AI Hub Dataset  
- MobileNetV2  

### External Services
- OpenAI API  
- Google Maps SDK  
- Google Places & GPS  

---

# ▶ 시작 가이드

## 요구사항
- JDK 11+  
- Android SDK 26+  
- Gradle 8.11.1 (Wrapper 사용)  

## 설치 및 실행
링크 접속 후 다운로드
https://drive.google.com/file/d/1-BWLjTZAXh5YIi9B-rsrIhH1KU514O-Z/view?usp=sharing  

## 📌 프로젝트 구성 요약 (WBS 기반)

- 기획 및 요구사항 분석
- AI 모델 설계 및 학습(Eye/Skin)
- Android 앱 개발
- 서버 구축(AWS EC2, Flask)
- 통합 테스트 및 배포

---

## 🎯 프로젝트 성과 & 향후 계획

### ✔ 성과

- 자체 학습한 Eye/Skin AI 모델 앱 서비스 적용
- Firebase 기반 데이터/사용자 관리
- ChatGPT 기반 반려동물 상담 기능 구현
- EC2 서버에서 안정적인 서비스 제공

### ✔ 향후 계획
- 품종/나이 기반 맞춤형 리포트
- 반려동물 커뮤니티 기능
- 건강 맞춤 상품 추천 기능
