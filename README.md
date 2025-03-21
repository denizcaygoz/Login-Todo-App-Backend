# Spring Boot Backend Project - User Authentication and Todo Management
Backend project built using Spring Boot, to handle user authentication and todo list management. The authentication system uses JWT tokens for secure login and registration. Also, the application provides GET and POST endpoints to retrieve and update user todo lists.
🔗 **[Login-Todo-App-Backend Frontend Project](https://github.com/denizcaygoz/Login-Todo-App)**  

---

## **Features**
- **User Authentication**: Login and Registration using JWT.
- **Todo Management**: Retrieve and update todos for authenticated users.
- **Database Integration**: PostgreSQL database managed through pgAdmin.

---

![authflow](https://github.com/user-attachments/assets/00a73d8e-4210-435a-8724-000b644ebd82)

## JWT Authentication Flow

1. **User Logs in**: Sends a `POST` request to `/user/login` with email and password.
2. **Server Generates JWT**: If credentials are correct, the server creates and returns a JWT.
3. **Client Sends Requests with JWT**: The client includes the JWT in the `Authorization` header when making authenticated requests.
4. **Server Verifies JWT**: The server validates the JWT, extracts the user information, and processes the request.

---

## Technologies Used

- **Spring Security** (Authorization and Authentication)
- **Spring Data JPA** (Database interaction)
- **JWT (JSON Web Token)** (Authentication)
- **PostgreSQL** (Database)
- **pgAdmin** (Database Management)
- **Postman** (test the endpoints.)

---

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/auth/login` | Login with email and password |
| `POST` | `/auth/register` | Register a new user |

### Todo Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/user/todolist` | Fetch user's todos (requires authentication) |
| `POST` | `/user/todolist` | Update user's todo list |

---

## Database Schema

![database](https://github.com/user-attachments/assets/25f2628f-510d-4727-9abb-8db710efe5ec)

The application uses a **PostgreSQL** database with the following schema:

### Users Table

```
users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
);
```

### Todos Table

```
todos (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL,
  task TEXT NOT NULL,
  is_completed BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### User Roles Enum

```
enum ERole {
  ROLE_USER,
  ROLE_MODERATOR,
  ROLE_ADMIN
}
```

### User Roles Table

```
user_roles (
  user_id INT NOT NULL,
  role_id INT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);
```

---
