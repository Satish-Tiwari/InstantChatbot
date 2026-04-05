# 🛠️ Setup Guide

## Prerequisites

- **Docker Manager** (Docker Desktop or Docker Compose)
- **Node.js** (v18+) for local frontend development
- **Java 17** for local backend development
- **Maven** (optional, Docker handles builds)

## Local Development (Using Docker)

The easiest way to get the **Instant Chatbot** platform running is through Docker Compose.

```bash
docker-compose up -d --build
```

This will spin up:
- **PostgreSQL (Port 5433)**: With Vector support.
- **Backend (Port 8080)**: Spring Boot REST API.
- **Frontend (Port 3000)**: Next.js Dashboard.

## Environment Configuration

Both backend and frontend services have `.env.example` files that can be copied to `.env` and modified.

### Backend `.env`
```env
# Database Configuration
POSTGRES_USER=postgres
POSTGRES_PASSWORD=admin
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/instantchatbot

# AI API Configuration
OPENAI_API_KEY=your_key_here

# App-specific Configuration
ZIP_STORAGE_PATH=/app/generated-bots
CORS_ORIGINS=http://localhost:3000
```

### Frontend `.env`
```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

## Running Components Individually

### 1. Start PostgreSQL
```bash
docker compose up -d postgres
```

### 2. Run Backend
```bash
cd backend
./mvnw spring-boot:run
```

### 3. Run Frontend
```bash
cd frontend
npm install
npm run dev
```

## Quick Verification

Once everything is up, visit:
- **Frontend**: http://localhost:3000
- **API Health**: http://localhost:8080/actuator/health
- **Postman**: Import the provided Postman collection for testing.
