# 🤖 Instant Chatbot — AI Website Assistant

A SaaS platform that transforms any website into a deployable AI-powered chatbot. Enter a URL, and the system crawls the website, builds a RAG knowledge base using **Spring AI + PGVector**, and generates a complete chatbot package ready for deployment.

---

## ✨ Features

- **Smart Web Crawling** — Jsoup-powered crawler follows internal links + sitemap.xml
- **Content Processing** — Cleans HTML, removes boilerplate, chunks text semantically
- **RAG Pipeline (Spring AI)** — OpenAI embeddings + PGVector store + GPT-4o-mini generation
- **Code Generation** — Auto-generates a FastAPI chatbot server + embeddable widget
- **ZIP Packaging** — Download a ready-to-deploy chatbot with docs & deployment scripts
- **Modern Dashboard** — Dark theme Next.js UI with real-time progress tracking
- **JWT Authentication** — Secure user accounts with isolated project data
- **Chat Preview** — Test your chatbot directly in the dashboard

---

## 🏗️ Architecture

```
┌─────────────────┐          ┌─────────────────────────────────────────┐
│   Next.js UI    │          │         Spring Boot Backend             │
│   (Port 3000)   │─── REST ─│                                         │
└─────────────────┘          │  ┌────────┐  ┌─────────┐  ┌──────────┐  │
                             │  │Crawler │→│Chunker  │→│Spring AI │  │
                             │  │(Jsoup) │  │         │  │Embed+RAG │  │
                             │  └────────┘  └─────────┘  └──┬───────┘  │
                             │                              │          │
                             │  ┌────────────────────────┐   │          │
                             │  │ Auth + Projects        │   │          │
                             │  │ Code Gen + ZIP         │   │          │
                             │  └────────────────────────┘   │          │
                             └───────────────────────────────┼──────────┘
                                                             │
                                                     ┌───────▼──────┐
                                                     │ PostgreSQL   │
                                                     │ + PGVector   │
                                                     │  (Port 5432) │
                                                     └──────────────┘
```

**Key: Everything runs in a single JVM.** No separate Python service, no ChromaDB — just Spring Boot + PostgreSQL.

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| **Backend API** | Spring Boot 3.2, Java 17, Spring Security, JPA |
| **AI / RAG** | Spring AI 1.0, OpenAI (GPT-4o-mini + text-embedding-3-small) |
| **Vector Store** | PGVector (PostgreSQL extension via Spring AI) |
| **Web Crawling** | Jsoup 1.17 |
| **Frontend** | Next.js 14, TypeScript, Tailwind CSS |
| **State Mgmt** | Redux Toolkit (auth) + TanStack Query v5 (server state) |
| **Forms** | React Hook Form + Zod schema validation |
| **Database** | PostgreSQL 16 + PGVector |
| **Auth** | JWT (jjwt 0.12) — separate secrets per service |
| **Deployment** | Docker, Docker Compose |

---

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- OpenAI API key

### 1. Clone & Configure

```bash
git clone <repository-url>
cd InstantChatbot

# Create .env for each service from their templates
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
```

Edit **`backend/.env`** — set your secrets:

```env
OPENAI_API_KEY=sk-your-key-here
JWT_SECRET=<generate-a-strong-random-string>
```

Edit **`frontend/.env`** — set your secret:

```env
NEXTAUTH_SECRET=<generate-a-strong-random-string>
```

> ⚠️ **Each service has its own secret key.** Never reuse the same value for both.

### 2. Start with Docker Compose

```bash
docker-compose up --build
```

This starts 3 services:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **PostgreSQL + PGVector**: localhost:5432

### 3. Use the Platform

1. Open http://localhost:3000
2. Create an account
3. Create a new project with a website URL
4. Click "Start Crawling"
5. Wait for processing to complete
6. Chat with your bot or download the ZIP

---

## 💻 Development Setup (Without Docker)

### Database (PostgreSQL + PGVector)

```bash
docker run -d -p 5432:5432 \
  -e POSTGRES_DB=instantchatbot \
  -e POSTGRES_USER=instantchatbot \
  -e POSTGRES_PASSWORD=instantchatbot123 \
  pgvector/pgvector:pg16
```

### Backend (Spring Boot)

```bash
cd backend
cp .env.example .env      # ← backend's own secret key
# Edit .env → set OPENAI_API_KEY and JWT_SECRET
mvn spring-boot:run
```

Key env vars in `backend/.env.example`:
| Variable | Purpose |
|----------|----------|
| `OPENAI_API_KEY` | Required — OpenAI API access |
| `JWT_SECRET` | Backend-only secret for signing JWTs |
| `DB_*` | PostgreSQL connection |

### Frontend (Next.js)

```bash
cd frontend
cp .env.example .env      # ← frontend's own secret key
# Edit .env → set NEXTAUTH_SECRET
npm install
npm run dev
```

Key env vars in `frontend/.env.example`:
| Variable | Purpose |
|----------|----------|
| `NEXT_PUBLIC_API_URL` | Backend URL (default `http://localhost:8080`) |
| `NEXTAUTH_SECRET` | Frontend-only secret for session signing |

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/auth/me` | Get current user |

### Projects
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects` | List user's projects |
| POST | `/api/projects` | Create new project |
| GET | `/api/projects/{id}` | Get project details |

### Crawl & Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/projects/{id}/crawl` | Start crawling + RAG pipeline |
| GET | `/api/projects/{id}/status` | Get processing status |
| POST | `/api/projects/{id}/chat` | Send chat message (Spring AI RAG) |
| GET | `/api/projects/{id}/download` | Download chatbot ZIP |

---

## 🧠 RAG Pipeline (Spring AI)

The entire AI pipeline runs inside the Spring Boot JVM:

```
URL Input → WebCrawlerService (Jsoup)
         → ContentCleanerService (HTML cleanup)
         → TextChunkerService (semantic splitting)
         → EmbeddingService (Spring AI → OpenAI → PGVector)
         → RagService (Spring AI ChatClient → OpenAI GPT-4o-mini)
```

**Services in `com.instantchatbot.service.ai`:**

| Service | Responsibility |
|---------|---------------|
| `WebCrawlerService` | Crawls website using Jsoup, follows links + sitemap |
| `ContentCleanerService` | Strips boilerplate, extracts main content |
| `TextChunkerService` | Splits text into semantic chunks with overlap |
| `EmbeddingService` | Generates embeddings, stores in PGVector, searches |
| `RagService` | Retrieves context + generates answers via ChatClient |
| `CrawlPipelineService` | Orchestrates the full async pipeline |

---

## 📦 Generated Chatbot Structure

When you download the ZIP, you get a standalone FastAPI chatbot:

```
chatbot-server/
├── main.py              # FastAPI server with /chat endpoint
├── requirements.txt     # Python dependencies
├── .env.example         # Configuration template
├── Dockerfile           # Container deployment
├── README.md            # Setup & deployment guide
├── deploy.sh            # Auto-deployment script
└── widget/
    └── chatbot-widget.js  # Embeddable chat widget
```

---

## 🔒 Security

- **Separate Secret Keys** — Backend (`JWT_SECRET`) and frontend (`NEXTAUTH_SECRET`) each have independent secrets
- **JWT Authentication** — Stateless token-based auth signed with the backend secret
- **Password Hashing** — BCrypt encryption
- **Data Isolation** — PGVector metadata filtering per project
- **Zod Validation** — Client-side schema validation on all forms
- **URL Validation** — Server-side input sanitization
- **CORS Configuration** — Configurable allowed origins

---

## 📊 Project Status Flow

```
PENDING → CRAWLING → PROCESSING → EMBEDDING → READY
                                             ↘ FAILED
```

---

## 📄 License

MIT License

---

Built with ❤️ using Spring Boot, Spring AI, PGVector, Next.js, and OpenAI
