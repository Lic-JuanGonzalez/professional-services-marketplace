CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE professional_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    headline VARCHAR(150) NOT NULL,
    bio TEXT,
    category VARCHAR(80) NOT NULL,
    location VARCHAR(150),
    hourly_rate NUMERIC(10, 2),
    avg_rating DOUBLE PRECISION NOT NULL DEFAULT 0,
    review_count INTEGER NOT NULL DEFAULT 0,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_professional_profiles_category ON professional_profiles(category);

CREATE TABLE service_offerings (
    id BIGSERIAL PRIMARY KEY,
    professional_id BIGINT NOT NULL REFERENCES professional_profiles(id),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(80) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_service_offerings_professional ON service_offerings(professional_id);
CREATE INDEX idx_service_offerings_category ON service_offerings(category);

CREATE TABLE hires (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES users(id),
    professional_id BIGINT NOT NULL REFERENCES professional_profiles(id),
    service_offering_id BIGINT NOT NULL REFERENCES service_offerings(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    scheduled_date TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_hires_client ON hires(client_id);
CREATE INDEX idx_hires_professional ON hires(professional_id);
CREATE INDEX idx_hires_status ON hires(status);

CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    hire_id BIGINT NOT NULL UNIQUE REFERENCES hires(id),
    reviewer_id BIGINT NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
