CREATE TABLE refresh_tokens (
                                id SERIAL PRIMARY KEY,
                                username VARCHAR(255) NOT NULL,
                                token VARCHAR(255) NOT NULL,
                                expires_at TIMESTAMP NOT NULL,
                                created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_username ON refresh_tokens(username);