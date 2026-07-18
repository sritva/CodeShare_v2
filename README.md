# CodeShare

A full-stack platform for sharing and discovering code snippets. Built with Spring Boot 3 and React.

## Features

- **Authentication** — Register and log in with JWT-based stateless auth. Passwords hashed with BCrypt.
- **Snippet Management** — Create, edit, and delete snippets with public/private visibility. Ownership enforced server-side.
- **Community Feed** — Paginated list of all public snippets, newest first.
- **Search** — Filter by title keyword and/or language.
- **AI Code Explanation** — Powered by Google Gemini. Cached per snippet, rate limited per session.
- **Shareable Links** — Generate a token-based link for any snippet. Viewable without an account.
- **Docker** — Full stack with a single `docker-compose up`.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2.5 |
| Security | Spring Security 6, JWT (jjwt 0.12.3), BCrypt |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL 8 (H2 in-memory for local dev) |
| Frontend | React 19, Vite, Tailwind CSS 3, CodeMirror 5 |
| AI | Google Gemini API |
| Build | Maven 3.9, frontend-maven-plugin |
| Tests | JUnit 5, Mockito |

## Running Locally

### H2 In-Memory (no setup required)

```bash
mvn spring-boot:run
```

Open **http://localhost:8080**

### MySQL (persistent data)

1. Create the database:
   ```sql
   CREATE DATABASE codeshare;
   ```

2. Set environment variables:
   ```bash
   export SPRING_DATASOURCE_USERNAME=root
   export SPRING_DATASOURCE_PASSWORD=yourpassword
   export JWT_SECRET=a-long-random-secret-string
   ```

3. Run with the MySQL profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```

### Frontend Dev Server

```bash
# Terminal 1 — backend
mvn spring-boot:run -Dspring-boot.run.profiles=mysql

# Terminal 2 — frontend
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**

### Tests

```bash
mvn test
```

## Docker

```bash
cp .env.example .env
docker-compose up --build
```

Open **http://localhost:8080**

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `JWT_SECRET` | Yes | Secret for signing JWTs. Min 32 characters. |
| `GEMINI_API_KEY` | No | Google Gemini key. AI features disabled if not set. |
| `SPRING_DATASOURCE_URL` | MySQL only | JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | MySQL only | Database username |
| `SPRING_DATASOURCE_PASSWORD` | MySQL only | Database password |

## Demo Accounts

Seeded automatically on first run.

| Username | Password |
|---|---|
| `alice` | `password123` |
| `bob` | `password123` |
| `charlie` | `password123` |
| `michael` | `password123` |

## API Endpoints

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register |
| POST | `/api/auth/login` | No | Login — returns `{ token, username }` |
| GET | `/api/auth/me` | JWT | Current user |

### Snippets
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/snippets/public?page=0` | No | Public feed |
| GET | `/api/snippets/search?keyword=&language=&page=0` | No | Search |
| GET | `/api/snippets/mine` | JWT | Your snippets |
| GET | `/api/snippets/{id}` | Optional | View snippet |
| POST | `/api/snippets` | JWT | Create |
| PUT | `/api/snippets/{id}` | JWT | Update (owner only) |
| DELETE | `/api/snippets/{id}` | JWT | Delete (owner only) |
| POST | `/api/snippets/{id}/explain` | JWT | AI explanation |
| POST | `/api/snippets/{id}/share` | JWT | Enable share link |
| POST | `/api/snippets/{id}/unshare` | JWT | Disable share link |
| GET | `/api/snippets/shared/{token}` | No | View via share token |

## Project Structure

```
src/main/java/com/codeshare/
├── controller/     # AuthApiController, SnippetApiController, SpaController
├── model/          # User, Snippet
├── repository/     # UserRepository, SnippetRepository
├── security/       # SecurityConfig, JwtUtil, JwtAuthFilter
├── service/        # AuthService, SnippetService, GeminiClient
└── util/           # DatabaseSeeder

frontend/src/
├── api.js
├── context/AuthContext.jsx
├── components/     # Navbar, PrivateRoute
└── pages/          # Home, ViewSnippet, CreateSnippet, EditSnippet,
                    # MySnippets, Search, SharedSnippetView, Login, Register
```
