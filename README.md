# 프로젝트 정보
## **PetCare**
- **프로젝트 로고**  ![Image](https://github.com/user-attachments/assets/c7d4bfc1-85d4-4a60-a511-017db42a22a1)
- **프로젝트 소개**: 반려동물 눈 건강 관리, 의료 기록, 챗봇 상담, 주변 병원 찾기 등의 기능을 제공하는 안드로이드 애플리케이션입니다.
- **배포 주소**: https://drive.google.com/file/d/1bXfgTFeo-7eIJXqsn5oxIt7vRuCF_fdu/view?usp=sharing
- **개발 기간**: 2025.03~
- **개발자 소개**: https://github.com/dohb128



# 시작 가이드

## 요구사항
- Java Development Kit (JDK) 11 이상
- Android SDK (API Level 26 이상)
- Gradle 8.11.1 (프로젝트에 포함된 Gradle Wrapper 사용 권장)

## 설치 및 실행
```bash
# 1. 프로젝트 클론
git clone [프로젝트 깃 주소]

# 2. 프로젝트 디렉토리로 이동
cd SW2025

# 3. 필요한 경우, Google Maps API 키 설정
# app/src/main/assets/api.env 파일에 GOOGLE_MAPS_API_KEY를 추가합니다.
# 예시: GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY

# 4. 프로젝트 빌드
./gradlew build

# 5. Android Studio에서 프로젝트 열기 및 실행
# Android Studio를 사용하여 프로젝트를 열고 에뮬레이터 또는 실제 기기에서 실행합니다.
```

# 기술 스택

- **모바일 개발**: Android (Java)
- **백엔드/데이터베이스**: Google Firebase (Authentication, Realtime Database, Firestore, Storage)
- **네트워크 통신**: OkHttp
- **지도/위치 서비스**: Google Maps SDK, Google Play Services Location, Google Places API
- **챗봇**: OpenAI API 연동
- **UI/UX**: Material Design, CircleImageView 라이브러리

# 시스템 아키텍처

![Image](https://github.com/user-attachments/assets/9909d4a9-648f-46ab-92b6-f81a97722727)

# 주요 기능

- **반려동물 관리**: 반려동물 정보 등록 및 관리
  - 마이페이지에서 프로필 및 닉네임 수정
  - ‘추가’ 버튼으로 반려동물 등록
  - 등록된 반려동물 수정 및 삭제 가능
  - 사용자 및 반려동물의 정보는 데이터베이스에 저장
![Image](https://github.com/user-attachments/assets/e21c19e2-7434-4796-91b8-0fe9fd0d9bef)

- **챗봇 상담**: OpenAI API를 활용한 반려동물 관련 챗봇 상담
![Image](https://github.com/user-attachments/assets/7836b9b4-1589-416e-8605-3ffabdc8906b)
  - OpenAI API를 활용해 챗봇 기능 구현
  - “당신은 반려동물 건강 전문가입니다. 반려동물의 질병, 증상, 예방접종, 영양 등에 대해 전문적이고 친근하게 답변해주세요. 항상 한국어로 답변하고, 필요시 수의사 상담을 권장하세요.“ 프롬포트 설정
  - Database에 접근하여, 반려동물의 이름, 나이, 체중, 진료기록 등을 참조해 개인화 함
  - 사용자가 질문 시 AI에게 전달 후 결과 출력

| 챗봇 초기화면 | 챗봇 질문 | 챗봇 답변 |
| --- | --- | --- |
| ![Image](https://github.com/user-attachments/assets/324634d9-6420-4be0-a6e8-ffb84641be55) | ![Image](https://github.com/user-attachments/assets/c8d254c1-b69a-45be-b994-610a1c6f0b9a) | ![Image](https://github.com/user-attachments/assets/8a081d4a-6fc0-4323-af8b-dc4de766ea27) |

- **눈 건강 검사**: 직접 제작하고 학습시킨 ['pet_eye_disease_model_v4.h5' 머신러닝 모델](https://github.com/dohb128/SW2025)을 **AWS EC2** 서버에 배포하여 반려동물의 눈 건강 상태를 확인

![Image](https://github.com/user-attachments/assets/c262c401-c34b-48d8-bee0-c6e28df184bc)
  - 홈 화면에서 플로팅 버튼 선택
  - 카메라 또는 갤러리로 이미지 선택 후 ‘눈 건강 확인하기‘ 버튼 선택
  - **AWS EC2 FlaskServer**로 이미지 전송 후 결과 출력
  - 반응속도 1~2초
  - 약 92% 정확도(13/14)

| 홈 화면의 버튼 | 눈 건강 검사 초기화면 | 눈 건강 검사 결과 |
| --- | --- | --- |
| ![Image](https://github.com/user-attachments/assets/1db65251-e0c5-4be6-85de-1a021f418fdf) | ![Image](https://github.com/user-attachments/assets/929ef2e4-5f37-433c-8839-c39ddc1fc9cb) | ![Image](https://github.com/user-attachments/assets/bda8dd87-a237-467f-a03a-10c5a6c69e2f) |

- **진료 기록 관리**: 반려동물의 예방 접종, 투약 등 의료 기록 등록 및 조회
  - 날짜, 진료 항목, 메모 등록 가능
  - ‘진료‘, ‘접종‘, ‘약 복용‘ 총 3가지 타입으로 분류
  - 등록된 동물별로 진료 기록 관리
  - 등록된 항목은 데이터베이스에 저장

| 진료 기록 화면 | 진료 기록 추가 |
| --- | --- |
| ![Image](https://github.com/user-attachments/assets/6ae75699-f2c4-4aad-b80c-c0565ef76a22) |![Image](https://github.com/user-attachments/assets/9158fa15-26c1-4481-a4a4-5f95ef2a09e4) |

- **주변 병원 찾기**: Google Maps를 이용한 주변 동물 병원 검색 및 위치 표시
  - Google Maps API를 활용해 구현
  - GPS로 현재 위치를 받아, 주변 동물병원을 탐색한 뒤, 마커를 선택하면 해당 병원의 정보 표시 

![Image](https://github.com/user-attachments/assets/70a64f58-664b-4816-9471-9fe1577a1780)

- **사용자 프로필**: 사용자 정보 관리 및 로그인 기능
![Image](https://github.com/user-attachments/assets/07361122-7977-4229-bc77-defede41c68a)

# 프로젝트 성과

* **독자적인 머신러닝 모델 개발 및 배포**: 반려동물 눈 질환 진단을 위한 모델을 **직접 제작하고 학습**시켰으며, 이를 **AWS EC2**에 배포하여 애플리케이션의 핵심 기능을 구현했습니다.
* **클라우드 기반 서버 구축**: Flask를 활용한 API 서버를 구축하고, AWS를 통해 안정적으로 운영함으로써 앱의 핵심 기능을 효과적으로 지원했습니다.
