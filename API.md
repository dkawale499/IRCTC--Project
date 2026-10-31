# IRCTC Backend API

This document describes the REST API used by the Angular frontend.

## Base URL

During local development:

```text
http://localhost:8080
```

Configure this in Angular as an environment value:

```typescript
export const environment = {
  apiUrl: 'http://localhost:8080'
};
```

The backend allows requests from `http://localhost:4200` by default. To allow another frontend URL, change `app.cors.allowed-origins` in `src/main/resources/application.properties`. Multiple origins can be separated by commas.

## Authentication and authorization

Register or log in through `/auth`. Store the login response's `token` and send it as a Bearer token on authenticated requests:

```http
Authorization: Bearer <JWT>
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

`/auth/**`, `/swagger-ui/**`, and `/v3/api-docs/**` are public. Other routes require a valid JWT. Routes under `/admin/**` additionally require the `ADMIN` role. Public registration creates a `USER` account; it does not grant admin access.

For local admin bootstrap, the application creates the `dkawale` account only when `ADMIN_BOOTSTRAP_PASSWORD` is set in the environment before startup. The password is stored using the configured password encoder. The bootstrap does not change an existing account or promote an existing user; if `dkawale` already exists as a non-admin, startup fails so an administrator can resolve the conflict explicitly. Do not commit the password or use a weak password outside a disposable local environment. `ADMIN_BOOTSTRAP_USERNAME` can override the default username.

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

New passenger accounts receive a one-time ₹100 wallet signup bonus, recorded as a wallet credit. Previously registered passenger accounts receive the same one-time bonus when their wallet is first opened.

### Login

```http
POST /auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "dkawale",
  "password": "<ADMIN_BOOTSTRAP_PASSWORD>"
}
```

Response: `200 OK`

```json
{
  "token": "eyJ...",
  "role": "ADMIN"
}
```

For a regular user, supply the email and password used during registration; successful public registration returns a `USER` role on login.

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
GET /trains/search?source=Delhi&destination=Mumbai&journeyDate=2026-10-15
Authorization: Bearer <JWT>
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
    "availableSeats": 278,
    "journeyDate": "2026-10-15",
    "ticketFare": 275.50,
    "coaches": [
      {
        "id": 1,
        "coachType": "SL",
        "coachCount": 2,
        "seatsPerCoach": 72,
        "totalSeats": 144
      }
    ]
  }
]
```

`coaches` may be empty for older schedules that were created using only `availableSeats`.
The schedule includes the admin-configured `ticketFare`. Older schedules without a stored fare default to ₹100.

### Get a scheduled ticket fare

```http
GET /trains/fare?trainNo=12345&journeyDate=2026-10-15
Authorization: ******
```

Returns the configured fare as a decimal number. Booking validates the train, date, and route against this schedule; a missing schedule or mismatched route is rejected.

## Ticket APIs

### Book a ticket

```http
POST /ticket
Authorization: Bearer <JWT>
Content-Type: application/json
```

Request:

```json
{
  "name": "Asha Sharma",
  "dob": "1995-05-10",
  "gender": "FEMALE",
  "doj": "2026-10-15",
  "source": "Delhi",
  "dest": "Mumbai",
  "trainNo": "12345"
}
```

Booking is available to passenger accounts. The backend charges the configured fare for the matching train schedule from the authenticated passenger's wallet as part of the same transaction that creates the ticket. An insufficient balance returns `402 Payment Required`, including the fare and available balance; no ticket is created and no wallet debit is recorded. Older schedules without a stored fare use ₹100.

Response: `201 Created`

```json
{
  "ticketId": 1,
  "ticketStatus": "Confirmed",
  "ticketFare": 275.50,
  "trainNo": "12345",
  "name": "Asha Sharma",
  "dob": "1995-05-10",
  "gender": "FEMALE",
  "doj": "2026-10-15",
  "source": "Delhi",
  "dest": "Mumbai"
}
```

### Get a ticket / search PNR

Either path retrieves a ticket by its numeric ticket ID (PNR):

```http
GET /ticket/{ticketId}
GET /tickets/pnr/{ticketId}
Authorization: Bearer <JWT>
```

Response: `200 OK` with the ticket object above, including its current `ticketStatus`.

### Cancel a ticket

```http
POST /ticket/{ticketId}/cancel
Authorization: Bearer <JWT>
```

Response: `200 OK` with the updated ticket and `ticketStatus: "Cancelled"`. A ticket that is already cancelled cannot be cancelled again. Passenger accounts may cancel only their own tickets; a successful cancellation refunds the exact fare recorded on the ticket to the ticket owner's wallet.

### Get the authenticated passenger wallet

```http
GET /wallet
Authorization: Bearer <user-token>
```

Returns the current balance, signup-credit total, passenger account details, registration date, transaction count, and wallet transaction history for the authenticated passenger. The endpoint requires the `USER` role.

Response: `200 OK`

```json
{
  "balance": 100.00,
  "signupCreditTotal": 100.00,
  "totalTransactions": 1,
  "passengerName": "Passenger Name",
  "email": "passenger@example.com",
  "phone": "555-0100",
  "registeredAt": "2026-10-31T14:00:00",
  "transactions": [
    {
      "id": 1,
      "type": "CREDIT",
      "description": "Welcome signup bonus",
      "createdAt": "2026-10-31T14:00:00",
      "amount": 100.00,
      "balanceAfter": 100.00,
      "status": "Success"
    }
  ]
}
```

Wallet top-ups are not available. New registrations and existing accounts on first wallet access receive a one-time ₹100 welcome bonus.

### Get tickets

```http
GET /tickets
Authorization: Bearer <JWT>
```

`/tickets` is administrator-only and returns an array of all ticket objects.

Passenger accounts retrieve only their own tickets through:

```http
GET /tickets/mine
Authorization: ******
```

The endpoint requires the `USER` role and returns an array associated with the authenticated passenger.

Administrators can retrieve tickets for a separate status tab by supplying a status query parameter:

```http
GET /tickets?status=Confirmed
GET /tickets?status=RAC
GET /tickets?status=Waitlisted
GET /tickets?status=Cancelled
```

Status values are case-insensitive. An unsupported status returns `400 Bad Request`.

### Get ticket status totals

This administrator-only endpoint returns the counts across all tickets.

```http
GET /tickets/summary
Authorization: Bearer <JWT>
```

Response: `200 OK`

```json
{
  "totalTickets": 12,
  "confirmed": 7,
  "rac": 2,
  "waitlisted": 1,
  "cancelled": 2
}
```

The status counts only include tickets whose status matches one of the four supported statuses.

## Admin APIs

All endpoints under `/admin` require a valid JWT with the `ADMIN` role.

### List registered passenger accounts

```http
GET /admin/users
Authorization: Bearer <admin-token>
```

Response: `200 OK` with passenger profile fields only. Passwords and login credentials are never included.

```json
[
  {
    "id": 1,
    "fullName": "Asha Sharma",
    "email": "asha@example.com",
    "phone": "9876543210"
  }
]
```

### Create a passenger account

```http
POST /admin/users
Authorization: Bearer <admin-token>
Content-Type: application/json
```

Request uses the same fields as public registration. The password must contain at least 8 characters.

```json
{
  "fullName": "Asha Sharma",
  "email": "asha@example.com",
  "phone": "9876543210",
  "password": "password123"
}
```

Response: `201 Created` with the safe user profile. Invalid input returns `400 Bad Request`; an already registered email returns `409 Conflict`. New accounts are always assigned the `USER` role.

### Remove a passenger account

```http
DELETE /admin/users/{userId}
Authorization: Bearer <admin-token>
```

Response: `204 No Content`. A missing user returns `404 Not Found`. Administrator accounts cannot be removed through this endpoint.

### Get tickets

```http
GET /admin/tickets
GET /admin/tickets?status=RAC
Authorization: Bearer <ADMIN_JWT>
```

Without `status`, returns all tickets. The optional status accepts `Confirmed`, `RAC`, `Waitlisted`, or `Cancelled` (case-insensitive). An unsupported value returns `400 Bad Request`.

### Get ticket status totals

```http
GET /admin/tickets/summary
Authorization: Bearer <ADMIN_JWT>
```

Response: `200 OK` with the same ticket-status totals described in the Ticket APIs section.

### Create a train schedule and configure coach seats

```http
POST /admin/trains
Authorization: Bearer <ADMIN_JWT>
Content-Type: application/json
```

Request:

```json
{
  "trainNo": "12345",
  "source": "Delhi",
  "destination": "Mumbai",
  "journeyDate": "2026-10-15",
  "ticketFare": 275.50,
  "coaches": [
    { "coachType": "SL", "coachCount": 2, "seatsPerCoach": 72 },
    { "coachType": "3AC", "coachCount": 1, "seatsPerCoach": 64 },
    { "coachType": "2AC", "coachCount": 1, "seatsPerCoach": 46 },
    { "coachType": "1AC", "coachCount": 1, "seatsPerCoach": 24 }
  ]
}
```

Supported coach types are `SL`, `3AC`, `2AC`, and `1AC`. Each coach type may appear once. `coachCount` and `seatsPerCoach` must be positive integers. The API calculates `availableSeats` as the sum of `coachCount * seatsPerCoach` (278 for this example). The journey date must be today or later. A train number cannot have more than one schedule on the same journey date.

For backward compatibility, `coaches` can be omitted if a non-negative `availableSeats` value is provided.
If `ticketFare` is omitted, the schedule uses the backward-compatible ₹100 fare. Admins can update an existing schedule's fare:

```http
PUT /admin/trains/{scheduleId}/fare
Authorization: ******
Content-Type: application/json
```

Request body: `{ "ticketFare": 325.00 }`. The fare must be greater than zero and have no more than two decimal places. The endpoint returns `200 OK` with the updated schedule.

Response: `201 Created` with the created schedule, total available seats, and coach details. The response includes each coach's `id` and calculated `totalSeats`.

### Update coach classes and seats

This replaces the full coach configuration for an existing schedule and recalculates its `availableSeats`.

```http
PUT /admin/trains/{scheduleId}/coaches
Authorization: Bearer <ADMIN_JWT>
Content-Type: application/json
```

Request body is an array of coach configurations:

```json
[
  { "coachType": "SL", "coachCount": 3, "seatsPerCoach": 72 },
  { "coachType": "3AC", "coachCount": 2, "seatsPerCoach": 64 }
]
```

At least one coach configuration is required. The same supported coach types and positive-value rules from schedule creation apply.

Response: `200 OK` with the updated schedule and coach details. Use the schedule `id` returned when the schedule is created or listed.

### Get all train schedules

```http
GET /admin/trains
Authorization: Bearer <ADMIN_JWT>
```

Response: `200 OK` with an array of schedules, including coach details and total available seats.

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
- `403 Forbidden`: the authenticated user does not have permission (for example, a `USER` calling an admin endpoint).
- `404 Not Found`: the requested resource does not exist.
- `409 Conflict` or `400 Bad Request`: show the backend message to the user.

The backend currently returns framework-generated error responses for validation and service exceptions, so Angular should handle both an object with a `message` field and a plain error response.
