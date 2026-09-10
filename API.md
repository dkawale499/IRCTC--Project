# IRCTC Backend API

This document is the contract for the Angular frontend.

## Base URL

During local development:

```text
http://localhost:8080
```

Configure this in Angular as an environment value, for example:

```typescript
export const environment = {
  apiUrl: 'http://localhost:8080'
};
```

The backend allows requests from `http://localhost:4200`. To allow another frontend URL, change `app.cors.allowed-origins` in `src/main/resources/application.properties`. Multiple origins can be separated by commas.

## Authentication

1. Register or log in through `/auth`.
2. Store the returned `token` in Angular storage.
3. Send it on protected requests:

```http
Authorization: Bearer <token>
```

Angular interceptor example:

```typescript
intercept(request: HttpRequest<unknown>, next: HttpHandler) {
  const token = localStorage.getItem('irctc_token');
  const authenticatedRequest = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;
  return next.handle(authenticatedRequest);
}
```

`/auth/**`, `/swagger-ui/**`, and `/v3/api-docs/**` are public. All other routes require a valid JWT.

## Auth APIs

### Register

```http
POST /auth/register
Content-Type: application/json
```

Request:

```json
{
  "fullName": "Asha Sharma",
  "email": "asha@example.com",
  "phone": "9876543210",
  "password": "password123"
}
```

Response: `201 Created`

```text
Registration successful
```

### Login

```http
POST /auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "asha@example.com",
  "password": "password123"
}
```

Response: `200 OK`

```json
{
  "token": "eyJ...",
  "role": "USER"
}
```

### Forgot password

```http
POST /auth/forgot-password
Content-Type: application/json
```

Request:

```json
{
  "email": "asha@example.com"
}
```

Response: `200 OK`

```json
{
  "message": "Reset token created. Send it to /auth/reset-password",
  "resetToken": "token-value"
}
```

For an unknown email, `resetToken` is `null`.

### Reset password

```http
POST /auth/reset-password
Content-Type: application/json
```

Request:

```json
{
  "token": "token-value",
  "newPassword": "newpassword123"
}
```

Response: `200 OK`

```text
Password reset successful
```

## Train APIs

### Search trains

```http
GET /trains/search?source=Delhi&destination=Mumbai&journeyDate=2026-10-01
Authorization: Bearer <token>
```

`journeyDate` must use `yyyy-MM-dd` format.

Response: `200 OK`

```json
[
  {
    "id": 1,
    "trainNo": "12345",
    "source": "Delhi",
    "destination": "Mumbai",
    "availableSeats": 500,
    "journeyDate": "2026-10-01"
  }
]
```

## Ticket APIs

### Book a ticket

```http
POST /ticket
Authorization: Bearer <token>
Content-Type: application/json
```

Request:

```json
{
  "name": "Asha Sharma",
  "dob": "1995-05-10",
  "gender": "FEMALE",
  "doj": "2026-10-01",
  "source": "Delhi",
  "dest": "Mumbai",
  "trainNo": "12345"
}
```

Response: `201 Created`

```json
{
  "ticketId": 1,
  "ticketStatus": "Confirmed",
  "trainNo": "12345",
  "name": "Asha Sharma",
  "dob": "1995-05-10",
  "gender": "FEMALE",
  "doj": "2026-10-01",
  "source": "Delhi",
  "dest": "Mumbai"
}
```

### Get a ticket

```http
GET /ticket/{ticketId}
Authorization: Bearer <token>
```

Response: `200 OK` with the ticket object above.

### Cancel a ticket

```http
POST /ticket/{ticketId}/cancel
Authorization: Bearer <token>
```

Response: `200 OK` with the updated ticket and `ticketStatus: "Cancelled"`.

### Get all tickets

```http
GET /tickets
Authorization: Bearer <token>
```

Response: `200 OK` with an array of ticket objects.

## Admin APIs

These endpoints are available under `/admin` and require authentication in the current backend.

### Get all tickets

```http
GET /admin/tickets
Authorization: Bearer <token>
```

### Create a train schedule

```http
POST /admin/trains
Authorization: Bearer <token>
Content-Type: application/json
```

Request:

```json
{
  "trainNo": "12345",
  "source": "Delhi",
  "destination": "Mumbai",
  "availableSeats": 500,
  "journeyDate": "2026-10-01"
}
```

Response: `201 Created` with the train schedule object.

### Get all train schedules

```http
GET /admin/trains
Authorization: Bearer <token>
```

## Swagger / OpenAPI

Interactive API documentation is available at:

```text
http://localhost:8080/swagger-ui.html
```

The OpenAPI JSON document is available at:

```text
http://localhost:8080/v3/api-docs
```

## Common Angular error handling

- `401 Unauthorized`: remove the stored token and navigate to login.
- `403 Forbidden`: the authenticated user does not have permission.
- `404 Not Found`: the requested ticket or resource does not exist.
- `409 Conflict` or `400 Bad Request`: show the backend message to the user.

The backend currently returns framework-generated error responses for validation and service exceptions, so Angular should handle both an object with a `message` field and a plain error response.
