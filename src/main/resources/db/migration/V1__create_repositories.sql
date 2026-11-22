
CREATE TABLE repositories
(
    id BIGINT NOT NULL ,
    full_name VARCHAR(255) NOT NULL ,
    description TEXT,
    language VARCHAR(100),
    html_url VARCHAR(500) NOT NULL ,
    topics JSON NOT NULL ,
    fork BOOLEAN NOT NULL ,
    archived BOOLEAN NOT NULL ,
    created_at DATETIME(6) NOT NULL,
    first_seen_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
);