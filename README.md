cd D:\EDGE\vitavault-backend
$md = @'
# VitaVault

![CI](https://github.com/Vishlli/vitavault/actions/workflows/ci.yml/badge.svg)

A Java/Spring Boot backend for a simulated digital wellness-service marketplace, built to practise payment-system fundamentals: idempotent payment processing, a double-entry ledger, refunds, and secure authentication.

> All payments are simulated. No real money or real health data is involved.

## What it does

- Registration and login with BCrypt password hashing and JWT access tokens (USER and PROVIDER roles)
- A service catalogue and a simulated wallet per user
- `POST /api/v1/payments` with a required `Idempotency-Key` header: retries return the original result instead of charging again, and reusing a key with a different request is rejected (HTTP 422)
- Row-level locking on the wallet so concurrent requests cannot overspend
- A double-entry ledger: every payment writes a debit and a credit entry, and refunds write reversal entries rather than editing history
- Full refunds, limited to one per payment by a database unique constraint

## Tech stack

Java 25, Spring Boot 4.1, Spring Security (JWT resource server), Spring Data JPA / Hibernate, PostgreSQL, Flyway migrations, JUnit 5, Maven, GitHub Actions.

## Running locally

1. Create a PostgreSQL database named `vitavault`.
2. Set `DB_PASSWORD` (your Postgres password) and `JWT_SECRET` (any random string of 32+ characters) as environment variables.
3. From the `vitavault-backend` folder: `./mvnw spring-boot:run`

Flyway creates the schema on first start.

## API examples
