CREATE TABLE repo_snapshots
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    repo_id     BIGINT      NOT NULL,
    captured_on DATE        NOT NULL,
    stars       INT         NOT NULL,
    forks       INT         NOT NULL,
    open_issues INT         NOT NULL,
    captured_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_repo_day (repo_id, captured_on),
    CONSTRAINT fk_snapshot_repo FOREIGN KEY (repo_id) REFERENCES repositories (id)
);