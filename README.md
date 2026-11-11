# Secure Note-Taking App - Backend

Backend service for Secure Note-Taking Application built with **Spring Boot 3**, **Java 21**, **Spring Security**, and **Spring Data MongoDB**.

---

## 🚀 Live URL
- **Production Backend URL**: `https://note-taker-oxfz.onrender.com`
- **Base API Path**: `https://note-taker-oxfz.onrender.com/api`

---

## 🛠 Tech Stack
- **Framework**: Spring Boot 3.4.2
- **Language**: Java 21
- **Database**: MongoDB (Atlas) via Spring Data MongoDB
- **Security & Auth**: Spring Security 6, BCrypt Password Encoder, JWT (`io.jsonwebtoken:jjwt:0.12.6`)
- **Containerization**: Docker (Multi-stage build)

---

## 🗄 Collections & Custom Indexes
As specified in requirements, only **4 custom indexes** are created (in addition to default `_id`):

| Collection | Custom Index | Supports Query |
|---|---|---|
| `users` | `{ email: 1 }` (unique) | Login lookup |
| `users` | `{ interests: 1 }` | Group-by-interests aggregation |
| `notes` | `{ ownerId: 1, _id: -1 }` | A user's paginated notes list |
| `posts` | `{ authorId: 1, _id: -1 }` | `$lookup` for a user's posts |

> All queries on profile, note by id, admin user list, and admin all notes list are served by the default `_id` index (sorted `_id` descending).

---

## 📋 API Endpoints

### 1. Authentication (`/api/auth`)
- `POST /api/auth/register` - Register a new user (with role `USER` or `ADMIN` and interests list).
- `POST /api/auth/login` - Login with email & password, returns JWT token.

### 2. User Notes (`/api/notes`) - Restricted to owner
- `POST /api/notes` - Create a new note.
- `GET /api/notes?page=0&size=10` - List current user's notes (paginated).
- `GET /api/notes/{id}` - Get note by ID (restricted to owner).
- `PUT /api/notes/{id}` - Update note by ID (restricted to owner).
- `DELETE /api/notes/{id}` - Delete note by ID (restricted to owner).

### 3. Admin Endpoints (`/api/admin`) - Requires `ROLE_ADMIN`
- `GET /api/admin/notes?page=0&size=10` - List all notes across all users.
- `GET /api/admin/users?page=0&size=10` - List all users (paginated).
- `GET /api/admin/users/{id}` - Get user details.
- `POST /api/admin/users` - Create a user.
- `PUT /api/admin/users/{id}` - Update user.
- `DELETE /api/admin/users/{id}` - Delete user.

### 4. Posts (`/api/posts`)
- `POST /api/posts` - Create a public post authored by the caller.

### 5. Aggregations (`/api/users`)
- `GET /api/users/by-interests?page=0&size=10` - Groups users by interests using single `mongoTemplate.aggregate()` stage pipeline.
- `GET /api/users/{id}/posts?page=0&size=10` - Single pipeline on `users` with `$match _id` and `$lookup` from `posts`.

---

## 💻 Local Development Setup

### Prerequisites
- JDK 21
- Maven 3.9+
- MongoDB connection string

### Run Locally
```bash
# 1. Navigate to backend directory
cd backend

# 2. Build project
mvn clean compile

# 3. Run automated tests (Index & explain verification)
mvn test

# 4. Start Spring Boot application
mvn spring-boot:run
```
Backend will start on port `8080`.

---

## 🐳 Docker Setup
To build and run with Docker locally:
```bash
# Build Docker image
docker build -t notes-backend .

# Run Docker container
docker run -p 8080:8080 -e PORT=8080 notes-backend
```

