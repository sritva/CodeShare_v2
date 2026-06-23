# CodeShare

A code snippet sharing platform built with Spring Boot 3, JSP, and H2 or MySQL. Users can register, log in, create public or private code snippets, browse community snippets, and search by title or language.

## Features

- **User Authentication**: Secure registration and login (BCrypt passwords).
- **Snippet Management**: Create, view, edit, and delete code snippets with public/private visibility.
- **Snippet Ownership**: Strict ownership enforcement—only creators can edit or delete their snippets.
- **Single Active Session**: Ensures one active session per user.
- **Advanced Search**: Search snippets by title and/or language.
- **Interactive Code Editor**: Integrated CodeMirror in the creation/editing forms for full code editing capabilities (line numbers, auto-matching brackets).

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2.5 |
| Security | Spring Security 6, BCrypt |
| Persistence | Spring Data JPA, Hibernate, MySQL |
| Views | JSP (server-side rendered), JSTL |
| Build | Maven |

## Database Setup

By default, the application runs on a zero-setup **in-memory H2 database** (resetting on restart). 

* To inspect H2 database tables, visit **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:codeshare`, username: `sa`, password: empty).

If you want persistent storage, you can run the app with **MySQL**:
1. Create a database in MySQL: `CREATE DATABASE codeshare;`
2. Update your credentials in `src/main/resources/application-mysql.properties`.

## Running Tests

```powershell
.\mvn.bat test
```

Runs 13 unit tests covering `AuthService` and `SnippetService` (validation, ownership, CRUD).

---

## How to Run

Run the following command from the project root:

* **Using H2 (Default):**
  ```powershell
  .\mvn.bat spring-boot:run
  ```

* **Using MySQL:**
  ```powershell
  .\mvn.bat spring-boot:run "-Dspring-boot.run.profiles=mysql"
  ```

Once started, open **http://localhost:8080** in your browser.

## Sample Credentials

The application is seeded with the following test accounts:

| Username | Password |
|---|---|
| `alice` | `password123` |
| `bob` | `password123` |
| `charlie` | `password123` |
| `michael` | `password123` |

## Project Structure

```
src/
├── main/
│   ├── java/com/codeshare/
│   │   ├── controller/       # AuthController, SnippetController
│   │   ├── model/            # User, Snippet entities
│   │   ├── repository/       # Spring Data JPA repositories
│   │   ├── security/         # SecurityConfig, UserDetailsService, SessionFilter
│   │   ├── service/          # AuthService, SnippetService
│   │   └── util/             # PasswordUtil, DatabaseSeeder
│   ├── resources/
│   │   ├── application.properties
│   │   ├── application-mysql.properties
│   │   └── static/css/style.css
│   └── webapp/WEB-INF/jsp/   # JSP views
└── test/java/com/codeshare/
    └── service/              # AuthServiceTest, SnippetServiceTest
```

## Routes

| Method | Path | Description |
|---|---|---|
| GET | `/login` | Login page |
| GET | `/register` | Register page |
| POST | `/register` | Submit registration |
| GET | `/home` | All public snippets |
| GET | `/my-snippets` | Current user's snippets |
| GET | `/create-snippet` | New snippet form |
| POST | `/create-snippet` | Submit new snippet |
| GET | `/view-snippet?id=` | View a snippet |
| GET | `/edit-snippet?id=` | Edit snippet form |
| POST | `/edit-snippet` | Submit edit |
| POST | `/delete-snippet` | Delete snippet |
| GET | `/search` | Search snippets |
| GET | `/logout` | Log out |
