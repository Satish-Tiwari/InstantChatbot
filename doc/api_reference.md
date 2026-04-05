# 📋 API Reference

The **Instant Chatbot** backend provides a RESTful API for managing users, projects, and chat interfaces.

## 🔑 Authentication

All protected endpoints require a JWT token in the `Authorization` header.

```http
Authorization: Bearer <your_token>
```

### 1. Register
- **Endpoint**: `/api/auth/register`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123",
    "name": "Jane Doe"
  }
  ```

### 2. Login
- **Endpoint**: `/api/auth/login`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```

### 3. Get Me
- **Endpoint**: `/api/auth/me`
- **Method**: `GET`
- **Response**:
  ```json
  {
    "id": 1,
    "email": "user@example.com",
    "name": "Jane Doe"
  }
  ```

---

## 📁 Projects

Manage project metadata and creation.

### 1. Create Project
- **Endpoint**: `/api/projects`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "name": "My Chatbot",
    "websiteUrl": "https://example.com"
  }
  ```

### 2. List Projects
- **Endpoint**: `/api/projects`
- **Method**: `GET`

### 3. Get Project Details
- **Endpoint**: `/api/projects/{id}`
- **Method**: `GET`

---

## 🕷️ Crawling

Trigger and monitor the crawling process.

### 1. Start Crawl
- **Endpoint**: `/api/projects/{projectId}/crawl`
- **Method**: `POST`

### 2. Get Status
- **Endpoint**: `/api/projects/{projectId}/status`
- **Method**: `GET`

---

## 💬 Chat & Downloads

Interactive chat playground and package distribution.

### 1. Chat
- **Endpoint**: `/api/projects/{projectId}/chat`
- **Method**: `POST`
- **Body**:
  ```json
  {
    "message": "What is this website about?"
  }
  ```
- **Response**:
  ```json
  {
    "answer": "The AI-generated response...",
    "sources": ["https://example.com/page1"],
    "confidence": 0.95
  }
  ```

### 2. Download Bot
- **Endpoint**: `/api/projects/{projectId}/download`
- **Method**: `GET`
- **Response**: Returns a ZIP file containing the chatbot package.
