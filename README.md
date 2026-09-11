# CodeShare V2

A modern full-stack platform for sharing, discovering, and collaborating on code snippets. Built with Spring Boot 3 and React 19.

---

## Features

- **Authentication & Security** — JWT-based stateless authentication (`jjwt 0.12.3`) with BCrypt password hashing. Secure token verification on protected endpoints.
- **Snippet Management** — Create, edit, and delete snippets with public or private visibility. Multi-language syntax highlighting powered by CodeMirror 5.
- **Snippet Forking & Lineage** — Fork any public or shared snippet directly into your account with automatic parent attribution and lineage tracking.
- **Social & Engagement**:
  - **Starring** — Star snippets to save them to your personal Starred library (`/starred`).
  - **Liking** — Upvote snippets with real-time like counts displayed across feeds and snippet views.
  - **Comments** — Discuss snippets with threaded comments and author-managed deletion.
- **User Profiles** — Public profile pages (`/user/:username`) displaying user bio, avatar, GitHub & LinkedIn links, and published snippets. Editable from the profile view.
- **Resilient AI Code Explanations** — Powered by Google Gemini (`gemini-3.7-flash` with automatic fallback to `gemini-2.0-flash`). Features thinking token filtering for clean Markdown rendering, cached responses via Caffeine, and per-user rate limiting.
- **Shareable Links** — Generate token-based public links for any snippet (public or private), viewable without an account, with instant copy and fork capabilities.
- **Search & Discovery** — Fast search by title keywords and language filters with pagination.
- **Production-Ready Architecture**:
  - In-memory Caffeine caching for high performance.
  - Spring Boot Actuator health checks (`/actuator/health`).
  - Gzip HTTP compression enabled.
  - Multi-stage Docker containerization with automated health checks.

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2.5, Spring Boot Actuator |
| **Security** | Spring Security 6, JWT (`jjwt 0.12.3`), BCrypt |
| **Caching** | Caffeine Cache (`spring-boot-starter-cache`) |
| **Persistence** | Spring Data JPA, Hibernate |
| **Database** | MySQL 8 (production & docker), H2 in-memory (local dev & testing) |
| **Frontend** | React 19, Vite, Tailwind CSS 3, CodeMirror 5, React Router 7, Axios |
| **AI** | Google Gemini API (`gemini-3.7-flash`, fallback: `gemini-2.0-flash`) |
| **Build & Tooling** | Maven 3.9, `frontend-maven-plugin` (integrated Node/NPM build) |
| **Testing** | JUnit 5, Mockito, Spring Security Test |
| **Containers** | Docker (multi-stage Alpine image), Docker Compose |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+ (or use the included `mvn.bat` / `mvn.ps1`)
- Node.js 20+ & npm (handled automatically by Maven during build, or needed for independent frontend dev)
- Docker & Docker Compose (optional, for containerized deployment)

---

### Running Locally

#### 1. H2 In-Memory Mode (Zero Setup)

Runs with an in-memory database seeded with sample users and snippets:

```bash
# Windows (PowerShell / Command Prompt)
.\mvn.bat spring-boot:run

# Linux / macOS
mvn spring-boot:run
```

Open **http://localhost:8080** in your browser.

#### 2. MySQL Mode (Persistent Storage)

1. Create the MySQL database:
   ```sql
   CREATE DATABASE codeshare;
   ```

2. Set required environment variables:
   ```bash
   export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/codeshare
   export SPRING_DATASOURCE_USERNAME=root
   export SPRING_DATASOURCE_PASSWORD=yourpassword
   export JWT_SECRET=your-secure-jwt-secret-min-32-chars-long
   ```

3. Run with the MySQL profile:
   ```bash
   # Windows
   .\mvn.bat spring-boot:run -Dspring-boot.run.profiles=mysql

   # Linux / macOS
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

#### 3. Frontend Development Server (Hot Reload)

For active frontend development with Vite HMR:

```bash
# Terminal 1 — Start Backend
.\mvn.bat spring-boot:run

# Terminal 2 — Start Vite Dev Server
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173** (API requests are proxied to port 8080).

---

### Running Tests

Run backend integration and unit tests:

```bash
# Windows
.\mvn.bat test

# Linux / macOS
mvn test
```

---

## Docker Deployment

Run the complete multi-container setup (MySQL 8 + Spring Boot App with embedded React SPA) with a single command:

```bash
# 1. Create environment file from template
cp .env.example .env

# 2. Start containers
docker compose up --build
```

The application will be available at **http://localhost:8080**, and MySQL is mapped to port `3307`.

To stop the containers:
```bash
docker compose down
```

---

## Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `JWT_SECRET` | Yes | — | Secret key for HMAC-SHA signing of JWTs (min 32 characters). |
| `GEMINI_API_KEY` | No | — | Google Gemini API key. AI code explanation is disabled if not provided. |
| `GEMINI_MODEL` | No | `gemini-3.7-flash` | Primary Gemini model (falls back to `gemini-2.0-flash` on failure). |
| `PORT` | No | `8080` | Web server port. |
| `SPRING_DATASOURCE_URL` | MySQL only | — | JDBC connection string (e.g. `jdbc:mysql://localhost:3306/codeshare`). |
| `SPRING_DATASOURCE_USERNAME` | MySQL only | — | Database user. |
| `SPRING_DATASOURCE_PASSWORD` | MySQL only | — | Database password. |
| `MYSQL_ROOT_PASSWORD` | Docker only | `rootpassword` | Root password for MySQL container. |
| `MYSQL_USER` | Docker only | `codeshare` | App database username in Docker. |
| `MYSQL_PASSWORD` | Docker only | `codeshare` | App database password in Docker. |

---

## Demo Accounts

Pre-seeded into the database automatically on startup:

| Username | Password |
|---|---|
| `alice` | `password123` |
| `bob` | `password123` |
| `charlie` | `password123` |
| `michael` | `password123` |

---

## API Reference

### Authentication (`/api/auth`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user account. |
| `POST` | `/api/auth/login` | Public | Authenticate user — returns `{ token, username }`. |
| `GET` | `/api/auth/me` | JWT | Get current authenticated user details. |

### Snippets (`/api/snippets`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/snippets/public?page=0` | Public | Paginated list of public snippets with social stats. |
| `GET` | `/api/snippets/search?keyword=&language=&page=0` | Public | Filter snippets by keyword and/or programming language. |
| `GET` | `/api/snippets/mine` | JWT | Retrieve all snippets created by authenticated user. |
| `GET` | `/api/snippets/starred` | JWT | Retrieve all snippets starred by authenticated user. |
| `GET` | `/api/snippets/{id}` | Optional | Get snippet by ID (private snippets require owner auth). |
| `POST` | `/api/snippets` | JWT | Create a new snippet. |
| `PUT` | `/api/snippets/{id}` | JWT | Update snippet (owner only). |
| `DELETE` | `/api/snippets/{id}` | JWT | Delete snippet (owner only). |
| `POST` | `/api/snippets/{id}/fork` | JWT | Fork snippet into authenticated user's library. |
| `POST` | `/api/snippets/{id}/like` | JWT | Like snippet. |
| `POST` | `/api/snippets/{id}/unlike` | JWT | Remove like from snippet. |
| `POST` | `/api/snippets/{id}/star` | JWT | Star snippet. |
| `POST` | `/api/snippets/{id}/unstar` | JWT | Unstar snippet. |
| `GET` | `/api/snippets/{id}/comments` | Public | Retrieve comments on snippet. |
| `POST` | `/api/snippets/{id}/comments` | JWT | Add comment to snippet. |
| `DELETE` | `/api/snippets/comments/{commentId}` | JWT | Delete comment (comment author only). |
| `POST` | `/api/snippets/{id}/explain` | JWT | Generate/fetch cached Gemini AI code explanation. |
| `POST` | `/api/snippets/{id}/share` | JWT | Generate a secret share token. |
| `POST` | `/api/snippets/{id}/unshare` | JWT | Revoke share token. |
| `GET` | `/api/snippets/shared/{token}` | Public | View snippet via share token (no account needed). |

### Users & Profiles (`/api/users`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/api/users/{username}` | Public | View public profile, social links, bio, and public snippets. |
| `PUT` | `/api/users/profile` | JWT | Update user bio, avatar URL, GitHub URL, and LinkedIn URL. |

### System & Health (`/actuator`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| `GET` | `/actuator/health` | Public | System and database health status. |

---

## Project Structure

```
CodeShareV2/
├── pom.xml                               # Maven build configuration (Java 17 & frontend plugin)
├── Dockerfile                            # Multi-stage Docker build
├── docker-compose.yml                    # Multi-container orchestration (App + MySQL 8)
├── .env.example                          # Environment template
│
├── src/main/java/com/codeshare/
│   ├── CodeShareApplication.java         # Spring Boot entry point
│   ├── controller/
│   │   ├── AuthApiController.java        # Registration, login, /me
│   │   ├── SnippetApiController.java     # Snippets CRUD, forking, likes, stars, comments, AI
│   │   ├── UserApiController.java       # User profiles & profile updates
│   │   └── SpaController.java            # Single-page application route forwarding
│   ├── dto/                              # Request & response payload schemas
│   ├── model/                            # JPA entities: User, Snippet, Comment
│   ├── repository/                       # Spring Data JPA repositories
│   ├── security/                         # Spring Security 6, JWT filter & utility
│   ├── service/                          # Business logic: Auth, Snippet, Comment, Gemini, RateLimiter
│   └── util/                             # DatabaseSeeder (initial demo data)
│
├── src/main/resources/
│   ├── application.properties            # Core configuration & profiles
│   └── application-mysql.properties      # MySQL production configuration
│
└── frontend/                             # React 19 Single Page Application
    ├── index.html
    ├── package.json
    ├── vite.config.js                    # Vite bundler & dev server proxy config
    └── src/
        ├── App.jsx                       # Client-side router & suspense
        ├── api.js                        # Axios instance with auth interceptors
        ├── context/AuthContext.jsx       # Authentication state management
        ├── components/
        │   ├── Navbar.jsx                # Responsive navigation header
        │   ├── Footer.jsx                # Footer component
        │   └── PrivateRoute.jsx          # Route guard for protected pages
        └── pages/
            ├── Home.jsx                  # Community snippet feed
            ├── ViewSnippet.jsx           # Snippet viewer, comments, forking, AI explanation
            ├── CreateSnippet.jsx         # New snippet form with CodeMirror editor
            ├── EditSnippet.jsx           # Snippet editor
            ├── MySnippets.jsx            # User's personal snippets
            ├── StarredSnippets.jsx       # User's starred snippets collection
            ├── UserProfile.jsx           # User profile & public snippets showcase
            ├── Search.jsx                # Keyword & language search
            ├── SharedSnippetView.jsx     # Token-based public snippet view
            ├── Login.jsx                 # User sign-in
            └── Register.jsx              # User registration
```
