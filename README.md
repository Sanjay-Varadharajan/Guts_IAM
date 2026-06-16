# Guts IAM

> Authentication is easy.
>
> Building authentication that survives token theft, session abuse, brute force attacks, replay attempts, developer mistakes, and future maintenance is where things become interesting.

---

## Overview

Guts IAM is a centralized Identity and Access Management (IAM) platform built using Spring Boot.

The project is focused on building a security-first authentication service that can act as the identity provider for future applications. Rather than treating authentication as a simple login endpoint, the system focuses on token lifecycle management, account security, session visibility, auditability, and attack surface reduction.

The primary goals are:

- Centralized authentication
- Centralized authorization
- Session visibility
- Security event auditing
- Token lifecycle management
- Defensive security controls

The philosophy is simple:

```text
Trust nothing.
Validate everything.
Log important actions.
Expire credentials.
Rotate secrets.
Assume mistakes will happen.
```

Because eventually they will.

---

## Current Feature Set

### Authentication

Implemented:

- User Registration
- User Login
- User Logout
- Password Verification
- JWT Generation
- Refresh Token Generation

The system authenticates users using email and password credentials and issues JWT tokens upon successful authentication.

Passwords are never stored directly.

A database full of plaintext passwords is not an authentication system. It is a future cybersecurity case study.

---

### Password Security

Passwords are secured using BCrypt hashing.

Characteristics:

- One-way hashing
- Salted hashes
- Resistant to rainbow table attacks
- Computationally expensive by design

Even if the database is compromised, the original passwords are not directly recoverable.

The system stores:

```text
$2a$14$...
```

Not:

```text
password123
```

Which is generally considered an improvement.

---

## JWT Authentication

Authentication is based on JSON Web Tokens (JWT).

Two token types are used:

### Access Token

Used for:

- Authentication
- Authorization

Contains user identity information required for resource access.

Example structure:

```json
{
  "sub": "user-id",
  "email": "user@example.com",
  "role": "USER"
}
```

Access tokens are intentionally short-lived and are designed to be disposable.

---

### Refresh Token

Used for:

- Access token renewal
- Session continuity

Refresh tokens are persisted and managed within the system rather than treated as stateless artifacts.

This enables:

- Revocation
- Session tracking
- Token lifecycle control

---

## Refresh Token Rotation

The platform implements refresh token rotation.

Flow:

```text
Client sends Refresh Token
            │
            ▼
Refresh Token Validation
            │
            ▼
Old Refresh Token Revoked
            │
            ▼
New Refresh Token Created
            │
            ▼
New Access Token Issued
```

Benefits:

- Reduced replay attack risk
- Better token lifecycle management
- Reduced impact of token leakage

A stolen refresh token should not remain useful forever.

Attackers appreciate that.

The system does not.

---

## Authorization

Role-based authorization is implemented.

Current roles:

```text
ROLE_USER
ROLE_ADMIN
```

Authorization decisions are enforced through Spring Security.

Example:

```java
.hasRole("ADMIN")
```

This ensures protected resources remain accessible only to authorized users.

Surprisingly, "I know a guy" is not a valid authorization mechanism.

---

## Session Tracking

Every authentication session is tracked.

Information recorded includes:

- IP Address
- User Agent
- Geographic Location
- Login Timestamp

This allows the system to maintain visibility into account activity and login history.

Example:

```text
IP Address: 1.1.1.1
User Agent: Firefox 150
Location: Chennai, India
```

This data helps answer questions such as:

```text
Was that login mine?
```

Using evidence instead of optimism.

---

## Login History

The platform maintains authentication history records.

Stored information includes:

- Login events
- Refresh token events
- Logout events
- Associated metadata

This creates an auditable timeline of account activity.

A security incident without logs is essentially archaeology.

---

## Security Audit Logging

Security-sensitive actions are recorded within an audit trail.

Current logged actions include:

```text
LOGIN
LOGOUT
REFRESH_TOKEN_CREATED
TOKEN_REFRESHED
```

Audit records include:

- Event type
- Timestamp
- IP Address
- User Agent
- Geographic Location

Purpose:

- Security investigations
- Activity tracking
- Incident response
- Operational visibility

The objective is simple:

If something important happens, it should leave evidence.

---

## User Enumeration Protection

A common authentication mistake is revealing whether an account exists.

Bad:

```text
Email does not exist
```

Also bad:

```text
Incorrect password
```

An attacker can use those responses to identify valid accounts.

Instead, the system returns:

```text
Invalid credentials
```

Regardless of whether:

```text
User does not exist
```

or

```text
Password is incorrect
```

The user receives the same response.

Attackers receive less information.

Everyone wins except the attacker.

---

## Timing Attack Mitigation

Authentication systems can unintentionally reveal account existence through response timing.

Example:

```text
Existing User
→ Password Hash Verification

Non Existing User
→ Immediate Response
```

The processing time becomes different.

An attacker can measure that difference repeatedly and infer valid accounts.

To reduce this risk, the system performs a dummy BCrypt verification even when a user does not exist.

Example:

```java
passwordEncoder.matches(
    password,
    dummyHashedPassword
);
```

This helps normalize response times and makes timing-based account discovery more difficult.

Not impossible.

Just significantly more annoying.

Which is often the entire goal of defensive security.

---

## Brute Force Protection

The platform tracks failed authentication attempts.

Capabilities include:

- Failed login monitoring
- Attempt counting
- Account lock handling

Purpose:

- Reduce password guessing attacks
- Detect abuse
- Slow automated login attempts

Computers are extremely patient.

Security controls should be too.

---

## API Versioning

Endpoints are versioned.

Current format:

```text
/api/v1/auth/register
/api/v1/auth/login
/api/v1/auth/logout
/api/v1/auth/refresh
```

Benefits:

- Controlled API evolution
- Backward compatibility
- Safer future changes

Future-you will eventually want to modify an endpoint.

Versioning is future-you's apology note.

---

## Data Model

### User

Stores:

```text
ID
Email
Password Hash
Role
Created Timestamp
Updated Timestamp
```

Represents authenticated platform users.

---

### Refresh Token

Stores:

```text
Token
User Reference
Expiry Timestamp
Revocation Status
Creation Timestamp
```

Represents active authentication sessions and token lifecycle state.

---

### Audit Log

Stores:

```text
Action
Timestamp
IP Address
User Agent
Location
Associated User
```

Represents historical security activity.

---

## Technology Stack

### Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA

### Database

- PostgreSQL

### Authentication

- JWT
- BCrypt

### Build Tool

- Maven

---

## Security Design Principles

The current implementation follows several principles:

### Least Information Exposure

Users receive only the information necessary to interact with the system.

Attackers receive as little information as possible.

---

### Defense in Depth

Security is not dependent on a single mechanism.

Instead, multiple layers exist:

- Password hashing
- JWT validation
- Refresh token rotation
- Audit logging
- Brute force protection
- User enumeration protection
- Timing attack mitigation

If one layer fails, others remain.

---

### Auditability

Security-relevant actions generate records.

The goal is to make account activity traceable and explainable.

Because "something weird happened" is not an investigation report.

---

## Current Status

Version:

```text
v1.1
```

State:

```text
Active Development
```

Focus:

```text
Identity Management
Authentication Security
Session Management
Auditability
```

The project has moved beyond basic JWT authentication and now focuses on the operational and security concerns that appear once real users, real sessions, and real attack scenarios enter the picture.

Turns out generating a token is the easy part.

Managing everything that happens after generating it is where the work begins.
