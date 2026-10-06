# 🐾 Zoo Management System (מערכת ניהול גן חיות)

מערכת מקיפה ומתקדמת לניהול גן חיות הבנויה באמצעות **Spring Boot 3**, **Java 17**, **Spring Data JPA**, מסדי נתונים **PostgreSQL / H2**, וממשק משתמש אינטראקטיבי מודרני.

---

## 🌟 מאפייני המערכת (Features)

- **ניהול חיות (Animals Management):**
  - מעקב אחר מינים ותתי-מינים מגוונים (חתוליים, פרימטים, עופות, זוחלים, יונקים, ימיים, דו-חיים).
  - מעקב סטטוס בריאותי (בריא, חולה, פצוע, בבידוד, בהשגחה).
  - תיעוד סוג תזונה (טורף, צמחוני, אוכל-כל, אוכל דגים, אוכל חרקים) וסטטוס שימור (סכנת הכחדה, פגיע ועוד).
  - זיהוי שבב אלקטרוני (Microchip ID).
- **ניהול האכלות (Feeding System):**
  - תיעוד היסטוריית האכלות בזמן אמת, כמות מזון, סוג מזון, שם המאכיל והתראות האכלה.
- **מעקב רפואי ווטרינרי (Veterinary Care):**
  - יומן טיפולים רפואיים, בדיקות תקופתיות, מתן תרופות ועדכון סטטוס בריאותי אוטומטי על ידי וטרינרים מוסמכים.
- **ניהול כלובים ומתחמים (Cages & Enclosures):**
  - ניהול קיבולת, התאמת מינים וסטטוס ניקיון ותחזוקה.
- **צוות עובדים ורופאים (Staff Management):**
  - חלוקת תפקידים (מטפלים, וטרינרים, מנהלים) והקצאת משימות טיפול.
- **דשבורד ווב אינטראקטיבי (Interactive Web UI):**
  - ממשק משתמש אלגנטי, רספונסיבי ונוח לשימוש עם תצוגת סטטיסטיקות, כרטיסי חיות, סינון לפי מינים, רישום חיות חדשות והרצת פעולות האכלה ורפואה בלחיצת כפתור.
- **בדיקות אוטומטיות (Comprehensive Tests):**
  - 23 בדיקות יחידה ואינטגרציה המכסות מודלים, שירותים (Services), בקרים (Controllers) והרצת המערכת.

---

## 🛠️ טכנולוגיות וארכיטקטורה (Tech Stack)

- **Backend:** Java 17, Spring Boot 3.4.1 (Spring Web, Spring Data JPA, Validation)
- **Database:**
  - **H2 In-Memory Database** – מובנה להרצה מקומית ולפיתוח מהיר ללא תלויות חיצוניות.
  - **PostgreSQL 16** – מסד נתונים ייעודי לסביבת ייצור (מוגדר ב-Docker).
- **Frontend:** Vanilla HTML5, CSS3 מודרני, JavaScript ES6+
- **DevOps & Containers:** Dockerfile (Multi-stage build), Docker Compose
- **Testing:** JUnit 5, MockMvc, AssertJ

---

## 🚀 הרצה מקומית (Getting Started)

### דרישות מקדימות:
- **Java 17** ומעלה
- **Maven** (כלול `mvnw` wrapper בפרויקט)
- *(אופציונלי)* Docker & Docker Compose

### 1. הרצה ישירה באמצעות Maven:

```bash
# הרצת הבדיקות
./mvnw clean test

# הרצת השרת
./mvnw spring-boot:run
```

המערכת תעלה בכתובת:
- 🌐 **ממשק המשתמש (Dashboard):** [http://localhost:9091](http://localhost:9091)
- 🗄️ **מסוף H2 Database Console:** [http://localhost:9091/h2-console](http://localhost:9091/h2-console)
  - JDBC URL: `jdbc:h2:mem:zoodb`
  - User Name: `sa`
  - Password: *(ריק)*

---

## 🐳 הרצה באמצעות Docker Compose

להרמת המערכת יחד עם מסד נתונים PostgreSQL בקונטיינר מבודד:

```bash
docker-compose up --build -d
```

לבדיקת הסטטוס:
```bash
docker-compose ps
```

לעצירת הקונטיינרים:
```bash
docker-compose down
```

---

## 📡 נקודות קצה עיקריות ב-API (REST Endpoints)

| מתודה | נתיב | תיאור |
|---|---|---|
| `GET` | `/api/animals` | קבלת כל החיות (עם אפשרות לסינון לפי מין או סטטוס) |
| `POST` | `/api/animals` | הוספת חיה חדשה למערכת |
| `GET` | `/api/animals/{id}` | קבלת פרטי חיה לפי מזהה |
| `POST` | `/api/animals/{id}/feed` | תיעוד האכלה חדשה עבור חיה |
| `POST` | `/api/animals/{id}/medical` | הוספת רשומה רפואית ועדכון סטטוס בריאותי |
| `GET` | `/api/animals/stats` | קבלת סטטיסטיקות כלליות (סה"כ חיות, חולים, מינים בסכנה) |
| `GET` | `/api/animals/metadata` | קבלת כל רשימות ה-Enums (מינים, תזונה, סטטוסים) |
| `GET` | `/health` | בדיקת בריאות השרת |

---

## 📁 מבנה הפרויקט (Project Structure)

```
menegments/
├── src/
│   ├── main/
│   │   ├── java/com/zoo/management/
│   │   │   ├── config/          # מחלקות הגדרה ופילטרים (Audit Filter)
│   │   │   ├── controller/      # REST API Controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── exception/       # טיפול בשגיאות וחריגות גלובלי
│   │   │   ├── model/           # ישויות הנתונים (Entities & Enums)
│   │   │   ├── service/         # שכבת הלוגיקה העסקית (Business Logic)
│   │   │   └── ManagementApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/          # ממשק המשתמש (HTML, CSS, JS)
│   └── test/                    # בדיקות יחידה ואינטגרציה
├── Dockerfile                   # Multi-stage Docker build
├── docker-compose.yml           # תזמור אפליקציה + PostgreSQL
├── pom.xml                      # הגדרות ותלויות Maven
└── README.md
```

---

## 📄 רישיון (License)
פרויקט זה מופץ תחת רישיון MIT.
