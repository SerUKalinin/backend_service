CREATE TABLE roles
(
    id          BIGSERIAL PRIMARY KEY,
    role_type   VARCHAR(20) NOT NULL
);

CREATE TABLE users
(
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(255) UNIQUE NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL
);

CREATE TABLE users_roles
(
    user_id     BIGINT NOT NULL,
    role_id     BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE posts
(
    id          BIGSERIAL PRIMARY KEY,
    content     VARCHAR(4000) NOT NULL,
    user_id     BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);
