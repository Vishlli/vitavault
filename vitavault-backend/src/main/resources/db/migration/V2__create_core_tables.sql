CREATE TABLE services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'INR',
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE wallets (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    balance NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    currency CHAR(3) NOT NULL DEFAULT 'INR'
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    service_id BIGINT NOT NULL REFERENCES services(id),
    amount NUMERIC(12,2) NOT NULL CHECK (amount >= 0),
    currency CHAR(3) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN
        ('PENDING','SUCCESS','FAILED','REFUND_PENDING','REFUNDED','REFUND_FAILED')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE idempotency_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    idempotency_key VARCHAR(100) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    transaction_id BIGINT REFERENCES transactions(id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, idempotency_key)
);

CREATE TABLE ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL REFERENCES transactions(id),
    account VARCHAR(50) NOT NULL,
    debit NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (debit >= 0),
    credit NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (credit >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);