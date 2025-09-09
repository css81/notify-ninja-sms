# NotifyNinja 프로젝트 구조 및 개발 가이드

이 문서는 NotifyNinja 앱 프로젝트의 **전체 구조**, **권장 파일 및 폴더 구조**, **레이아웃 XML 관리**, **권한/서비스 처리**, **개발 관례** 등을 정리한 문서입니다.  
개발자 및 유지보수 팀이 자유롭게 참고용으로 활용할 수 있습니다.

---

## 1. 프로젝트 개요

- **앱 목적**: 특정 앱 알림을 감지하여 SMS/Push로 포워딩  
- **주요 기능**
  - 앱별 알림 감지
  - SMS 포워딩 (최대 5개 연락처)
  - Push 알림 포워딩
  - 개인정보 동의 처리 및 권한 관리
  - 시스템 알림 감지

- **사용 기술**
  - Java + Android SDK
  - SQLite (DBHelper)
  - NotificationListenerService
  - RecyclerView

---

## 2. Java 코드 구조

모든 Java 소스 코드는 `com.sschoi.notifyninja` 패키지 하위에서 기능별로 나누어 관리합니다.

```
src/main/java/com/sschoi/notifyninja/
├── activity/       # 앱 화면(Activity) 관련 클래스
│   ├── MainActivity.java
│   ├── RegisterActivity.java
│   ├── PrivacyConsentActivity.java
│   └── SplashActivity.java
├── adapter/        # RecyclerView 등 Adapter 관련 클래스
│   └── AppAdapter.java
├── model/          # 데이터 모델 클래스
│   └── AppModel.java
├── db/             # DBHelper 등 데이터베이스 관련 클래스
│   └── DBHelper.java
├── service/        # NotificationListener 등 서비스 관련 클래스
│   └── MyNotificationListener.java
└── util/           # Helper 클래스
    └── SMSHelper.java
```

### 2.1 정리 기준

- **Activity**: 화면 단위로 activity 패키지에 배치  
- **Adapter**: RecyclerView 등 리스트 처리용 Adapter는 adapter 패키지  
- **Model**: 앱 데이터 구조, DB 테이블 매핑 클래스  
- **DB**: SQLite 등 DB 처리 관련 클래스  
- **Service**: NotificationListenerService 등 백그라운드 서비스  
- **Util**: SMS 전송, 공통 기능 헬퍼 클래스  

---

## 3. Layout XML 구조

모든 XML 레이아웃 파일은 `res/layout` 하위에서 화면/아이템 단위로 관리합니다.

```
app/src/main/res/layout/
├── activity/
│   ├── activity_main.xml
│   ├── activity_register.xml
│   ├── activity_privacy_consent.xml
│   └── activity_splash.xml
└── adapter/
    └── item_app.xml
```

### 3.1 정리 기준

- **activity/**: 각 Activity 화면 레이아웃  
- **adapter/**: RecyclerView 등 Adapter에서 사용하는 아이템 레이아웃  

### 3.2 XML 파일 기능

| XML 파일                       | 설명                                      |
|--------------------------------|-------------------------------------------|
| activity_main.xml               | 앱 메인 화면, 앱/번호 리스트, 설정 버튼  |
| activity_register.xml           | 앱/번호 등록 화면                         |
| activity_privacy_consent.xml    | 권한/개인정보 동의 화면                   |
| activity_splash.xml             | 앱 시작 시 스플래시 화면                  |
| item_app.xml                    | RecyclerView에서 사용하는 앱 아이템 레이아웃 |

---

## 4. DB 구조 (SQLite)

`DBHelper.java`를 통해 관리합니다.

- **테이블명**: `apps`
- **컬럼**
  - `id`: INTEGER PRIMARY KEY AUTOINCREMENT  
  - `package_name`: TEXT, 앱 패키지명  
  - `app_name`: TEXT, 앱 이름  
  - `phone`: TEXT, SMS 전송 번호  
- **제약 조건**: `(package_name, phone)` 조합 UNIQUE, 중복 등록 시 무시  
- **업그레이드 정책**: 기존 데이터 유지하며 구조 변경 가능  

### 4.1 주요 메소드

| 메소드                     | 설명                                    |
|-----------------------------|----------------------------------------|
| insertApp(pkg, name, phone) | 앱+번호 등록                             |
| deleteApp(pkg)              | 특정 앱 삭제                             |
| getAllApps()                | 전체 앱+번호 리스트 조회                 |
| getAllByPackage(pkg)        | 특정 앱의 모든 등록 번호 조회             |

---

## 5. Notification 감지 및 SMS 전송

`MyNotificationListener.java` 사용

- **알림 감지**
  - 앱 패키지별로 NotificationListenerService에서 알림 수신
  - Notification 제목/본문/BigText 추출
- **SMS 전송**
  - 등록된 번호 최대 5개까지 발송
  - SMSHelper 사용하여 플랫폼 호환 처리
  - Push Forward 기능 확장 가능

---

## 6. 권한 처리

### 6.1 필수 권한

| 권한                                    | 설명                                |
|-----------------------------------------|------------------------------------|
| `SEND_SMS`                               | SMS 전송                            |
| `RECEIVE_SMS`                            | SMS 수신                            |
| `READ_SMS`                               | SMS 읽기                             |
| `BIND_NOTIFICATION_LISTENER_SERVICE`     | 알림 감지 서비스 접근                 |
| `QUERY_ALL_PACKAGES`                     | 앱 리스트 조회 (Android 11 이상)     |

### 6.2 동적 권한 요청

- 앱 최초 실행 시 **PrivacyConsentActivity**에서 권한 안내 및 동의 요청
- `ActivityResultLauncher` 사용하여 동적 권한 처리
- 사용자가 거부하면 기능 제한 안내

---

## 7. 권장 화면 흐름

```
SplashActivity
      ↓
PrivacyConsentActivity
      ↓
MainActivity
      ↓
RegisterActivity (필요 시)
```

- 스플래시 화면: 앱 버전 및 개발자 표시  
- 개인정보 동의 화면: SMS/Push 전송 동의 및 설명  
- 메인 화면: 앱 알림 리스트, 등록/삭제, 설정  
- 등록 화면: 앱별 포워딩 번호 추가

---

## 8. 파일 구조 예시

```
NotifyNinja/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sschoi/notifyninja/
│   │   │   │   ├── activity/
│   │   │   │   ├── adapter/
│   │   │   │   ├── model/
│   │   │   │   ├── db/
│   │   │   │   ├── service/
│   │   │   │   └── util/
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity/
│   │   │   │   │   └── adapter/
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── styles.xml
│   │   │   │   │   └── strings.xml
│   │   │   └── AndroidManifest.xml
├── build.gradle
└── README.md
```

---

## 9. 장점

- **유지보수 용이**: 기능 단위 패키지 정리  
- **확장성**: 앱 기능 추가 시 혼란 최소화  
- **보안/정책 준수**: 권한 및 개인정보 동의 구조화  
- **관리 효율**: Activity, Adapter, DB, Service, Util 명확히 분리  

---

## 10. 참고 사항

- **SMS/Push 포워딩**
  - 등록된 최대 5개 번호에만 발송
  - NotificationListenerService에서 알림 감지 후 SMSHelper 호출
- **개인정보 동의**
  - PrivacyConsentActivity에서 SMS/Push 동의 여부 확인 후 기능 활성화
- **재난문자**
  - NotificationListener로 시스템 알림도 감지 가능
- **Gradle**
  - `buildFeatures.buildConfig = true` 활성화 필요 (custom BuildConfig 필드 사용 시)

