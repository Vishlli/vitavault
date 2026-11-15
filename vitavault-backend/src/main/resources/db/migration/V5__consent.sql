CREATE TABLE wellness_records (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    category VARCHAR(20) NOT NULL CHECK (category IN ('SLEEP','ACTIVITY')),
    record_date DATE NOT NULL,
    metric_value NUMERIC(8,2) NOT NULL CHECK (metric_value >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE consents (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    provider_id BIGINT NOT NULL REFERENCES users(id),
    category VARCHAR(20) NOT NULL CHECK (category IN ('SLEEP','ACTIVITY')),
    status VARCHAR(10) NOT NULL CHECK (status IN ('ACTIVE','REVOKED')),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked_at TIMESTAMP
);

CREATE TABLE access_logs (
    id BIGSERIAL PRIMARY KEY,
    consent_id BIGINT REFERENCES consents(id),
    provider_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    outcome VARCHAR(10) NOT NULL,
    accessed_at TIMESTAMP NOT NULL DEFAULT NOW()
);