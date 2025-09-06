# 프로젝트 정보

- **프로젝트 이름**: PetCare
- **프로젝트 로고**: <img width="1024" height="1024" alt="Image" src="https://github.com/user-attachments/assets/c7d4bfc1-85d4-4a60-a511-017db42a22a1" />
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

# 주요 기능

- **반려동물 관리**: 반려동물 정보 등록 및 관리
- **챗봇 상담**: OpenAI API를 활용한 반려동물 관련 챗봇 상담
- **눈 건강 검사**: 반려동물의 눈 건강 상태를 확인하는 기능
- **의료 기록 관리**: 반려동물의 예방 접종, 투약 등 의료 기록 등록 및 조회
- **주변 병원 찾기**: Google Maps를 이용한 주변 동물 병원 검색 및 위치 표시
- **사용자 프로필**: 사용자 정보 관리 및 로그인 기능

# 기타 추가 사항

- **개발 일지**: https://www.notion.so/2647494724cb8085931cd7687269dcd1?source=copy_link