# MyRoutine

Jetpack Compose 기반으로 제작한 루틴 관리 안드로이드 앱입니다.  
사용자는 일일 할 일을 등록하고, 알림 설정 및 주간/월간 리포트를 통해 자신의 루틴을 꾸준히 유지할 수 있습니다.  
Firestore를 통해 클라우드에 루틴을 저장하고, 다양한 기기 간 데이터 동기화를 지원합니다.

---

## 📱 주요 기능

- 📋 **일일 루틴 관리**
  - 할 일 등록, 반복 요일 설정, 완료 체크
- ⏰ **알림 기능**
  - 각 루틴마다 지정된 시간에 알림 발송
- 📅 **캘린더 뷰**
  - 날짜별로 루틴 내역 확인
- 📊 **리포트**
  - 주간/월간 완료율 통계, 가장 자주 지킨/놓친 루틴
- 🗓 **반복 조건 설정**
  - 평일/주말/공휴일 구분 지원
- ☁️ **클라우드 연동**
  - Firebase Firestore를 통한 사용자별 데이터 저장

---

## 🛠 사용 기술

| 분야        | 기술                           |
|-------------|--------------------------------|
| 언어        | Kotlin                         |
| UI          | Jetpack Compose, Material3     |
| 네비게이션  | Navigation Compose              |
| DI          | Hilt                           |
| 로컬 DB     | Room                           |
| 클라우드 DB | Firebase Firestore              |
| 인증 (선택) | Firebase Authentication         |

---

## 📂 프로젝트 구조
<pre>
MyRoutine/
├── ui/           # 각 화면 Composable 구성 (Today, Calendar, Report 등)
├── data/         # Firestore & Room 데이터 처리
├── model/        # 데이터 모델
├── viewmodel/    # ViewModel 구성
└── MainActivity.kt   # Scaffold + NavHost 구성
</pre>

---

## 🚧 작업 예정 목록

- [ ] Firestore 연동
- [ ] 알림 기능 구현
- [ ] 공휴일 API 연동
- [ ] 주간/월간 리포트 차트 추가

---

## 👤 개발자 정보

- 제작자: [강종우]  
- 개인 프로젝트 용도로 Jetpack Compose와 Firebase 학습을 위해 제작 중

---
