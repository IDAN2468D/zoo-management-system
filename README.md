# 🐾 Zoo Management System (מערכת ניהול גן חיות)

[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 3.4.1](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Tests](https://img.shields.io/badge/Tests-61%20Passed-success.svg)](https://github.com/IDAN2468D/zoo-management-system)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg)](https://www.docker.com/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D.svg)](http://localhost:9091/swagger-ui.html)

מערכת Enterprise מקיפה ומתקדמת לניהול גן חיות, הבנויה באמצעות **Spring Boot 3.4.1**, **Java 17**, **Spring Security (RBAC)**, **Spring Data JPA**, מסדי נתונים **PostgreSQL 16 / H2**, ממשק משתמש אינטראקטיבי עשיר (Dashboard), מערך צילום בינה מלאכותית (NaNoBanana 2.1) ומערך בדיקות אוטומטי מקיף הכולל **61 בדיקות יחידה ואינטגרציה**.

---

## 🌟 מאפייני המערכת העיקריים (Key Features)

### 🦁 1. ניהול חיות מתקדם (Animals Management)
- **20 חיות מגוונות בנתוני ברירת המחדל:** אריות, טיגריס, פנתר, שימפנזה, גורילה, למור, עיטם, תוכי, פלמינגו, פינגווין, דולפין, צב ים, פיל, ג'ירפה, זברה, פנדה, פיתון, זיקית ואילנית.
- **פילוח רב-שכבתי:** מינים (Species), תתי-מינים (SubSpecies), סוגי תזונה (DietType), מגדר (Gender) וסטטוס שימור (ConservationStatus).
- **מעקב בריאותי:** בריא, חולה, פצוע, בהשגחה, ובידוד רפואי (Quarantined).
- **זיהוי שבב דיגיטלי:** מעקב אחר מזהה שבב ייחודי (Microchip ID), ארץ מוצא ולוחות זמני האכלה.
- **צילום ריאליסטי מובנה:** אינטגרציית תמונות לכל 20 החיות המוגשות ישירות דרך השרת (`/images/{name}.jpg`).

### 🔐 2. אבטחה, אימות והרשאות מבוססות תפקידים (Security & RBAC)
- **מנגנון Spring Security מובנה:** אימות בטוח באמצעות Basic Auth ו-Session, הצפנת סיסמאות עם BCrypt.
- **חלוקת תפקידים קפדנית (Role-Based Access Control):**
  - **`ROLE_ADMIN` (מנהל ראשי):** גישה מלאה לכל חלקי המערכת, הוספה, עריכה ומחיקת חיות, ניהול מלא של מלאי, כלובים, משימות, רופאים ועובדים.
  - **`ROLE_VET` (וטרינר):** הרשאה מלאה להזנת דוחות רפואיים, ביצוע בדיקות, עדכון סטטוס בריאותי, העברה לבידוד רפואי וטיפול במשימות רפואיות.
  - **`ROLE_KEEPER` (מטפל חיות):** הרשאה להאכלת חיות, חידוש מלאי מזון, עדכון משימות טיפול והעברת חיות בין מתחמים.
- **משתמשי ברירת מחדל מובנים:**
  - 👑 **מנהל:** שם משתמש `admin` | סיסמה `admin123`
  - 🩺 **וטרינרית:** שם משתמש `vet` | סיסמה `vet123`
  - 🥩 **מטפלת:** שם משתמש `keeper` | סיסמה `keeper123`
- **עמוד התחברות ייעודי (`/login.html`):** ממשק התחברות מהיר עם כפתורי מעבר מהיר (Quick Fill) לפי תפקידים.

### 📦 3. ניהול מלאי והזנה (Inventory & Stock Management)
- מעקב רציף אחר כמויות מזון, יחידות מידה (ק"ג, יחידות) וספי מינימום קריטיים (Minimum Threshold).
- התראות אוטומטיות בזמן אמת על מלאי מזון נמוך הדורש הזמנה.
- פעולת חידוש מלאי מהירה (`Restock`) המאושרת למטפלים ולמנהלים.

### 📋 4. מערך משימות צוות הגן (Zoo Task Management)
- לוח משימות שיתופי הכולל כותרת, תיאור מפורט, תפקיד יעד (Admin/Vet/Keeper), עובד מוקצה, שיוך לחיה, רמת דחיפות (Urgent, High, Medium, Low), תאריך יעד וסטטוס (Pending, In Progress, Completed).
- עדכון סטטוס משימה בלחיצת כפתור ישירות מהדשבורד.

### 🚨 5. מרכז התראות חכם בזמן אמת (Smart Alerts Hub)
- מנוע חוקים מובנה המזהה אוטומטית חריגות ובעיות:
  - 🚨 **בידוד רפואי קריטי:** זיהוי חיות בבידוד הדורשות תשומת לב מיידית.
  - 🩺 **טיפול רפואי דחוף:** חיות חולות או פצועות שאינן בבידוד.
  - 🥩 **התראות רעב והאכלה:** התראה מיידית עבור חיות שלא הואכלו מעל 8 שעות (אזהרה) או מעל 12 שעות (קריטי).
  - ⚠️ **תפוסת יתר בכלובים:** התראה כאשר מספר החיות חורג מקיבולת המתחם המקסימלית.
  - ⚡ **משימות דחופות:** התראה על משימות בעדיפות URGENT שטרם הושלמו.
  - 📦 **חוסר במלאי:** התראה על פריטי מזון שירדו מתחת לסף המינימום.

### 📊 6. דשבורד אנליטיקה ו-BI (Analytics & Insights)
- פילוח התפלגות חיות לפי מחלקות ומשפחות.
- פילוח בריאותי (בריאים מול חולים ומושגחים).
- פילוח סוגי תזונה וסטטוס שימור (סכנת הכחדה עולמית).
- דוח תפוסת כלובים בזמן אמת עם חישוב אחוזי תפוסה ומדדים סביבתיים (טמפרטורה, לחות).

### 🩺 7. תיעוד רפואי והאכלות (Medical Records & Feeding Logs)
- יומן היסטוריית האכלות מלא לכל חיה עם כמויות, סוג מזון, שם המאכיל והערות.
- יומן טיפולים רפואיים, בדיקות תקופתיות, חיסונים, תרופות והמלצות וטרינריות.

### 📜 8. מסנן ביקורת ומעקב (Audit Logging Filter)
- `ZooAuditLoggingFilter` המתעד בלוגים מפורטים כל קריאת HTTP, כולל זמן ריצה, סטטוס תגובה, מתודה, נתיב ושם המשתמש המבצע.

---

## 🛠️ טכנולוגיות וארכיטקטורה (Tech Stack)

| שכבה | טכנולוגיה | פירוט |
|---|---|---|
| **שפת פיתוח** | **Java 17** (LTS) | תחביר מודרני, Records, Pattern Matching |
| **Backend Framework** | **Spring Boot 3.4.1** | Spring Web, Spring Data JPA, Spring Security, Validation |
| **אבטחה ואימות** | **Spring Security** | BCrypt Password Encoder, HTTP Basic, RBAC |
| **תיעוד API** | **OpenAPI 3 / Swagger** | Springdoc OpenAPI UI בנתיב `/swagger-ui.html` |
| **מסדי נתונים** | **H2 & PostgreSQL 16** | H2 לפיתוח מהיר, PostgreSQL לסביבת Docker/Production |
| **Frontend** | **Vanilla HTML5, CSS3, ES6+ JS** | דשבורד מודרני, Glassmorphism, רספונסיבי, תמיכה מלאה ב-RTL |
| **DevOps & Containers** | **Docker & Docker Compose** | Multi-stage Docker build, קונטיינרים מבודדים לאפליקציה ולמסד הנתונים |
| **בדיקות אוטומטיות** | **JUnit 5, MockMvc, AssertJ** | **61 בדיקות אוטומטיות** מלאות |

---

## 🚀 הרצה מקומית מהירה (Quick Start)

### דרישות מקדימות:
- **Java 17** ומעלה מותקן
- **Maven** (כלול `mvnw` wrapper בפרויקט)
- *(אופציונלי)* Docker ו-Docker Compose

### 1. הרצת כל הבדיקות:
```bash
./mvnw clean test
```
> [!NOTE]
> כל 61 הבדיקות (אימות, בקרים, שירותים, מודלים, הרשאות) רצות ועוברות בהצלחה מלאה!

### 2. הפעלת השרת:
```bash
./mvnw spring-boot:run
```

המערכת תהיה זמינה בנתיבים הבאים:
- 🌐 **ממשק המשתמש (Dashboard):** [http://localhost:9091](http://localhost:9091)
- 🔑 **דף התחברות (Login):** [http://localhost:9091/login.html](http://localhost:9091/login.html)
- 📖 **תיעוד ה-API המלא (Swagger UI):** [http://localhost:9091/swagger-ui.html](http://localhost:9091/swagger-ui.html)
- 📄 **OpenAPI JSON Spec:** [http://localhost:9091/v3/api-docs](http://localhost:9091/v3/api-docs)
- 🗄️ **מסוף H2 Database Console:** [http://localhost:9091/h2-console](http://localhost:9091/h2-console)
  - **JDBC URL:** `jdbc:h2:mem:zoodb`
  - **User:** `sa`
  - **Password:** *(ריק)*
- 🩺 **בדיקת בריאות המערכת (Health Check):** [http://localhost:9091/api/health](http://localhost:9091/api/health)

---

## 👥 משתמשי מערכת והרשאות (Pre-configured Credentials)

המערכת מגיעה מוגדרת מראש עם 3 משתמשים עבור כל רמת הרשאה ב-RBAC:

| תפקיד | שם משתמש | סיסמה | תיאור והרשאות עיקריות |
|---|---|---|---|
| **ADMIN** | `admin` | `admin123` | **מנהל ראשי:** ניהול מלא של כל החיות, כלובים, עובדים, מלאי ומשימות |
| **VET** | `vet` | `vet123` | **וטרינרית ראשית:** עדכון תיקים רפואיים, בדיקות, העברה לבידוד וטיפול במשימות |
| **KEEPER** | `keeper` | `keeper123` | **מטפלת חיות:** האכלת חיות, חידוש מלאי מזון והעברת חיות בין מתחמים |

---

## 🐳 הרצה באמצעות Docker & Docker Compose

להרמת סביבת ייצור מלאה הכוללת את האפליקציה ומסד נתונים **PostgreSQL 16**:

```bash
# בנייה והרצה ברקע
docker-compose up --build -d

# בדיקת סטטוס הקונטיינרים
docker-compose ps

# צפייה בלוגים של האפליקציה
docker-compose logs -f app

# עצירת הסביבה
docker-compose down
```

---

## 📡 נקודות קצה ב-API (REST API Endpoints)

### 🔑 אימות והרשאה (`/api/auth`)
| מתודה | נתיב | הרשאה | תיאור |
|---|---|---|---|
| `POST` | `/api/auth/login` | ציבורי | התחברות למערכת וקבלת פרטי משתמש ותפקיד |
| `POST` | `/api/auth/register` | ציבורי | הרשמת משתמש חדש במערכת |
| `GET` | `/api/auth/me` | ציבורי | קבלת פרטי המשתמש המחובר הנוכחי |
| `POST` | `/api/auth/logout` | ציבורי | התנתקות מהמערכת |

### 🦁 חיות (`/api/animals`)
| מתודה | נתיב | הרשאה | תיאור |
|---|---|---|---|
| `GET` | `/api/animals` | ציבורי | קבלת כל החיות (כולל פרמטרים לסינון: `species`, `health`, `diet`, `endangered`, `cageId`, `q`) |
| `GET` | `/api/animals/{id}` | ציבורי | קבלת פרטי חיה לפי מזהה |
| `GET` | `/api/animals/stats` | ציבורי | קבלת סטטיסטיקות כלליות (סה"כ חיות, חולים, מינים בסכנה) |
| `GET` | `/api/animals/search?q={query}` | ציבורי | חיפוש חיות חופשי לפי שם, מין או שבב |
| `GET` | `/api/animals/endangered` | ציבורי | קבלת כל החיות המוגדרות בסכנת הכחדה |
| `GET` | `/api/animals/hungry?hours={h}` | ציבורי | קבלת רשימת חיות שלא הואכלו מעל X שעות |
| `GET` | `/api/animals/metadata` | ציבורי | קבלת כל הגדרות ה-Enums (מינים, תזונה, סטטוסים, מגדר) לתמיכה ב-UI |
| `POST` | `/api/animals` | `ADMIN` | הוספת חיה חדשה |
| `PUT` | `/api/animals/{id}` | `ADMIN` | עדכון פרטי חיה קיימת |
| `DELETE` | `/api/animals/{id}` | `ADMIN` | מחיקת חיה |
| `POST` | `/api/animals/{id}/feed` | `KEEPER`, `ADMIN` | תיעוד האכלה חדשה עבור חיה |
| `GET` | `/api/animals/{id}/feedings` | ציבורי | היסטוריית האכלות של חיה |
| `POST` | `/api/animals/{id}/medical` | `VET`, `ADMIN` | תיעוד בדיקה/טיפול רפואי ועדכון סטטוס בריאותי |
| `GET` | `/api/animals/{id}/medical` | ציבורי | היסטוריה רפואית של חיה |
| `POST` | `/api/animals/{id}/quarantine` | `VET`, `ADMIN` | העברה מיידית של חיה לבידוד רפואי |
| `PUT` | `/api/animals/{id}/cage/{cageId}` | `KEEPER`, `ADMIN` | העברת חיה לכלוב/מתחם מסוים |
| `DELETE` | `/api/animals/{id}/cage` | `KEEPER`, `ADMIN` | הוצאת חיה מכלוב (ללא כלוב) |

### 📦 מלאי ומזון (`/api/inventory`)
| מתודה | נתיב | הרשאה | תיאור |
|---|---|---|---|
| `GET` | `/api/inventory` | ציבורי | קבלת רשימת פריטי המלאי והמזון |
| `POST` | `/api/inventory` | `ADMIN` | יצירת פריט מלאי חדש |
| `PUT` | `/api/inventory/{id}` | `ADMIN` | עריכת פריט מלאי קיים |
| `DELETE` | `/api/inventory/{id}` | `ADMIN` | מחיקת פריט מלאי |
| `POST` | `/api/inventory/{id}/restock` | `KEEPER`, `ADMIN` | הוספת כמות מלאי לפריט קיים |
| `POST` | `/api/inventory/bulk` | `ADMIN` | יבוא / הוספה מרוכזת של פריטי מלאי |

### 📋 משימות צוות (`/api/tasks`)
| מתודה | נתיב | הרשאה | תיאור |
|---|---|---|---|
| `GET` | `/api/tasks` | ציבורי | קבלת כל משימות הצוות (ממוינות לפי תאריך יצירה) |
| `GET` | `/api/tasks/{id}` | ציבורי | קבלת משימה לפי מזהה |
| `POST` | `/api/tasks` | `KEEPER`, `VET`, `ADMIN` | יצירת משימה חדשה |
| `PUT` | `/api/tasks/{id}/status` | `KEEPER`, `VET`, `ADMIN` | עדכון סטטוס משימה בלבד (`PENDING`, `IN_PROGRESS`, `COMPLETED`) |
| `PUT` | `/api/tasks/{id}` | `KEEPER`, `VET`, `ADMIN` | עריכת פרטי משימה מלאים |
| `DELETE` | `/api/tasks/{id}` | `ADMIN` | מחיקת משימה |

### 🚨 התראות חכמות (`/api/alerts`)
| מתודה | נתיב | הרשאה | תיאור |
|---|---|---|---|
| `GET` | `/api/alerts` | ציבורי | קבלת כל ההתראות הפעילות (בריאות, רעב, תפוסת כלובים, משימות דחופות, מלאי נמוך) |

### 📊 אנליטיקה ו-BI (`/api/analytics`)
| מתודה | נתיב | הרשאה | תיאור |
|---|---|---|---|
| `GET` | `/api/analytics` | ציבורי | דוח אנליטי מקיף (התפלגות מינים, בריאות, תזונה, סכנת הכחדה, תפוסת כלובים ומדדי סביבה) |

### 🏡 מתחמים וכלובים (`/api/cages`)
| מתודה | נתיב | הרשאה | תיאור |
|---|---|---|---|
| `GET` | `/api/cages` | ציבורי | קבלת כל המתחמים והכלובים |
| `GET` | `/api/cages/{id}` | ציבורי | קבלת מתחם לפי מזהה |
| `POST` | `/api/cages` | `ADMIN` | יצירת מתחם/כלוב חדש |
| `PUT` | `/api/cages/{id}` | `ADMIN` | עדכון נתוני כלוב |
| `DELETE` | `/api/cages/{id}` | `ADMIN` | מחיקת כלוב |

### 👨‍⚕️ צוות עובדים ורופאים (`/api/employees`, `/api/veterinarians`)
| מתודה | נתיב | הרשאה | תיאור |
|---|---|---|---|
| `GET` | `/api/employees` | ציבורי | קבלת רשימת העובדים והמטפלים |
| `POST` | `/api/employees` | `ADMIN` | הוספת עובד חדש |
| `PUT` | `/api/employees/{id}` | `ADMIN` | עריכת עובד |
| `DELETE` | `/api/employees/{id}` | `ADMIN` | מחיקת עובד |
| `POST` | `/api/employees/{empId}/cages/{cageId}` | `ADMIN` | הקצאת כלוב לטיפול עובד |
| `GET` | `/api/veterinarians` | ציבורי | קבלת רשימת הווטרינרים המוסמכים |
| `POST` | `/api/veterinarians` | `ADMIN` | הוספת וטרינר |
| `PUT` | `/api/veterinarians/{id}` | `ADMIN` | עדכון פרטי וטרינר |
| `DELETE` | `/api/veterinarians/{id}` | `ADMIN` | מחיקת וטרינר |

---

## 📁 מבנה הפרויקט (Project Directory Structure)

```
zoo-management-system/
├── .github/
│   └── workflows/
│       └── docker-image.yml       # תהליך CI/CD אוטומטי לבניית Docker Image
├── src/
│   ├── main/
│   │   ├── java/com/zoo/management/
│   │   │   ├── config/            # קונפיגורציית Spring Security, OpenAPI, מסנן Audit ו-Data Initializer
│   │   │   ├── controller/        # בקרי REST API (חיות, התראות, אנליטיקה, מלאי, משימות, אימות, כלובים וצוות)
│   │   │   ├── dto/               # מודלי העברת נתונים (Auth, Feeding, Medical, Stats, Login, Register)
│   │   │   ├── exception/         # טיפול גלובלי בחריגות ושגיאות (ControllerAdvice)
│   │   │   ├── model/             # ישויות JPA, יחסים ו-Enums
│   │   │   ├── repository/        # ממשקי Spring Data JPA
│   │   │   ├── service/           # לוגיקה עסקית (ZooService, CustomUserDetailsService)
│   │   │   └── ManagementApplication.java
│   │   └── resources/
│   │       ├── application.properties # הגדרות שרת, מסד נתונים, לוגים ו-Swagger
│   │       └── static/            # ממשק משתמש מלא (HTML5, CSS3, JavaScript, תמונות חיות)
│   │           ├── index.html     # דשבורד ניהול ראשי
│   │           ├── login.html     # מסך התחברות
│   │           ├── styles.css     # עיצוב מודרני מבוסס משתנים ו-Glassmorphism
│   │           ├── app.js         # לוגיקת צד-לקוח, קריאות API וניהול תצוגה
│   │           └── images/        # צילומי בינה מלאכותית של 20 חיות הגן
│   └── test/                      # 61 בדיקות יחידה ואינטגרציה (JUnit 5, MockMvc)
├── Dockerfile                     # Multi-stage Docker build מותאם ייצור
├── docker-compose.yml             # תזמור שרת האפליקציה יחד עם PostgreSQL 16
├── pom.xml                        # הגדרות תלויות Maven
├── test_system.ps1                # סקריפט PowerShell לבדיקת Endpoints מקצה לקצה
└── README.md                      # מדריך המערכת והתיעוד
```

---

## 🧪 בדיקות אוטומטיות (Test Suite Breakdown)

המערכת מגובה ב-**61 בדיקות אוטומטיות מלאות** הרצות ללא תלות במסד נתונים חיצוני:
- `ZooAnimalServiceTests` (8 בדיקות) – לוגיקה עסקית, העברת כלובים, האכלה, תיעוד רפואי וסינונים.
- `AnimalControllerTests` (20+ בדיקות) – נקודות קצה של חיות, אימות קלט, טיפול בשגיאות וקודי תגובה.
- `AuthControllerTests` – תהליך התחברות, שגיאות סיסמה, הרשמה וזיהוי תפקידים.
- `InventoryControllerTests` (7 בדיקות) – יצירה, עריכה, מחיקה, חידוש מלאי והגבלות הרשאה (RBAC).
- `ZooTaskControllerTests` (5 בדיקות) – יצירה, שינוי סטטוס, עדכון, מחיקה ואבטחה.
- `AlertControllerTests` – חוקי התראות חכמות וחישוב סטטוסים קריטיים.
- `AnalyticsControllerTests` – דיוק מדדי התפלגות וחישוב תפוסת מתחמים.
- `ModelTests` (9 בדיקות) – ישויות נתונים, Enums, תאימות שדות וקשרי גומלין.
- `ManagementApplicationTests` – אימות טעינת ה-Spring Context במלואו.

להרצת הבדיקות:
```bash
./mvnw clean test
```

---

## 📄 רישיון (License)

פרויקט זה מופץ תחת רישיון **MIT**. פרטים נוספים ניתן למצוא בקובץ המצורף בפרויקט.
