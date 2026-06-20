# 🌾 Small-Scale Farmer Crop Planner

A Spring Boot web application that helps small-scale farmers plan crop production, manage planting schedules, and predict harvest dates.

---

## ✨ Features

- **User Registration & Login** — Secure session-based authentication with BCrypt password hashing
- **Planting Schedules** — Create and manage schedules with automatic harvest date calculation
- **Crop Catalogue** — 11 pre-seeded crops (Summer & Winter), filterable by season
- **Dashboard** — View total schedules, upcoming harvests, and current season at a glance
- **Status Tracking** — Mark crops as Planted or Harvested; delete any schedule

---

## 🛠 Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Backend   | Java 21, Spring Boot 3.3.5          |
| Security  | Spring Security (session-based)     |
| Database  | MySQL 8 + Spring Data JPA/Hibernate |
| Frontend  | Vanilla HTML/CSS/JavaScript         |
| Build     | Maven 3.9+                          |

---

## 🚀 Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- MySQL 8+

### 1 — Set up the database

```sql
CREATE DATABASE IF NOT EXISTS crop_planner_db;
```

Or run the included helper:
```bash
mysql -u root -p < schema.sql
```

### 2 — Configure the database password

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

> ⚠️ **Never commit real credentials to Git.** The `application.properties` file is in `.gitignore` — keep it that way in production.

### 3 — Build & run

```bash
mvn spring-boot:run
```

The app starts at **http://localhost:8080**

Tables are auto-created by Hibernate on first run. Crop data is seeded automatically by `DataInitializer`.

---

## 📁 Project Structure

```
src/
└── main/
    ├── java/com/cropplanner/
    │   ├── CropPlannerApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java
    │   │   └── DataInitializer.java
    │   ├── controller/
    │   │   ├── AuthController.java
    │   │   ├── CropController.java
    │   │   └── ScheduleController.java
    │   ├── model/
    │   │   ├── User.java
    │   │   ├── Crop.java
    │   │   ├── PlantingSchedule.java
    │   │   └── DTOs.java
    │   ├── repository/
    │   │   ├── UserRepository.java
    │   │   ├── CropRepository.java
    │   │   └── ScheduleRepository.java
    │   └── security/
    │       └── UserDetailsServiceImpl.java
    └── resources/
        ├── static/
        │   ├── index.html
        │   ├── login.html
        │   ├── register.html
        │   ├── dashboard.html
        │   ├── crops.html
        │   ├── css/style.css
        │   └── js/app.js
        └── application.properties
```

---

## 🔌 REST API

| Method | Endpoint                       | Auth     | Description              |
|--------|--------------------------------|----------|--------------------------|
| POST   | `/api/auth/register`           | Public   | Register a new user      |
| POST   | `/api/auth/login`              | Public   | Login                    |
| GET    | `/api/auth/logout`             | Public   | Logout                   |
| GET    | `/api/auth/session`            | Public   | Check session status     |
| GET    | `/api/crops`                   | Public   | List all crops           |
| GET    | `/api/crops/season/{season}`   | Public   | Filter crops by season   |
| GET    | `/api/schedules`               | Required | Get user's schedules     |
| POST   | `/api/schedules`               | Required | Create a schedule        |
| PUT    | `/api/schedules/{id}/status`   | Required | Update schedule status   |
| DELETE | `/api/schedules/{id}`          | Required | Delete a schedule        |
| GET    | `/api/schedules/dashboard`     | Required | Get dashboard statistics |

---

## 🌱 Seeded Crops

| Crop             | Season | Growth Days |
|------------------|--------|-------------|
| Beans (Dry)      | Summer | 85          |
| Butternut Squash | Summer | 110         |
| Groundnuts       | Summer | 130         |
| Maize (Corn)     | Summer | 90          |
| Onions           | Summer | 120         |
| Tomatoes         | Summer | 75          |
| Cabbage          | Winter | 80          |
| Carrots          | Winter | 70          |
| Potatoes         | Winter | 100         |
| Spinach          | Winter | 45          |
| Wheat            | Winter | 120         |

---

## 📝 License

This project was developed as a student academic project. All rights reserved.
