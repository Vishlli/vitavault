cd D:\EDGE\vitavault-backend
git pull --rebase
$md = @'
# VitaVault

![CI](https://github.com/Vishlli/vitavault/actions/workflows/ci.yml/badge.svg)

A Java/Spring Boot backend for a simulated digital wellness-service marketplace, built to practise payment-system fundamentals: idempotent payment processing, a double-entry ledger, refunds, secure authentication, and user-controlled data sharing.

> All payments are simulated. No real money or real health data is involved.

## What it does

- Registration and login with BCrypt password hashing and JWT access tokens (USER and PROVIDER roles)
- A service catalogue and a simulated wallet per user
- `POST /api/v1/payments` with a required `Idempotency-Key` header: retries return the original result instead of charging again, and reusing a key with a different request is rejected (HTTP 422)
- Row-level locking on the wallet so concurrent requests cannot overspend
- A double-entry ledger: every payment writes a debit and a credit entry, and refunds write reversal entries rather than editing history
- Full refunds, limited to one per payment by a database unique constraint
- Consent-based data sharing: a user grants a provider time-limited access to one data category (SLEEP or ACTIVITY), can revoke it at any time, and every provider access attempt (allowed or denied) is written to an audit log the user can read

## Tech stack

Java 25, Spring Boot 4.1, Spring Security (JWT resource server), Spring Data JPA / Hibernate, PostgreSQL, Flyway migrations, JUnit 5, Maven, GitHub Actions.

## Running locally

1. Create a PostgreSQL database named `vitavault`.
2. Set `DB_PASSWORD` (your Postgres password) and `JWT_SECRET` (any random string of 32+ characters) as environment variables.
3. From the `vitavault-backend` folder: `./mvnw spring-boot:run`

Flyway creates the schema on first start.

## API examples

POST /api/v1/auth/register {“email”:”a@test.com”,”password”:”Password123",”role”:”USER”}
POST /api/v1/auth/login {“email”:”a@test.com”,”password”:”Password123"}
GET /api/v1/services
POST /api/v1/payments header: Idempotency-Key: <unique key> {“serviceId”:1}
GET /api/v1/payments/{id} (payment plus its ledger entries)
POST /api/v1/payments/{id}/refunds
POST /api/v1/wellness/records {“category”:”SLEEP”,”recordDate”:”2026-11-14",”value”:7.2}
POST /api/v1/consents {“providerId”:2,”category”:”SLEEP”,”days”:7}
POST /api/v1/consents/{id}/revoke
GET /api/v1/access-logs
GET /api/v1/provider/data/{userId}?category=SLEEP (PROVIDER role; 403 without valid consent)


## Testing

- **Automated (runs in CI on every push):** `PaymentConcurrencyTest` sends 10 simultaneous payment requests with the same idempotency key against PostgreSQL. It asserts that exactly one transaction is created, the other nine return the same transaction, the ledger has two entries, and the wallet is debited once.
- **Verified manually against the running API:** wrong-key rejection, refund and duplicate-refund rejection (HTTP 409), ledger balance (debits equal credits), and the full consent flow (denied before consent, allowed after, denied again after revocation, with the audit log recording all three).

Automated tests for refunds and consent are the next planned addition.

## Known limitations

- Payments are simulated; there is no real payment provider
- Refunds are full-amount only
- Only the concurrency test is automated so far
- No frontend and no deployment yet
- Single currency (INR)
'@
[System.IO.File]::WriteAllText("$PWD\README.md", $md, (New-Object System.Text.UTF8Encoding($false)))
git add .
git commit -m "Update README: consent, testing, limitations"
git push
